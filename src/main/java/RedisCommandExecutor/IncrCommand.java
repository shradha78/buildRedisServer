package RedisCommandExecutor;

import RedisServer.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class IncrCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        String keyIncr = args.get(0);
        KeyValue keyValueIncr = RedisServer.Main.storeKeyValue.containsKey(keyIncr) ?
                                    RedisServer.Main.storeKeyValue.get(keyIncr) : new KeyValue("0",0);
        String value = keyValueIncr.getValue();
        int valueIncr = 0;
        try {
            valueIncr = Integer.parseInt(value);
            valueIncr += 1;
        }catch(NumberFormatException numberFormatException){
            sendErrorResponse(outputStream, "value is not an integer or out of range");
            return;
        }
        RedisServer.Main.storeKeyValue.put(keyIncr, (new RedisServer.KeyValue(String.valueOf(valueIncr),0)));
        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream)){
            return;
        }
        sendIntegerResponse(outputStream, String.valueOf(valueIncr),"Integer value is ");
    }

    public static void sendErrorResponse(OutputStream outputStream, String message) throws IOException {
        outputStream.write(("-ERR " +  message + "\r\n").getBytes());
    }

    private static void sendIntegerResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        String responseInteger = ":" + value + "\r\n";
        System.out.printf(debugPrintStatement + responseInteger + "\n");
        outputStream.write(responseInteger.getBytes());
    }
}
