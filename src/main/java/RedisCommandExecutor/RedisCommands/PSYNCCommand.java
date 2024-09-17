package RedisCommandExecutor.RedisCommands;

import DataUtils.MasterWriteCommands;
import RedisReplication.RedisServerConfig;
import RedisReplication.ReplicaManager;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HexFormat;
import java.util.List;


public class PSYNCCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("Received Psync command from replica");
        StringBuilder response = new StringBuilder();
        RedisServerConfig redisServerConfig = RedisServerConfig.getInstance();
        response.append("+FULLRESYNC ")
                .append(redisServerConfig.getReplicationId()).append(" ")
                .append(redisServerConfig.getReplicationOffset()).append("\r\n");

        outputStream.write(response.toString().getBytes());

        byte[] contents = HexFormat.of().parseHex(
                "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2");

        outputStream.write (("$" + contents.length + "\r\n").getBytes());

        outputStream.write(contents);

        System.out.println("Adding current session to replicas list");
        //session.setReplica(true);
        ReplicaManager.addReplica(session);

        System.out.println("Added current session to replicas list");

        // After the handshake, propagate existing commands
        List<String> commands = MasterWriteCommands.getWriteCommands();
        for (String command : commands) {
            System.out.println("Commands for write: " + command);
            outputStream.write(command.getBytes());
            outputStream.flush();
        }

    }
}
