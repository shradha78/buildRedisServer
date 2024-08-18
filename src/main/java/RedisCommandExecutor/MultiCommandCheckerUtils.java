package RedisCommandExecutor;

import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;

public class MultiCommandCheckerUtils {
    public static boolean checkForMultiCommandInQueue(OutputStream outputStream) throws IOException {
        if(!Main.queueOfCommandsForMultiAndExec.isEmpty()
        && Main.queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")){
            sendQueuedAsResponse(outputStream, "Queued");
            return true;
        }
        return false;
    }

    private static void sendQueuedAsResponse(OutputStream outputStream,String value) throws IOException {
        String responseQueued = "+" + value + "\r\n";
        outputStream.write(responseQueued.getBytes());
    }
}
