package dev.nick.app.wildcard.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.WildcardApp;
import dev.nick.app.wildcard.app.AppCompat;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.repo.IProviderService;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class GuardService extends Service {

    private final static int EVENT_TICK = 0x1;
    private final static int EVENT_UPDATE = 0x2;

    private static long cycleTime = 300;

    private Handler mHandler = null;
    private boolean isUnLockActivity = false;

    private List<WildPackage> mWorkingList;

    private Logger mLogger;

    @Override
    public void onCreate() {
        super.onCreate();

        mLogger = LoggerManager.getLogger(getClass());

        final WildcardApp app = (WildcardApp) getApplication();
        mWorkingList = app.getProviderService().read();

        app.getProviderService().observe(
                new IProviderService.Observer() {
                    @Override
                    public void onChange() {
                        mHandler.removeMessages(EVENT_UPDATE);
                        mHandler.sendEmptyMessage(EVENT_UPDATE);
                    }
                }
        );

        HandlerThread handlerThread = new HandlerThread("event_thread");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
            public void dispatchMessage(android.os.Message msg) {
                switch (msg.what) {
                    case EVENT_TICK:
                        if (isLockName() && !isUnLockActivity) {
                            mLogger.debug("Locking!");
                            new PinLockStub(getApplicationContext()).lock();
                            isUnLockActivity = true;
                        }
                        break;
                    case EVENT_UPDATE:
                        mWorkingList = app.getProviderService().read();
                        break;
                }
                mHandler.sendEmptyMessageDelayed(EVENT_TICK, cycleTime);
            }
        };
        mHandler.sendEmptyMessage(EVENT_TICK);
    }

    private boolean isLockName() {
        String packageName = AppCompat.from(this).getTopPackage();

        if (getHomes().contains(packageName)) {
            isUnLockActivity = false;
        }

        mLogger.verbose(packageName);

        WildPackage wildPackage = new WildPackage();
        wildPackage.setPkgName(packageName);
        return mWorkingList.contains(wildPackage);
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
