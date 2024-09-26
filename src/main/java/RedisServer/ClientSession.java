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
    private boolean isReplica;

    public ClientSession(Socket socket,boolean isReplica) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.commandQueue = new LinkedList<>();
        this.isReplica = isReplica;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isReplica() {
        return isReplica;
    }

    public ClientSession setReplica(boolean replica) {
        isReplica = replica;
        return this;
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
