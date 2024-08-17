package RedisServer;

import java.util.List;

public class RedisCommand {
    private String command;
    private List<String> listOfActions;

    public RedisCommand(String command, List<String> listOfActions) {
        this.command = command;
        this.listOfActions = listOfActions;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getListOfActions() {
        return listOfActions;
    }

    public void setListOfActions(List<String> listOfActions) {
        this.listOfActions = listOfActions;
    }
}
