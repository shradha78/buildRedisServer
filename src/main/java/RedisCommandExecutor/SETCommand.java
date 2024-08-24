package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SETCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.printf("In class SETCommand ");
        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }
        String setKey = args.get(0);
        String setValue = args.get(1);
        System.out.printf("Key to set : " + setKey);
        System.out.printf("Value to set : " + setValue);
        long expiryTime = 0;
        if (args.size() > 2 && args.get(2).equalsIgnoreCase("PX")) {
            int seconds = Integer.parseInt(args.get(3));
            expiryTime = System.currentTimeMillis() + seconds; //storing future expiry time
            System.out.printf("Expiry time is  " + expiryTime + "\n");
        }
        RedisServer.Main.storeKeyValue.put(setKey,new RedisServer.KeyValue(setKey,setValue, expiryTime));
        sendSimpleOKResponse(outputStream);
    }
    public static void sendSimpleOKResponse(OutputStream outputStream) throws IOException {
        outputStream.write("+OK\r\n".getBytes());
    }
}
