package dev.nick.app.wildcard.camera;

import android.content.Context;

import dev.nick.app.pinlock.PinLockStub;

public class SpyPinLock extends PinLockStub {

    public SpyPinLock(Context context, LockSettings info, Listener listener) {
        super(context, info, listener);
    }
}
