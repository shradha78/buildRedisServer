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
        ReplicaManager.addReplica(session);
        try {
            while (true) {
                String element = MasterWriteCommands.getWriteCommand();
                outputStream.write(element.getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}