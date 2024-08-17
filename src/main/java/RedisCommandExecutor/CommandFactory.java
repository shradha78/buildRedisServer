package RedisCommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, IRedisCommandHandler> commands = new HashMap<>();

    public CommandFactory() {
        commands.put("ECHO", new EchoCommand());
        commands.put("GET", new GETCommand());
        commands.put("SET", new SETCommand());
        commands.put("PING", new SETCommand());
        commands.put("INCR", new SETCommand());

    }

    public static IRedisCommandHandler getCommand(String commandName) {
        if(!commands.containsKey(commandName.toUpperCase())){
            return null;
        }
        return commands.get(commandName.toUpperCase());
    }
}
