package RedisCommandExecutor.RedisParser;

import java.util.ArrayList;
import java.util.List;

public class RedisCommandParser {

    public RedisCommand parseCommand(List<String> mainInputLines, long timestamp){

//        System.out.println("In command Parser : \n");

        if(mainInputLines == null || mainInputLines.isEmpty()){
//            System.out.println("No Message parsed from RESP");
            return new RedisCommand("Unknown Command",null, timestamp);
        }

        String commandName = mainInputLines.get(0).toUpperCase();

//        System.out.println("Command is : " + commandName + "\n");

        List<String> messageArgs = mainInputLines.size() > 1 ?
                mainInputLines.subList(1, mainInputLines.size()) : new ArrayList<>();//all remaining arguments from parsed list

        //for debugging
//        for(int i = 0; i < messageArgs.size();i++) {
//            System.out.println("Command arguments are : " + messageArgs.get(i) );
//        }

        return new RedisCommand(commandName,messageArgs,timestamp);
    }
}
