package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;
import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class XaddCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String streamKey = args.get(0);
        String streamKeyId = args.get(1);
        KeyValue newKeyValueToBeAdded = new KeyValue(args.get(2), args.get(3), 0);

        RedisStreams redisStreams = Main.streams.computeIfAbsent(streamKey, RedisStreams::new);
        Constants validationResult = redisStreams.validateStreamId(streamKeyId);

        if (validationResult != Constants.VALID) {
            handleInvalidId(validationResult, outputStream);
            return;
        }

        streamKeyId = redisStreams.addEntryToStreamID(streamKeyId, newKeyValueToBeAdded);
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
