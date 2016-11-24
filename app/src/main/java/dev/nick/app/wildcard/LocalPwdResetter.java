package dev.nick.app.wildcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import dev.nick.app.pinlock.PwdReSetter;
import dev.nick.app.wildcard.repo.SettingsProvider;

public class LocalPwdResetter extends PwdReSetter {

    boolean mComplex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    void resolveIntent(Intent intent) {
        if (intent == null) return;
        mComplex = intent.getBooleanExtra("complex", SettingsProvider.get().complexPwd(this));
    }

    @Override
    public void onPwdSet() {
        super.onPwdSet();
        SettingsProvider.get().setComplexPwe(this, mComplex);
    }
}
