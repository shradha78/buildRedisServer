package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;
import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendErrorResponse;
import static RedisResponses.ShortParsedResponses.sendIntegerResponse;


public class IncrCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {

        System.out.println("In Incr class \n");

        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }

        String keyIncr = args.get(0);

        KeyValue keyValueIncr = DataUtils.KeyValuePairData.containsKey(keyIncr) ?
                                    DataUtils.KeyValuePairData.getSpecificKeyDetails(keyIncr) : new KeyValue(keyIncr,"0",0);
        String value = keyValueIncr.getValue();
        int valueIncr = 0;

        try {
            valueIncr = Integer.parseInt(value);
            valueIncr += 1;
        }catch(NumberFormatException numberFormatException){
            sendErrorResponse(outputStream, "value is not an integer or out of range");
            return;
        }

        DataUtils.KeyValuePairData.addKeyValueData(keyIncr, (new KeyValue(keyIncr,String.valueOf(valueIncr),0)));

        sendIntegerResponse(outputStream, String.valueOf(valueIncr),"Integer value is ");
    }


}
