package dev.nick.app.pinlock.utils;


import android.content.Context;
import android.content.SharedPreferences;

import dev.nick.app.pinlock.provider.VaultSettings;

public class PreferenceHelper {

    private SharedPreferences mSpre;

    public PreferenceHelper(Context c) {
        mSpre = c.getSharedPreferences(VaultSettings.PRE_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getStoredPwd() {
        return mSpre.getString(VaultSettings.PRE_KEY_PWD, null);
    }

    public void updatePwd(String pwd) {
        mSpre.edit().putString(VaultSettings.PRE_KEY_PWD, pwd).apply();
    }

    public boolean isVaultFirstRun() {
        return mSpre.getBoolean(VaultSettings.APP_FIRST_RUN, true);
    }

    public void markHasRun() {
        mSpre.edit().putBoolean(VaultSettings.APP_FIRST_RUN, false).apply();
    }
}
