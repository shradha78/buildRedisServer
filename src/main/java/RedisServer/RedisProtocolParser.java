package RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisProtocolParser {
    public List<String> parseRESPMessage(BufferedReader br) throws IOException {
        List<String> mainMessageParts = new ArrayList<>();
        System.out.println("IN parsing RESP Message\n");
        String line = br.readLine();
        if(line == null || !line.startsWith("*")){
            throw new IOException("Invalid RESP message");
        }
//        System.out.println("Client's command :: " + line);
//        int numberOfArguments = Integer.parseInt(line.substring(1,2));//for debug through local
        int numberOfArguments = Integer.parseInt(line.substring(1));//In array data type after * there is number of elements
        for(int i = 0; i < numberOfArguments;i++){
            br.readLine(); //Skipping length arguments
            String message = br.readLine();
            mainMessageParts.add(message); //will contain command and message string
        }

        return mainMessageParts;
    }
}
