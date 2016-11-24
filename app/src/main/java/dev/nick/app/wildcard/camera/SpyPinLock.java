package dev.nick.app.wildcard.camera;

import android.content.Context;
import android.content.Intent;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.LocalPwdResetter;

public class SpyPinLock extends PinLockStub {

    public SpyPinLock(Context context, LockSettings info, Listener listener) {
        super(context, info, listener);
    }

    @Override
    public void onShowHelp() {
        super.onShowHelp();
        Intent intent = new Intent(mContext, LocalPwdResetter.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
