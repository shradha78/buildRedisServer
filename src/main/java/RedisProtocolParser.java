import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisProtocolParser {
    public List<String> parseRESPTypeArrayMessage(BufferedReader br) throws IOException {
        List<String> mainMessageParts = new ArrayList<>();

        String line = br.readLine();
        if(line == null || !line.startsWith("*")){
            System.out.printf("Message in Invalid Block " + line +"\n");
            throw new IOException("Invalid RESP message");
        }
        int numberOfArguments = Integer.parseInt(line.substring(1));//In array data type after * there is number of elements
        for(int i = 0; i < numberOfArguments;i++){
            br.readLine(); //Skipping length arguments
            String message = br.readLine();
            System.out.printf("The Argument are : " + message + "\n");
            mainMessageParts.add(message); //will contain command and message string
        }
        return mainMessageParts;
    }
}
