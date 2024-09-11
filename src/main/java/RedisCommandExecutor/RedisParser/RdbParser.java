package RedisCommandExecutor.RedisParser;

import DataUtils.KeyValue;
import DataUtils.KeyValuePairData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class RdbParser {

    public static final byte OPCODE_END_OF_FILE = (byte)0xFF;
    public static final byte OPCODE_DATABASE_SELECTOR = (byte)0xFE;
    public static final byte OPCODE_EXPIRE_TIME = (byte)0xFD;
    public static final byte OPCODE_EXPIRE_TIME_MILLISECONDS = (byte)0xFC;
    public static final byte OPCODE_RESIZE_DATABASE = (byte)0xFB;
    public static final byte OPCODE_AUXILIARY_FIELDS = (byte)0xFA;
    public static final byte LENGTH_6BIT = 0b00;
    public static final byte LENGTH_14BIT = 0b01;
    public static final byte LENGTH_32BIT = 0b10;
    public static final byte LENGTH_SPECIAL = 0b11;
    public static final byte STRING_INTEGER_8BIT = 0;
    public static final byte STRING_INTEGER_16BIT = 1;
    public static final byte STRING_INTEGER_32BIT = 2;
    public static final byte STRING_VALUE_TYPE = 0;
    private final DataInputStream inputStream;

    public RdbParser(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void load() throws IOException {
        parseMagic();
        final int version = parseVersion();
        log("version", version);
        Integer databaseNumber = null;
        while (true) {
            final byte opcode = inputStream.readByte();
            if (opcode == OPCODE_END_OF_FILE) {
                log("end of file");
                break;
            }
            switch (opcode) {
                case OPCODE_AUXILIARY_FIELDS: {
                    final String key = readString();
                    final String value = readString();
                    log("metadata", key, value);
                    break;
                }
                case OPCODE_DATABASE_SELECTOR: {
                    databaseNumber = readUnsignedByte();
                    log("databaseNumber", databaseNumber);
                    break;
                }
                case OPCODE_RESIZE_DATABASE: {
                    final int hashTableSize = readLength();
                    log("hashTableSize", hashTableSize);
                    final int expireHashTableSize = readLength();
                    log("expireHashTableSize", expireHashTableSize);
                    break;
                }
                case OPCODE_EXPIRE_TIME: {
                    throw new UnsupportedOperationException(
                            "unsupported OPCODE_EXPIRE_TIME");
                }
                case OPCODE_EXPIRE_TIME_MILLISECONDS:
                default: {
                    if (databaseNumber == null) {
                        throw new IllegalArgumentException("unexpected value: " + opcode);
                    }
                    int valueType = opcode;
                    long expiration = -1;
                    if (opcode == OPCODE_EXPIRE_TIME_MILLISECONDS) {
                        expiration = readUnsignedLong();
                        valueType = readUnsignedByte();
                        log("expiration", expiration);
                    }
                    final String key = readString();
                    log("key", key);
                    final String value = (String)readValue(valueType);
                    log("value", value);
                    KeyValuePairData.addKeyValueData(key,new KeyValue(key, value, expiration));
                }
            }
        }
    }
    private Object readValue(int valueType) throws IOException {
        Object res;
        switch (valueType) {
            case STRING_VALUE_TYPE:
                res = readString();
                break;
            default:
                throw new IllegalStateException("unsupported value type: " + valueType);
        }
        return res;
    }
    public void parseMagic() throws IOException {
        byte[] magic1 = new byte[5];
        inputStream.readFully(magic1);
        final String magic = new String(magic1, StandardCharsets.US_ASCII);
        if (!magic.equals("REDIS")) {
            throw new IllegalStateException("invalid magic: " + magic);
        }
    }
    public int parseVersion() throws IOException {
        byte[] magic1 = new byte[4];
        inputStream.readFully(magic1);
        final String version = new String(magic1, StandardCharsets.US_ASCII);
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("invalid version: " + version, exception);
        }
    }

    public void log(Object... content) {
        System.out.println("rdb: " + Arrays.toString(content));
    }

    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(inputStream.readByte());
    }

    public long readUnsignedInteger() throws IOException {
        return Integer.toUnsignedLong(Integer.reverseBytes(inputStream.readInt()));
    }

    public long readUnsignedLong() throws IOException {
        return Long.reverseBytes(inputStream.readLong());
    }

    public int readLength() throws IOException {
        final int first = readUnsignedByte();
        final int encoding = first >> 6;
        final int value = first & 0b0011_1111;
        int res;
        switch (encoding) {
            case LENGTH_6BIT:
                res = value;
                break;
            case LENGTH_14BIT:
                final int second = readUnsignedByte();
                res = (value << 8) | second;
                break;
            /* bad special "number" encoding */
            case LENGTH_SPECIAL:
                res = -(value + 1);
                break;
            default:
                throw new IllegalStateException("unexpected length encoding: " +
                        Integer.toBinaryString(encoding));
        }
        return res;
    }

    public String readString() throws IOException {
        final int length = readLength();
        if (length < 0) {
            /* bad special "number" encoding */
            final int type = (-length) - 1;
            // TODO ByteOrder?
            String res;
            switch (type) {
                case STRING_INTEGER_8BIT:
                    res = String.valueOf(Byte.toUnsignedInt(inputStream.readByte()));
                    break;
                case STRING_INTEGER_16BIT:
                    res = String.valueOf(
                            Short.toUnsignedInt(Short.reverseBytes(inputStream.readShort())));
                    break;
                case STRING_INTEGER_32BIT:
                    res = Integer.toUnsignedString(
                            Integer.reverseBytes(inputStream.readInt()));
                    break;
                default:
                    throw new IllegalArgumentException("unexpected length type: " +
                            Integer.toBinaryString(type));
            };
            return res;
        }
        byte[] magic1 = new byte[length];
        inputStream.readFully(magic1);
        return new String(magic1, StandardCharsets.US_ASCII);
    }

    public void skip(int length) throws IOException { inputStream.skip(length); }

    public static void load(Path path) throws IOException {
        try (final InputStream fileInputStream = Files.newInputStream(path);
             final DataInputStream dataInputStream =
                     new DataInputStream(fileInputStream);) {
            final RdbParser loader = new RdbParser(dataInputStream);
            loader.load();
        }
    }
}
