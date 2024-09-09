package RedisCommandExecutor.RedisCommandsParser;

import java.util.List;

public class RedisCommand {
    private String command;
    private List<String> listOfCommandArguments;
    private long timestamp;

    public RedisCommand(String command, List<String> listOfCommandArguments, long timestamp) {
        this.command = command;
        this.listOfCommandArguments = listOfCommandArguments;
        this.timestamp = timestamp;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getListOfCommandArguments() {
        return listOfCommandArguments;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
