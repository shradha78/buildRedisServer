package RedisCommandExecutor.RedisCommands;

import DataUtils.Constants;
import RedisServer.ClientSession;
import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static RedisResponses.ShortParsedResponses.sendBulkStringResponse;
import static RedisResponses.ShortParsedResponses.sendErrorResponse;


public class XaddCommand implements IRedisCommandHandler{

    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String streamKey = args.get(0);
        String streamKeyId = args.get(1);
        KeyValue newKeyValueToBeAdded = new KeyValue(args.get(2), args.get(3), 0);

        RedisStreams redisStreams = DataUtils.StreamsData.getStreamData(streamKey);
        Constants validationResult = redisStreams.validateStreamId(streamKeyId);

        if (validationResult != Constants.VALID) {
            handleInvalidId(validationResult, outputStream);
            return;
        }
        try {
            synchronized (redisStreams) {

                streamKeyId = redisStreams.addEntryToStreamID(streamKeyId, newKeyValueToBeAdded);
                System.out.println("Is the key added to streams ? : "  + redisStreams.checkIfValueIsAddedToMainStreams(streamKeyId) + "\n");
                redisStreams.notifyAll();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendBulkStringResponse(outputStream, streamKeyId, "Stream Bulk String Output : ");
    }

    private void handleInvalidId(Constants validationResult, OutputStream outputStream) throws IOException {
        if (validationResult == Constants.EQUAL_OR_SMALLER) {
            sendErrorResponse(outputStream, "The ID specified in XADD is equal or smaller than the target stream top item");
        } else if (validationResult == Constants.GREATER_THAN_ZERO) {
            sendErrorResponse(outputStream, "The ID specified in XADD must be greater than 0-0");
        }
    }
}
