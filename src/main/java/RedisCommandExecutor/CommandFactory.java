package RedisCommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, IRedisCommandHandler> commands;

   static{ //using static block instead of constructor since map is static and needs to intialised in the same instead of a constructor
        commands = new HashMap<>();
        commands.put("ECHO", new EchoCommand());
        commands.put("GET", new GETCommand());
        commands.put("SET", new SETCommand());
        commands.put("PING", new PingCommand());
        commands.put("INCR", new IncrCommand());
        commands.put("MULTI", new MultiCommand());
        commands.put("EXEC", new ExecCommand());

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
