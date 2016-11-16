package dev.nick.app.wildcard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.WildcardApp;
import dev.nick.app.wildcard.app.AppCompat;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.repo.IProviderService;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class GuardService extends Service implements PinLockStub.Listener {

    private final static int EVENT_TICK = 0x1;
    private final static int EVENT_UPDATE = 0x2;

    private static long cycleTime = 300;

    private final Map<String, WildPackage> mWorkingList = new HashMap<>();

    private String mLastLockingPackage = "anyone";

    private Handler mHandler = null;

    private AtomicBoolean mLastLockingKeeping = new AtomicBoolean(false);
    private AtomicBoolean mLockingUIShowing = new AtomicBoolean(false);

    private Logger mLogger;

    @Override
    public void onCreate() {
        super.onCreate();

        mLogger = LoggerManager.getLogger(getClass());

        readWorkingPackages();

        final WildcardApp app = (WildcardApp) getApplication();
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
                        if (!mLockingUIShowing.get()) {
                            String pkgName = readTopPackage();
                            if (!mLastLockingKeeping.get()) {
                                if (pkgName != null) {
                                    mLastLockingKeeping.set(true);
                                    WildPackage wildPackage = mWorkingList.get(pkgName);
                                    if (wildPackage == null) return;
                                    mLastLockingPackage = pkgName;
                                    mLogger.verbose("Locking:" + pkgName);
                                    new PinLockStub(getApplicationContext(),
                                            new PinLockStub.LockInfo(wildPackage.getName(), wildPackage.getIcon()), GuardService.this)
                                            .lock();
                                }
                            }
                        }
                        break;
                    case EVENT_UPDATE:
                        readWorkingPackages();
                        break;
                }
                mHandler.sendEmptyMessageDelayed(EVENT_TICK, cycleTime);
            }
        };
        mHandler.sendEmptyMessage(EVENT_TICK);
    }

    private void readWorkingPackages() {
        synchronized (mWorkingList) {
            mWorkingList.clear();
            WildcardApp app = (WildcardApp) getApplication();
            List<WildPackage> packages = app.getProviderService().read();
            for (WildPackage p : packages) {
                mWorkingList.put(p.getPkgName(), p);
            }
        }
    }

    private String readTopPackage() {

        String packageName = AppCompat.from(this).getTopPackage();

        mLogger.verbose(packageName);

        if (!"com.google.android.packageinstaller".equals(packageName)
                && !"com.android.packageinstaller".equals(packageName)
                && !mLastLockingPackage.equals(packageName)) {
            mLastLockingKeeping.set(false);
        }

        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        WildPackage wildPackage = new WildPackage();
        wildPackage.setPkgName(packageName);
        return (mWorkingList.get(packageName) != null ? packageName : null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onShown() {
        mLockingUIShowing.set(true);
    }

    @Override
    public void onDismiss() {
        mLockingUIShowing.set(false);
    }
}
