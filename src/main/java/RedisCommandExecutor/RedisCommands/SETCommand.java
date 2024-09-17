package RedisCommandExecutor.RedisCommands;

import DataUtils.MasterWriteCommands;
import RedisServer.ClientSession;
import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendSimpleOKResponse;


public class SETCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("In class SETCommand ");

        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }

        String setKey = args.get(0);

        String setValue = args.get(1);

        String respArray = "";
        respArray += "*3\r\n$3\r\nSET\r\n"+ "$" + setKey.length() + "\r\n" + setKey + "\r\n" + "$" + setValue.length() + "\r\n" + setValue + "\r\n";
        MasterWriteCommands.addWriteCommands(respArray);


        System.out.println("Key to set : " + setKey);
        System.out.println("Value to set : " + setValue);

        long expiryTime = 0;

        if (args.size() > 2 && args.get(2).equalsIgnoreCase("PX")) {
            int seconds = Integer.parseInt(args.get(3));
            expiryTime = System.currentTimeMillis() + seconds; //storing future expiry time
            System.out.println("Expiry time is  " + expiryTime + "\n");
        }

        DataUtils.KeyValuePairData.addKeyValueData(setKey,new KeyValue(setKey,setValue, expiryTime));

        sendSimpleOKResponse(outputStream);
    }

}
