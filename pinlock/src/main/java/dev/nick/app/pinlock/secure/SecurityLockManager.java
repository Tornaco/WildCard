package dev.nick.app.pinlock.secure;

import android.app.Activity;

import dev.nick.app.pinlock.activity.SecureActivity;
import dev.nick.app.pinlock.application.VaultApp;
import dev.nick.app.pinlock.utils.Logger;

public class SecurityLockManager {

    final VaultApp mApp;
    private final Stack<Activity> mSecureActivities;

    public SecurityLockManager(VaultApp mApp) {
        if (mApp == null)
            throw new RuntimeException("Invalid args!");
        this.mApp = mApp;
        mSecureActivities = new Stack<>();
    }

    public void registerActivity(Activity activity) {
        if (activity instanceof SecureActivity)
            this.mSecureActivities.push(activity);
        else {
            throw new IllegalArgumentException("Only SecureActivity accepted.");
        }
    }

    public void popActivity(SecureActivity activity) {
        if (activity == this.mSecureActivities.peek()) {
            this.mSecureActivities.pop();
            Logger.d("Successfully pop an activity: " + activity.toString(), getClass());
        } else {
            if (mSecureActivities.remove(activity)) {
                Logger.d("The activity to pop is not match with the pop, but still be removed:"
                        + activity.toString(), getClass());
            } else {
                Logger.e("Failed to remove from the stack!", getClass());
            }
        }
    }

    public boolean isOnTop(Activity activity) {
        return activity == mSecureActivities.peek();
    }

    public void checkState(Activity activity) {
        Logger.d("SecurityLockManager, check state for: " + mSecureActivities.toString(), getClass());
        SecureActivity secureActivity = (SecureActivity) mSecureActivities.peek();
        if (secureActivity != null && activity == secureActivity)
            secureActivity.checkStateWhenResume(this);
        else {
            Logger.e("No activity exist or activity not match to check state!", getClass());
        }
    }
}
