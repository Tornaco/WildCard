package dev.nick.app.wildcard.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nick on 16-2-7.
 * Email: nick.guo.dev@icloud.com
 * Github: https://github.com/NickAndroid
 */
public class SharedExecutor {


    private static SharedExecutor sInstance;

    private ExecutorService mService;

    private SharedExecutor() {
        this.mService = Executors.newFixedThreadPool(Runtime
                .getRuntime().availableProcessors());
    }

    public static synchronized SharedExecutor get() {
        if (sInstance == null) sInstance = new SharedExecutor();
        return sInstance;
    }

    public void execute(Runnable runnable) {
        mService.execute(runnable);
    }

    public ExecutorService getService() {
        return mService;
    }
}
