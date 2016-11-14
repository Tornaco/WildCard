package dev.nick.app.pinlock.utils;


import android.os.Looper;
import android.util.Log;

public class Logger {

    private static final boolean ALLOW_D = true;
    private static final boolean ALLOW_I = true;
    private static final boolean ALLOW_E = true;


    public static void i(String log, Class clz) {
        if (ALLOW_I) {
            if (clz == null) clz = Looper.class;
            Log.i(clz.getName(), log);
        }
    }

    public static void d(String log, Class clz) {
        if (ALLOW_D) {
            if (clz == null) clz = Looper.class;
            Log.d(clz.getName(), log);
        }
    }

    public static void e(String log, Class clz) {
        if (ALLOW_E) {
            if (clz == null) clz = Looper.class;
            Log.e(clz.getName(), log);
        }
    }
}
