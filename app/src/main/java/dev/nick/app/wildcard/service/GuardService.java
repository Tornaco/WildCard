package dev.nick.app.wildcard.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.WildcardApp;
import dev.nick.app.wildcard.app.AppCompat;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.camera.SpyPinLock;
import dev.nick.app.wildcard.repo.IProviderService;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class GuardService extends Service implements PinLockStub.Listener {

    private final static int EVENT_TICK = 0x1;
    private final static int EVENT_UPDATE = 0x2;

    private final Map<String, WildPackage> mWorkingList = new HashMap<>();

    private String mLastLockingPackage = "anyone";

    private Handler mHandler = null;
    private Handler mUIThreadHandler;

    private AtomicBoolean mLastLockingKeeping = new AtomicBoolean(false);
    private AtomicBoolean mLockingUIShowing = new AtomicBoolean(false);

    private Logger mLogger;

    private boolean mScreenOn = true;
    private boolean mEnabled = false;

    private long mServiceInterval;
    private int mThemeColor;
    private boolean hookBack, hookHome;

    private SessionManager mSessionManager;

    private PinLockStub mLocker;

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                // Bad action.
                return;
            }
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                onScreenState(true);
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                onScreenState(false);
            }
        }
    };

    private Observer mSettingsObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            readSettings();
        }
    };

    private void onScreenState(boolean on) {
        mScreenOn = on;
        mLogger.debug("Screen on:" + on);
        if (!on) {
            mLastLockingKeeping.set(false);
            mSessionManager.onScreenOff();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLogger = LoggerManager.getLogger(getClass());

        mLogger.debug("Bring up!");

        readWorkingPackages();

        final WildcardApp app = (WildcardApp) getApplication();
        app.getProviderService().observe( // No need to unSub.
                new IProviderService.Observer() {
                    @Override
                    public void onChange() {
                        mHandler.removeMessages(EVENT_UPDATE);
                        mHandler.sendEmptyMessage(EVENT_UPDATE);
                    }
                }
        );

        readSettings();
        SettingsProvider.get().addObserver(mSettingsObserver);

        mSessionManager = new SessionManager(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, intentFilter);

        HandlerThread handlerThread = new HandlerThread("event_thread");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
            public void dispatchMessage(Message msg) {
                switch (msg.what) {
                    case EVENT_TICK:
                        onTick();
                        mHandler.sendEmptyMessageDelayed(EVENT_TICK, mServiceInterval);
                        break;
                    case EVENT_UPDATE:
                        readWorkingPackages();
                        break;
                }
            }
        };
        mHandler.sendEmptyMessage(EVENT_TICK);

        mUIThreadHandler = new Handler(Looper.getMainLooper());
    }

    private void onTick() {
        if (mScreenOn && (!mLockingUIShowing.get() || !hookBack)) {
            String pkgName = readTopPackage();
            if (!mLastLockingKeeping.get()) {
                if (pkgName != null) {
                    mLastLockingKeeping.set(true);
                    WildPackage wildPackage = mWorkingList.get(pkgName);
                    if (wildPackage == null) return;
                    mLastLockingPackage = pkgName;
                    mLogger.verbose("Locking:" + pkgName);

                    if (mSessionManager.verified(pkgName)) {
                        mLogger.verbose("Ignored verified pkg:" + pkgName);
                        return;
                    }

                    PinLockStub.LockSettings settings = new PinLockStub.LockSettings(
                            wildPackage.getIcon(), wildPackage.getPkgName(), mThemeColor
                            , hookBack, hookHome);

                    mLocker = new SpyPinLock(getApplicationContext(), settings, GuardService.this);
                    mLocker.lock();
                }
            }
        }
    }

    private void readSettings() {
        SettingsProvider provider = SettingsProvider.get();
        mServiceInterval = provider.guardInterval(this);
        mThemeColor = provider.themeColor(this);
        hookBack = provider.backHooked(this);
        hookHome = provider.homeHooked(this);
        mEnabled = provider.enabled(this);

        mLogger.debug("Service interval:" + mServiceInterval);

        if (!mEnabled) {
            stopSelf();
        }
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
            onLockingPkgHidden();
        }

        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        WildPackage wildPackage = new WildPackage();
        wildPackage.setPkgName(packageName);
        return (mWorkingList.get(packageName) != null ? packageName : null);
    }

    private void onLockingPkgHidden() {
        if (mLocker != null && !hookBack && mLocker.isLocked()) {
            mLocker.unLock(false);
        }
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
    public void onDestroy() {
        super.onDestroy();
        SettingsProvider.get().deleteObserver(mSettingsObserver);
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mScreenReceiver);
        mLogger.debug("Shutting down!");
    }

    @Override
    public void onShown(PinLockStub.LockSettings settings) {
        mLockingUIShowing.set(true);
    }

    @Override
    public void onDismiss(PinLockStub.LockSettings info) {
        mLockingUIShowing.set(false);
        mSessionManager.setVerified(info.getPkg());
    }

    private static class SessionManager extends Handler {

        final List<String> mVerifiedPackages;
        final int MSG_CLEAR_PKG = 0x10;
        private AtomicInteger mVerifyStrategy;

        private long mTimeoutMills;

        private Logger mLogger;

        SessionManager(final Context context) {
            mLogger = LoggerManager.getLogger(getClass());
            mVerifiedPackages = new ArrayList<>();
            readSettings(context);
            SettingsProvider.get().addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object o) {
                    readSettings(context);
                }
            });
        }

        private void readSettings(Context context) {
            int strategy = SettingsProvider.get().verifyStrategy(context);
            if (mVerifyStrategy != null && mVerifyStrategy.get() != strategy) {
                mVerifyStrategy.set(strategy);
                onVerifyStrategyChange();
            } else if (mVerifyStrategy == null) {
                mVerifyStrategy = new AtomicInteger(strategy);
            } else {
                mVerifyStrategy.set(strategy);
            }
            mTimeoutMills = SettingsProvider.get().sessionTimeout(context) * 1000;

            mLogger.debug("mVerifyStrategy:" + mVerifyStrategy.get()
                    + ", mTimeoutMills:" + mTimeoutMills);
        }

        private void onVerifyStrategyChange() {
            synchronized (mVerifiedPackages) {
                mLogger.debug("Clearing session.");
                mVerifiedPackages.clear();
            }
        }

        boolean verified(String pkg) {
            switch (mVerifyStrategy.get()) {
                case SettingsProvider.NeedVerifyAfter.BOOT:
                case SettingsProvider.NeedVerifyAfter.SCREEN_ON:
                case SettingsProvider.NeedVerifyAfter.TIMEOUT:
                    synchronized (mVerifiedPackages) {
                        return mVerifiedPackages.contains(pkg);
                    }
            }
            return false;
        }

        void setVerified(String pkg) {
            synchronized (mVerifiedPackages) {
                if (!mVerifiedPackages.contains(pkg)) {
                    mVerifiedPackages.add(pkg);
                    mLogger.debug("Adding session:" + pkg);
                }
            }
            switch (mVerifyStrategy.get()) {
                case SettingsProvider.NeedVerifyAfter.TIMEOUT:
                    sendMessageDelayed(Message.obtain(this, MSG_CLEAR_PKG, pkg), mTimeoutMills);
                    break;
            }
        }

        void onScreenOff() {
            switch (mVerifyStrategy.get()) {
                case SettingsProvider.NeedVerifyAfter.SCREEN_ON:
                    synchronized (mVerifiedPackages) {
                        mLogger.debug("Clearing session.");
                        mVerifiedPackages.clear();
                    }
                    break;
                case SettingsProvider.NeedVerifyAfter.TIMEOUT:
                    break;
            }
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CLEAR_PKG:
                    String pkg = (String) msg.obj;
                    synchronized (mVerifiedPackages) {
                        mVerifiedPackages.remove(pkg);
                        mLogger.debug("Removing session:" + pkg);
                    }
                    break;
            }
        }
    }
}
