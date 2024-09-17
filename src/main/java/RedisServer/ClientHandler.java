package RedisServer;

import RedisCommandExecutor.RedisCommands.CommandFactory;
import RedisCommandExecutor.RedisCommands.IRedisCommandHandler;
import RedisCommandExecutor.RedisParser.RedisCommand;
import RedisCommandExecutor.RedisParser.RedisCommandParser;
import RedisCommandExecutor.RedisParser.RedisProtocolParser;

import java.io.*;
import java.net.Socket;
import java.util.List;

import static DataUtils.CommandsQueue.queueCommands;
import static RedisResponses.ShortParsedResponses.sendErrorResponse;

public class ClientHandler implements Runnable {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;
    private Socket socket;
    private OutputStream outputStream;
    private ClientSession clientSession;

    public ClientHandler(Socket socket,ClientSession clientSession) {
        this.socket = socket;
        this.clientSession = clientSession;
    }

    void handlingClientCommands(ClientSession session) throws IOException {

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            this.outputStream = socket.getOutputStream();

            PrintWriter printWriter = new PrintWriter(outputStream, true);

           while (true) {
                try {
                    long currentTime = 0;

                    while (br.ready()) {
                        //parsing input from the client
                        List<String> parsedInput = redisProtocolParser.parseRESPMessage(br, currentTime);

                        //After parsing the input from client, separating command and its arguments
                        RedisCommand command = redisCommandParser.parseCommand(parsedInput, currentTime);

                        //maintaining queue of commands, specifically for MULTI and EXEC Command
                        queueCommands(command, session);

                        try {
                            processCommand(command, outputStream, session);//based on commands, it will process output
                        } catch (IOException e) {
                            System.out.println("Error while processing command: " + e.getMessage());
                            break;
                        }
                    }

                } catch (IOException e) {
                    outputStream.write("-ERR invalid input\r\n".getBytes());
                    break;
                }
           }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException while handling client commands: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    System.out.println("Closing client socket.");
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException while closing socket: " + e.getMessage());
            }
        }
    }

    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("Processing command: " + command.getCommand());
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());

//        System.out.printf("Checking value for redis command handler %s\n", redisCommandHandler != null ? redisCommandHandler.getClass().getName() : "null");
        System.out.println("Is session a replica in processing ? " + session.isReplica());
        if (redisCommandHandler != null) {
            if (session.isReplica()) {
                System.out.println("Session is a replica");
                // For replica: execute command and update internal state, no response needed
                redisCommandHandler.execute(command.getListOfCommandArguments(), session.getOutputStream(), session);
            } else {
                System.out.println("Master session");
                // For clients: execute command and send response
                redisCommandHandler.execute(command.getListOfCommandArguments(), outputStream, session);
            }
        } else {
            if (!session.isReplica()) {
                sendErrorResponse(outputStream, " Unknown Command");
            }
        }

    }

    @Override
    public void run() {
        try {
            System.out.println("Running on Thread : "+ Thread.currentThread().getName());
            handlingClientCommands(clientSession);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
