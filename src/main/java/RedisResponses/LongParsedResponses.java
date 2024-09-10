package RedisResponses;

import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class LongParsedResponses {

    public static void sendArrayRESPresponseForKeyValues(OutputStream outputStream, Map<String, KeyValue> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.println("WRITING RESPONSE");

        // Start with the array header indicating the number of key-value pairs.
        // Each entry in the map represents a sub-array with 2 elements: key and an array of field-value pairs.
        sb.append("*").append(list.size()).append("\r\n");

        // Add each key and its corresponding value array to the response
        for (Map.Entry<String, KeyValue> entry : list.entrySet()) {
            String key = entry.getKey();
            KeyValue value = entry.getValue();

            // For each key, start a new array with 2 elements
            sb.append("*2").append("\r\n");

            // Add the key as the first bulk string
            sb.append("$").append(key.length()).append("\r\n");
            sb.append(key).append("\r\n");

            // Start a new array for the field-value pair
            sb.append("*2").append("\r\n");

            // Assuming that the KeyValue object has two parts: field and value
            String field = value.getKey(); // You'll need to adjust this based on your actual class
            String val = value.getValue();

            // Add the field as a bulk string
            sb.append("$").append(field.length()).append("\r\n");
            sb.append(field).append("\r\n");

            // Add the value as a bulk string
            sb.append("$").append(val.length()).append("\r\n");
            sb.append(val).append("\r\n");
        }

        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

    public static void sendArrayRESPresponseForXRead(OutputStream outputStream, Map<String,Map<String,KeyValue>> streamEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.println("WRITING RESPONSE IN XREAD");

        // Start with the array header indicating the number of streams
        sb.append("*").append(streamEntries.size()).append("\r\n");

        for (Map.Entry<String,Map<String,KeyValue>> streamEntry : streamEntries.entrySet()) {
            String streamKey = streamEntry.getKey();
            Map<String,KeyValue> idKeyValuePairs = streamEntry.getValue();
            sb.append("*2").append("\r\n");

            sb.append("$").append(streamKey.length()).append("\r\n");
            sb.append(streamKey).append("\r\n");

            sb.append("*").append(idKeyValuePairs.size()).append("\r\n");

            for (Map.Entry<String,KeyValue> entry : idKeyValuePairs.entrySet()) {
                String id = entry.getKey(); // Assuming the key is used as the ID
                String field = entry.getValue().getKey(); // Field should be the actual key
                String value = entry.getValue().getValue();
                System.out.println("In Writing response --> id = " + id  +" field = " + field + "value = " + value + "\n");

                sb.append("*2").append("\r\n");

                sb.append("$").append(id.length()).append("\r\n");
                sb.append(id).append("\r\n");

                sb.append("*2").append("\r\n");

                sb.append("$").append(field.length()).append("\r\n");
                sb.append(field).append("\r\n");

                sb.append("$").append(value.length()).append("\r\n");
                sb.append(value).append("\r\n");
            }
        }
        System.out.println("OUTPUT --> " + sb.toString());
        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

    public static void sendArrayRESPresponseForStrings(OutputStream outputStream, List<String> list) throws IOException{
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(list.size()).append("\r\n");
        for(String s : list){
            sb.append("$").append(s.length()).append("\r\n");
            sb.append(s).append("\r\n");
        }
        outputStream.write(sb.toString().getBytes());
    }

}
