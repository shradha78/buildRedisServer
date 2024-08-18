package RedisCommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, IRedisCommandHandler> commands;

    public CommandFactory() {
        commands = new HashMap<>();
        commands.put("ECHO", new EchoCommand());
        commands.put("GET", new GETCommand());
        commands.put("SET", new SETCommand());
        commands.put("PING", new PingCommand());
        commands.put("INCR", new IncrCommand());

    }

    public static IRedisCommandHandler getCommandFromAvailableCommands(String commandName) {
        System.out.printf("In Command Factory, Command Received is :  " + commandName + "\n" );
        System.out.printf("Checking if we are getting a value " + commands.getOrDefault(commandName , new SETCommand()) + "\n");
        if(!commands.containsKey(commandName.toUpperCase())){
            return null;
        }

        return commands.get(commandName.toUpperCase());
    }
}
