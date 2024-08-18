package RedisCommandExecutor;

import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class ExecCommand implements IRedisCommandHandler {
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (!Main.queueOfCommandsForMultiAndExec.isEmpty() && !Main.queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")) {
            sendErrorResponse(outputStream, "EXEC without MULTI");
        } else {
            if(!Main.queueOfCommandsForMultiAndExec.isEmpty() &&Main.queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI") ){
                Main.queueOfCommandsForMultiAndExec.poll();
            }
            sendEmptyArrayResponse(outputStream,"0", "Empty array response is : ");
        }
    }

    private static void sendEmptyArrayResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        String responseArray = "*" + value + "\r\n";
        System.out.printf(debugPrintStatement + responseArray + "\n");
        outputStream.write(responseArray.getBytes());
    }
}
