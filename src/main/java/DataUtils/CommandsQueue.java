package DataUtils;

import RedisServer.ClientSession;
import RedisCommandExecutor.RedisParser.RedisCommand;

import java.util.Queue;

public class CommandsQueue {

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
