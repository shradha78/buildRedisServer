package RedisCommandExecutor.RedisCommandsParser;

import java.util.List;

public class RedisCommand {
    private String command;
    private List<String> listOfCommandArguments;

    public RedisCommand(String command, List<String> listOfCommandArguments) {
        this.command = command;
        this.listOfCommandArguments = listOfCommandArguments;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getListOfCommandArguments() {
        return listOfCommandArguments;
    }

}
