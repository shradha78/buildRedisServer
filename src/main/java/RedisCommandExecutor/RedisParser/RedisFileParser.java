package RedisCommandExecutor.RedisParser;

import DataUtils.KeyValue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class RedisFileParser {

    public static void parseRDBFile(String fileName) {

        try {
            FileInputStream fis = new FileInputStream(fileName);
            byte[] header = new byte[9];//The first 9 bytes are the magic string and version number (e.g., "REDIS0011").
            fis.read(header);

            String headerString = new String(header, StandardCharsets.UTF_8);

            if(!headerString.equals("REDIS0011")){
                throw new IOException("Invalid RDB file format");
            }

            parseDatabaseSectionInRDBFile(fis);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    FE: Marks the start of the database subsection.
    FB: Indicates the size of the hash table (key-value and expiration table sizes).
    00: Represents a key-value pair. This is followed by the key and value.
     */
    private static void parseDatabaseSectionInRDBFile(FileInputStream fis) {
        int byteRead;

        while(true){
            try {
                if (!((byteRead = fis.read()) != -1)) {
                    if(byteRead == 0XFE){
                        System.out.println("Database section found");
                    }else if(byteRead == 0X00){
                        String key = readString(fis);
                        String value = readString(fis);

                        // Store the key-value pair
                        System.out.println("Key: " + key + " Value: " + value);
                        DataUtils.KeyValuePairData.addKeyValueData(key,new KeyValue(key,value,0));
                    }else if (byteRead == 0xFF) {
                        System.out.println("End of RDB file.");
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
    public static String readString(InputStream is) throws IOException {
        int firstByte = is.read();

        // Check if first two bits are 00 (6-bit length encoding)
        if ((firstByte & 0xC0) == 0x00) {
            int length = firstByte & 0x3F;  // Remaining 6 bits
            return readBytesAsString(is, length);
        }
        // Check if first two bits are 01 (14-bit length encoding)
        else if ((firstByte & 0xC0) == 0x40) {
            int secondByte = is.read();
            int length = ((firstByte & 0x3F) << 8) | secondByte;  // 14-bit length
            return readBytesAsString(is, length);
        }
        // Check if first two bits are 10 (32-bit length encoding)
        else if ((firstByte & 0xC0) == 0x80) {
            byte[] lengthBytes = new byte[4];
            int bytesRead = is.read(lengthBytes);
            if (bytesRead != 4) {
                throw new IOException("Invalid number of bytes read for 32-bit length");
            }

            // Handle little-endian byte order
            int length = ByteBuffer.wrap(lengthBytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
            return readBytesAsString(is, length);
        }
        // Special encoding cases (11)
        else if ((firstByte & 0xC0) == 0xC0) {
            int specialEncodingType = firstByte & 0x3F;
            return handleSpecialEncoding(specialEncodingType, is);
        }
        else {
            throw new IOException("Unsupported string encoding");
        }
    }

    private static String readBytesAsString(InputStream is, int length) throws IOException {
        byte[] strBytes = new byte[length];
        int bytesRead = is.read(strBytes);
        if (bytesRead != length) {
            throw new IOException("Invalid number of bytes read for string");
        }
        return new String(strBytes);
    }

    private static String handleSpecialEncoding(int encodingType, InputStream is) throws IOException {
        if (encodingType == 0x00) {
            int value = is.read(); // 8-bit integer
            return Integer.toString(value);
        } else if (encodingType == 0x01) {
            byte[] bytes = new byte[2];
            int bytesRead = is.read(bytes);
            if (bytesRead != 2) {
                throw new IOException("Invalid number of bytes read for 16-bit integer");
            }
            int value = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
            return Integer.toString(value);
        } else if (encodingType == 0x02) {
            byte[] bytes = new byte[4];
            int bytesRead = is.read(bytes);
            if (bytesRead != 4) {
                throw new IOException("Invalid number of bytes read for 32-bit integer");
            }
            int value = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
            return Integer.toString(value);
        } else {
            throw new IOException("Unsupported special encoding type");
        }
    }
}
