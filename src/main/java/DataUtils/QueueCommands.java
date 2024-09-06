package DataUtils;

import RedisServer.ClientSession;
import RedisCommandExecutor.RedisCommandsParser.RedisCommand;

import java.util.Queue;

public class QueueCommands {

    public static void queueCommands(RedisCommand command, ClientSession session) {

        Queue<RedisCommand> queueOfCommandsForMultiAndExec = session.getCommandQueue();

        if (!queueOfCommandsForMultiAndExec.isEmpty()) {

            while (!queueOfCommandsForMultiAndExec.isEmpty() && !queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")) {
                queueOfCommandsForMultiAndExec.poll();
            }
        }

        queueOfCommandsForMultiAndExec.add(command);

        System.out.printf("Queued command: %s\n", command.getCommand());
    }

}
