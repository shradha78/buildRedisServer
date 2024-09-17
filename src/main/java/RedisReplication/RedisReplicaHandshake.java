package RedisReplication;

import RedisServer.ClientHandler;
import RedisServer.ClientSession;

import java.io.*;
import java.net.Socket;

public class RedisReplicaHandshake {
    private final Socket masterSocket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final int replicaPort;

    public RedisReplicaHandshake(Socket masterSocket, int replicaPort) throws IOException {
        this.masterSocket = masterSocket;
        this.outputStream = masterSocket.getOutputStream();
        this.inputStream = masterSocket.getInputStream();
        this.replicaPort = replicaPort;
    }

    public void startHandshake() throws IOException {
        sendPing();
        if (receiveResponse().equals("+PONG")) {
            sendReplconfListeningPort();
            if (receiveResponse().equals("+OK")) {
                sendReplconfCapaPsync2();
                if (receiveResponse().equals("+OK")) {
                    sendPsync();
                    new Thread(() -> {
                        try {
                            System.out.println("Starting ClientHandler thread.");
                            System.out.println("Master socket is : "+ masterSocket.getInetAddress() + ":" + masterSocket.getPort());
                            Socket replicaSocket = new Socket(masterSocket.getInetAddress() ,replicaPort);
                            new Thread(new ClientHandler(replicaSocket,new ClientSession(replicaSocket,true))).start();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                    // Ignoring response for now as instructed
                    receiveResponse();// Expect +FULLRESYNC <REPL_ID> 0
                    System.out.println("After Psync, receiving response " + receiveResponse());
                    //new Thread(new ClientSession(masterSocket,true)).start();
                } else {
                    System.out.println("Failed on REPLCONF capa psync2");
                }
            } else {
                System.out.println("Failed on REPLCONF listening-port");
            }
        } else {
            System.out.println("Failed on PING");
        }
    }

    private void sendPing() throws IOException {
        String pingMessage = "*1\r\n$4\r\nPING\r\n";
        outputStream.write(pingMessage.getBytes());
        outputStream.flush();
        System.out.println("PING sent to master");
    }

    private void sendReplconfListeningPort() throws IOException {
        String replconfListeningPortMessage = "*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n" + replicaPort + "\r\n";
        outputStream.write(replconfListeningPortMessage.getBytes());
        outputStream.flush();
        System.out.println("REPLCONF listening-port " + replicaPort + " sent to master");
    }

    private void sendReplconfCapaPsync2() throws IOException {
        String replconfCapaPsync2Message = "*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n";
        outputStream.write(replconfCapaPsync2Message.getBytes());
        outputStream.flush();
        System.out.println("REPLCONF capa psync2 sent to master");
    }

    private void  sendPsync() throws IOException {
        String psyncMessage = "*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n";
        outputStream.write(psyncMessage.getBytes());
        outputStream.flush();
        System.out.println("PSYNC sent to master");
    }

    private String receiveResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String response = reader.readLine();
        System.out.println("Response from master: " + response);
        return response;
    }
}

