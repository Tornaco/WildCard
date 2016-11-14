package dev.nick.app.wildcard.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.wildcard.LockActivity;
import dev.nick.app.wildcard.app.AppCompat;

public class GuardService extends Service {

    private final static int LOOPHANDLER = 0;
    private static long cycleTime = 1000;
    private final String TAG = "LockService";
    private Handler mHandler = null;
    private HandlerThread handlerThread = null;
    private boolean isUnLockActivity = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread = new HandlerThread("count_thread");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
            public void dispatchMessage(android.os.Message msg) {
                switch (msg.what) {
                    case LOOPHANDLER:
                        Log.i(TAG, "do something..." + (System.currentTimeMillis() / 1000));
                        if (isLockName() && !isUnLockActivity) {
                            Log.i(TAG, "locking...");
                            Intent intent = new Intent(GuardService.this, LockActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            isUnLockActivity = true;
                        }
                        break;
                }
                mHandler.sendEmptyMessageDelayed(LOOPHANDLER, cycleTime);
            }
        };
        mHandler.sendEmptyMessage(LOOPHANDLER);
    }

    private boolean isLockName() {

        String packageName = AppCompat.from(this).getTopPackage();

        if (getHomes().contains(packageName)) {
            isUnLockActivity = false;
        }
        Log.v("LockService", "packageName == " + packageName);

        if ("com.android.settings".equals(packageName)) {
            return true;
        }
        return false;
    }

    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
            System.out.println(ri.activityInfo.packageName);
        }
        return names;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
