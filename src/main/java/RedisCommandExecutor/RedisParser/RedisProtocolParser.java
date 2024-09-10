package RedisCommandExecutor.RedisParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisProtocolParser {

    public List<String> parseRESPMessage(BufferedReader br, long currentTimestamp) throws IOException {
        List<String> mainInputLines = new ArrayList<>();
        System.out.println("IN parsing RESP Message");

        String line = br.readLine();
        currentTimestamp = System.currentTimeMillis();
        System.out.println("******* Next Line ");

        if(line == null || !line.startsWith("*") || line.isEmpty()){
            System.out.println("******* In null Line read ");
            //throw new IOException("Invalid RESP message");
            return new ArrayList<>();
        }
//        System.out.println("Client's command :: " + line);
//        int numberOfArguments = Integer.parseInt(line.substring(1,2));//for debug through local

        int numberOfArguments = Integer.parseInt(line.substring(1));//In array data type after * there is number of elements

        for(int i = 0; i < numberOfArguments;i++){
            br.readLine(); //Skipping length arguments
            String message = br.readLine();
            mainInputLines.add(message); //will contain command and message string
        }

        return mainInputLines;
    }
}
