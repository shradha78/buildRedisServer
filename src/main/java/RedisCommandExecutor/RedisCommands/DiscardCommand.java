package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendErrorResponse;
import static RedisResponses.ShortParsedResponses.sendSimpleOKResponse;

public class DiscardCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        // Check if there is a MULTI command to process
        if (session.getCommandQueue().isEmpty()
                || !session.getCommandQueue().peek().getCommand().equals("MULTI")) {
            sendErrorResponse(outputStream, "DISCARD without MULTI");
        }else{
            session.getCommandQueue().clear();
            sendSimpleOKResponse(outputStream);
        }

    }
}
