package RedisServer;

import RedisCommandExecutor.RedisCommandsParser.RedisCommand;

import java.util.LinkedList;
import java.util.Queue;

public class ClientSession {
    private Queue<RedisCommand> commandQueue;

    public ClientSession() {
        this.commandQueue = new LinkedList<>();
    }

    public Queue<RedisCommand> getCommandQueue() {
        return commandQueue;
    }
}
