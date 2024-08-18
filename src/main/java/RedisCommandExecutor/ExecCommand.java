package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.Main;
import RedisServer.RedisCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class ExecCommand implements IRedisCommandHandler {
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        if (!session.getCommandQueue().isEmpty() && !session.getCommandQueue().peek().getCommand().equals("MULTI")) {
            sendErrorResponse(outputStream, "EXEC without MULTI");
        } else {
            if(!session.getCommandQueue().isEmpty() && session.getCommandQueue().peek().getCommand().equals("MULTI") ){
                session.getCommandQueue().poll();
                int count = 1;
                while(!session.getCommandQueue().isEmpty()){
                    RedisCommand redisCommand = session.getCommandQueue().poll();
                    if(redisCommand.getCommand().equals("EXEC")){
                        return;
                    }
                    RedisServer.Main.processCommand(redisCommand,outputStream,session);
                }
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
