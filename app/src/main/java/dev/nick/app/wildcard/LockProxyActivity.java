package dev.nick.app.wildcard;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nick.scalpel.core.opt.SharedExecutor;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.camera.SpyPinLock;
import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class LockProxyActivity extends TransactionSafeActivity {

    PinLockStub mStub;
    Logger mLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        applyTheme();

        setContentView(R.layout.activiy_locker);

        resolveIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLogger.info("onNewIntent");
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        final String pkg = intent.getStringExtra("pkg");
        final int color = intent.getIntExtra("color", ContextCompat.getColor(this, R.color.primary));

        mLogger.debug("pkg:" + pkg);

        PinLockStub.LockSettings settings = new PinLockStub.LockSettings(null, pkg, color, false, false);

        mStub = new SpyPinLock(this, settings, new PinLockStub.Listener() {
            @Override
            public void onShown(PinLockStub.LockSettings settings) {
                // Noop
            }

            @Override
            public void onDismiss(PinLockStub.LockSettings info) {
                // Noop
            }
        }) {
            @Override
            protected void addPinView(boolean animate) {
                initView();
                if (mPinLayout.getWindowToken() != null) {
                    return;
                }
                ViewGroup container = (ViewGroup) findViewById(R.id.container);
                container.removeAllViews();
                container.addView(mPinLayout);
            }

            @Override
            protected synchronized void removePinView(boolean animate) {
                Bundle data = new Bundle(1);
                data.putString("pkg", pkg);
                Event event = new Event(EventDefination.EVENT_VERIFY_SUCCESS, data);
                EventBus.from(LockProxyActivity.this).publish(event);
                finish();
            }
        };
        mStub.lock();
        new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... voids) {
                return getAppIcon(pkg);
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                ImageView logoView = (ImageView) findViewById(dev.nick.app.pinlock.R.id.logo);
                logoView.setImageDrawable(drawable);
            }
        }.executeOnExecutor(SharedExecutor.get().getService());
    }

    @Override
    public void onBackPressed() {
        // Hooked.
    }

    public Drawable getAppIcon(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return info.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            return ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
        }
    }
}
