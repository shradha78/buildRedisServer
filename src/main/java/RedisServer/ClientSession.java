package RedisServer;

import RedisCommandExecutor.RedisParser.RedisCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ClientSession {
    private Queue<RedisCommand> commandQueue;
    private Socket socket;
    private OutputStream outputStream;

    public ClientSession(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.commandQueue = new LinkedList<>();
    }

    // Method to send commands to this replica
    public void sendCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
        outputStream.flush();
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public Queue<RedisCommand> getCommandQueue() {
        return commandQueue;
    }
}
