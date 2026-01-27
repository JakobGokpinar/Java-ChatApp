package goksoft.chat.app.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorManager {
    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(4);

    private static final ScheduledExecutorService scheduledExecutor =
            Executors.newScheduledThreadPool(2);

    public static ExecutorService getExecutor() {
        return executorService;
    }

    public static ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public static void shutdown() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
    }
}
