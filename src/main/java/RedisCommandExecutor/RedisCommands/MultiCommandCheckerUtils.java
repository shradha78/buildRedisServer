package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;

public class MultiCommandCheckerUtils {
    public static boolean checkForMultiCommandInQueue(OutputStream outputStream, ClientSession session) throws IOException {
        if (!session.getCommandQueue().isEmpty() &&
                session.getCommandQueue().peek().getCommand().equals("MULTI")) {
            sendQueuedAsResponse(outputStream, "QUEUED");
            return true;
        }

        return false;
    }

    private static void sendQueuedAsResponse(OutputStream outputStream, String value) throws IOException {
        String responseQueued = "+" + value + "\r\n";
        outputStream.write(responseQueued.getBytes());
    }
}
