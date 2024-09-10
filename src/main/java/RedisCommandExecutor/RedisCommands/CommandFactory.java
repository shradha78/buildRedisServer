package RedisCommandExecutor.RedisCommands;

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
        commands.put("DISCARD", new DiscardCommand());
        commands.put("TYPE", new TypeCommand());
        commands.put("XADD", new XaddCommand());
        commands.put("XRANGE", new XRangeCommand());
        commands.put("XREAD", new XReadCommand());
        commands.put("CONFIG", new ConfigGetCommand());
        commands.put("KEYS", new KeysCommand());

    }

    public static IRedisCommandHandler getCommandFromAvailableCommands(String commandName) {
        System.out.println("In Command Factory, Command Received is :  " + commandName + "\n" );
        System.out.println("Checking if we are getting a value " + commands.getOrDefault(commandName , new PingCommand()) + "\n");
        if(commandName.equals("Unknown Command")){
            commandName = "PING";
        }
        if(!commands.containsKey(commandName.toUpperCase())){
            return null;
        }

        return commands.get(commandName.toUpperCase());
    }
}
