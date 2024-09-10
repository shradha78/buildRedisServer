package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;
import RedisCommandExecutor.RedisParser.RedisCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendErrorResponse;


public class ExecCommand implements IRedisCommandHandler {
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        // Check if there is a MULTI command to process
        if (session.getCommandQueue().isEmpty()
                || !session.getCommandQueue().peek().getCommand().equals("MULTI")) {
            sendErrorResponse(outputStream, "EXEC without MULTI");
            return;
        }

        // Process queued commands
        List<String> responses = new ArrayList<>();

        session.getCommandQueue().poll(); // Remove the MULTI command

        int count = 0;

        while (!session.getCommandQueue().isEmpty()) {

            RedisCommand redisCommand = session.getCommandQueue().poll();

            if (redisCommand.getCommand().equals("EXEC")) {
                // Break if EXEC is found (this should be the last command in the queue)
                break;
            }
            // Process each command and capture its response
            ByteArrayOutputStream commandOutputStream = new ByteArrayOutputStream();

            RedisServer.Main.processCommand(redisCommand, commandOutputStream, session);

            responses.add(commandOutputStream.toString("UTF-8")); // Capture the command response

            count++;
        }

        // Format the responses as an array
        StringBuilder responseArray = new StringBuilder("*" + count + "\r\n");

        for (String response : responses) {
            responseArray.append(response);
        }

        // Send the array response
        outputStream.write(responseArray.toString().getBytes());
    }


}
