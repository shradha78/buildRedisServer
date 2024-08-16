import java.util.List;

public class RedisCommandParser {
    public RedisCommand parseCommand(List<String> mainMessageParts){
        if(mainMessageParts == null || mainMessageParts.isEmpty()){
            System.out.printf("No Message parsed from RESP");
            return new RedisCommand("Unknown Command",null);
        }
        String commandName = mainMessageParts.get(0);
        List<String> messageArgs = mainMessageParts.subList(1, mainMessageParts.size());//all remaining arguments from parsed list

        return new RedisCommand(commandName,messageArgs);
    }
}
