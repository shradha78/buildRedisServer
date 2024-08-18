package RedisCommandExecutor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class EchoCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> commandArgs, OutputStream outputStream) throws IOException {
        if (commandArgs.size() != 1) {
            outputStream.write("-ERR wrong number of arguments for 'ECHO' command\r\n".getBytes());
        } else {
            if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream)){
                return;
            }
            String response = commandArgs.get(0);
            sendBulkStringResponse(outputStream, response, "Response Bulk String is : ");
        }
    }

    public static void sendBulkStringResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        if(value.equals("")){
            String responseBulkNullString = "$-1\r\n";
            outputStream.write(responseBulkNullString.getBytes());
            return;
        }
        String responseBulkString = "$" + value.length() + "\r\n" + value + "\r\n";
        System.out.printf(debugPrintStatement + responseBulkString + "\n");
        outputStream.write(responseBulkString.getBytes());
    }
}
