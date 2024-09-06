package RedisResponses;

import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ShortParsedResponses {

    public static void sendBulkStringResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        System.out.printf("In SENDING BULK STRING RESPONSE: "+ value +"\n");
        if(value.equals("")){
            System.out.printf("In Sending NULL RESPONSE: \n");
            String responseBulkNullString = "$-1\r\n";
            outputStream.write(responseBulkNullString.getBytes());
            outputStream.flush();
            return;
        }
        String responseBulkString = "$" + value.length() + "\r\n" + value + "\r\n";
        System.out.println(debugPrintStatement + responseBulkString + "\n");
        outputStream.write(responseBulkString.getBytes());
        outputStream.flush();
    }

    public static void sendErrorResponse(OutputStream outputStream, String message) throws IOException {
        outputStream.write(("-ERR " +  message + "\r\n").getBytes());
    }

    public static void sendIntegerResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        String responseInteger = ":" + value + "\r\n";
        System.out.println(debugPrintStatement + responseInteger + "\n");
        outputStream.write(responseInteger.getBytes());
    }

    public static void sendEmptyArrayResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        String responseArray = "*" + value + "\r\n";
        System.out.println(debugPrintStatement + responseArray + "\n");
        outputStream.write(responseArray.getBytes());
    }

    public static void sendSimpleOKResponse(OutputStream outputStream) throws IOException {
        outputStream.write("+OK\r\n".getBytes());
    }

    public static void sendSimpleResponse(OutputStream outputStream, String string) throws IOException {
        outputStream.write(("+"+string+"\r\n").getBytes());
    }


}
