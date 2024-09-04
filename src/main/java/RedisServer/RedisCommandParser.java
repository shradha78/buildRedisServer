package RedisServer;

import RedisServer.RedisCommand;

import java.util.ArrayList;
import java.util.List;

public class RedisCommandParser {
    public RedisCommand parseCommand(List<String> mainMessageParts){
        System.out.println("In command Parser : \n");
        if(mainMessageParts == null || mainMessageParts.isEmpty()){
            System.out.println("No Message parsed from RESP");
            return new RedisCommand("Unknown Command",null);
        }
        String commandName = mainMessageParts.get(0).toUpperCase();
        System.out.println("Command is : " + commandName + "\n");
        List<String> messageArgs = mainMessageParts.size() > 1 ?
                mainMessageParts.subList(1, mainMessageParts.size()) : new ArrayList<>();//all remaining arguments from parsed list
        for(int i = 0; i < messageArgs.size();i++) {
            System.out.println("Command arguments are : " + messageArgs.get(i) + " \n");
        }
        return new RedisCommand(commandName,messageArgs);
    }
}
