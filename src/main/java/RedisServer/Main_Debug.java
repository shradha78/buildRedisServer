//package RedisServer;
//
//import RedisCommandExecutor.CommandFactory;
//import RedisCommandExecutor.IRedisCommandHandler;
//import RedisCommandExecutor.RedisStreams;
//
//import java.io.*;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.sql.Timestamp;
//import java.util.*;
//import java.util.concurrent.*;
//
//import static RedisCommandExecutor.IncrCommand.sendErrorResponse;
//
//public class Main {
//    private static RedisCommandParser redisCommandParser;
//    private static RedisProtocolParser redisProtocolParser;
//    public static HashMap<String, KeyValue> storeKeyValue;
//    public static Map<String, RedisStreams> streams;
//
//
//    public static void main(String[] args) throws IOException {
//        // You can use print statements as follows for debugging, they'll be visible when running tests.
//        System.out.println("Logs from your program will appear here!");
//
//        new Thread(() -> {
//            try {
//                initializeServer();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//
//        InetAddress host = InetAddress.getLocalHost();
//
//        Socket c1 = new Socket(host.getHostName(), 6379);
//        Socket c2 = new Socket(host.getHostName(), 6379);
//
//
//        System.out.println("HERE>>>>>>>");
//
//        OutputStreamWriter c1oos = new OutputStreamWriter(c1.getOutputStream());
//        OutputStreamWriter c2oos = new OutputStreamWriter(c2.getOutputStream());
//
//        InputStreamReader c1ois = new InputStreamReader(c1.getInputStream());
//        InputStreamReader c2ois = new InputStreamReader(c2.getInputStream());
//
//        c1oos.write("$4\r\nXADD\r\n$3\r\nabc\r\n$3\r\n0-1\r\n$5\r\nvalue\r\n$3\r\n123\r\n");
//
//        c2oos.write("$5\r\nXREAD\r\n$5\r\nSTREAMS\r\n$3\r\nabc\r\n$3\r\n0-1\r\n");
//
////        try {
////            System.out.println(c2ois.readObject());
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        }
//
//        System.out.println("IS IT HERE????....");
//        while (true) {}
//    }
//
//    private static void initializeServer() throws IOException {
//        Socket clientSocket = null;
//        int port = 6379;
//
//        ServerSocket serverSocket = new ServerSocket(port);
//        // Since the tester restarts your program quite often, setting SO_REUSEADDR
//        // ensures that we don't run into 'Address already in use' errors
//        serverSocket.setReuseAddress(true);
//
//        listenToPort(serverSocket, port);
//    }
//
//    private static void listenToPort(ServerSocket serverSocket, int port) {
//        redisCommandParser = new RedisCommandParser();
//        redisProtocolParser = new RedisProtocolParser();
//        storeKeyValue = new HashMap<>();
//        streams = new ConcurrentHashMap<>();
//
//        Socket clientSocket = null;
//        try {
//
//
//            while (true) {
//                // Wait for connection from client.
//                clientSocket = serverSocket.accept();
//                final Socket finalClientSocket = clientSocket;
//                System.out.println("CLIENT CONNECTION:::" + finalClientSocket);
//                //Socket finalClientSocket = clientSocket;
//                new Thread(() -> {
//                    try {
//                        System.out.printf("Connected with Client : " + finalClientSocket.getPort() + "\n");
//                        ClientSession session = new ClientSession();
////                        handlingClientCommands(finalClientSocket, session);
//                        handlingClientCommands(finalClientSocket, session);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//            }
//        } catch (IOException e) {
//            System.out.println("IOException: " + e.getMessage());
//        } finally {
//            try {
//                if (clientSocket != null) {
//                    clientSocket.close();
//                }
//            } catch (IOException e) {
//                System.out.println("IOException: " + e.getMessage());
//            }
//        }
//    }
//
//    private static void handlingClientCommands(Socket clientSocket, ClientSession session) throws IOException {
//
//        try {
//            System.out.println("TRYING.... for " + clientSocket);
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(clientSocket.getInputStream()));
//            OutputStream outputStream = clientSocket.getOutputStream();
//            while (true) {
//                try {
//                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
//                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
//                    queueCommands(command, session);
//                    processCommand(command, outputStream, session);//based on commands, it will process output
//                    //Debug
////                RedisCommand command = new RedisCommand("XADD", new ArrayList<>(Arrays.asList("shradha", "0-1", "temperature", "36")));
////                processCommand(command, outputStream, session);//based on commands, it will process output
//////                    command = new RedisCommand("XREAD", new ArrayList<>(Arrays.asList("BLOCK", "1000", "streams", "shradha", "0-1")));
//////                    processCommand(command, outputStream, session);//based on commands, it will process output
////                command = new RedisCommand("XADD", new ArrayList<>(Arrays.asList("shradha", "0-2", "temperature", "40")));
////                processCommand(command, outputStream, session);//based on commands, it will process output
////                command = new RedisCommand("XREAD", new ArrayList<>(Arrays.asList("streams", "shradha", "0-1")));
////                processCommand(command, outputStream, session);//br
//                } catch (IOException e) {
//                    outputStream.write("-ERR invalid input\r\n".getBytes());
////                    break;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
////            clientSocket.close();
//        }
//    }
//
//    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {
//        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());
//        System.out.printf("Checking value for redis command handler " + redisCommandHandler.getClass().getName() + "\n");
//        if (redisCommandHandler != null) {
//            redisCommandHandler.execute(command.getListOfActions(), outputStream, session);
//        } else {
//            sendErrorResponse(outputStream, " Unknown Command");
//        }
//    }
//
//    private static void queueCommands(RedisCommand command, ClientSession session) {
//        Queue<RedisCommand> queueOfCommandsForMultiAndExec = session.getCommandQueue();
//
//        if (!queueOfCommandsForMultiAndExec.isEmpty()) {
//            while (!queueOfCommandsForMultiAndExec.isEmpty() && !queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")) {
//                queueOfCommandsForMultiAndExec.poll();
//            }
//        }
//        queueOfCommandsForMultiAndExec.add(command);
//    }
//
//}
