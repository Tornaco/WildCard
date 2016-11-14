package dev.nick.app.wildcard;

import dev.nick.app.pinlock.activity.SecureActivity;

public class LockActivity extends SecureActivity {

    @Override
    protected void onCreating() {

    }

    @Override
    protected void onPinCodeSaved(String pin) {
        super.onPinCodeSaved(pin);
        finish();
    }

    @Override
    protected void onPinCorrect() {
        super.onPinCorrect();
        finish();
    }

    @Override
    protected boolean isEntryActivity() {
        return true;
    }
}
