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

    public void clearPwd() {
        mSpre.edit().putBoolean(LockSettings.APP_FIRST_RUN, true).apply();
        mSpre.edit().putString(LockSettings.PRE_KEY_PWD, null).apply();
    }

    public String getSecQuestion() {
        return mSpre.getString(LockSettings.SEC_Q, null);
    }

    public String getSecAnswer() {
        return mSpre.getString(LockSettings.SEC_A, null);
    }

    public void setQuestion(String question) {
        mSpre.edit().putString(LockSettings.SEC_Q, question).apply();
    }

    public void setAnswer(String answer) {
        mSpre.edit().putString(LockSettings.SEC_A, answer).apply();
    }

    public boolean complexPwd() {
        return mSpre.getBoolean(LockSettings.COMPLEX, true);
    }

    public void setComplexPwd(boolean value) {
        mSpre.edit().putBoolean(LockSettings.COMPLEX, value).apply();
    }
}
