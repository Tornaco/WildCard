package dev.nick.app.pinlock.activity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

public class EmptyActivity extends VaultEntryActivity {

    CancellationSignal mSignal;

    @Override
    protected boolean isEntryActivity() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSignal = new CancellationSignal();
        FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(this);
        fingerprintManagerCompat.authenticate(null, 0, mSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                Log.d("FPTe", "onAuthenticationError");
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d("FPTe", "onAuthenticationSucceeded");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d("FPTe", "onAuthenticationFailed");
            }
        }, new Handler(Looper.getMainLooper()));

    }
}
