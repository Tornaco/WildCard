package dev.nick.app.pinlock.application;

import android.app.Activity;
import android.app.Application;

import dev.nick.app.pinlock.secure.SecurityLockManager;
import dev.nick.app.pinlock.utils.Logger;

public class VaultApp extends Application {

    private SecurityLockManager mLockManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mLockManager == null) {
            mLockManager = new SecurityLockManager(this);
            Logger.d("VaultApp: onCreate() and init with lock manager.", getClass());
        }
    }

    public void checkState(Activity activity) {
        mLockManager.checkState(activity);
    }

    public SecurityLockManager getLockManager() {
        return mLockManager;
    }
}
