package dev.nick.app.pinlock.utils;


import android.content.Context;
import android.content.SharedPreferences;

import dev.nick.app.pinlock.provider.LockSettings;

public class PreferenceHelper {

    private SharedPreferences mSpre;

    public PreferenceHelper(Context c) {
        mSpre = c.getSharedPreferences(LockSettings.PRE_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getStoredPwd() {
        return mSpre.getString(LockSettings.PRE_KEY_PWD, null);
    }

    public void updatePwd(String pwd) {
        mSpre.edit().putString(LockSettings.PRE_KEY_PWD, pwd).apply();
    }

    public boolean isFirstRunSetting() {
        return mSpre.getBoolean(LockSettings.APP_FIRST_RUN, true);
    }

    public void onPwdSet() {
        mSpre.edit().putBoolean(LockSettings.APP_FIRST_RUN, false).apply();
    }
}
