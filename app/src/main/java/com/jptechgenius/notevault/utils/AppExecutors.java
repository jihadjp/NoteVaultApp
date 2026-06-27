package com.jptechgenius.notevault.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application.
 * Grouping tasks like this avoids the effects of task starvation (e.g., disk reads don't wait
 * behind network requests).
 */
public class AppExecutors {

    private static final int THREAD_COUNT = 3;

    private static volatile AppExecutors instance;

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors(
                            Executors.newSingleThreadExecutor(),
                            Executors.newFixedThreadPool(THREAD_COUNT),
                            new MainThreadExecutor()
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Single-threaded executor for Room DB operations.
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * Thread pool for network calls.
     */
    public Executor networkIO() {
        return networkIO;
    }

    /**
     * Main/UI thread executor.
     */
    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
