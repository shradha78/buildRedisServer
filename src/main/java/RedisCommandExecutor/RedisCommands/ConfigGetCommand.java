package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ConfigGetCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("Executing ConfigGetCommand");

        String configName = args.get(1);

        String configValue = DataUtils.ConfigurationData.getConfigDetails(configName);

        RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,configName,"The name of Config asked in Input");
        RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,configValue,"The value of Config asked in Input");


    }
}
