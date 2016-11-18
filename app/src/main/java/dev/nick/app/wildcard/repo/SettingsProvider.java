package dev.nick.app.wildcard.repo;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Observable;

import dev.nick.app.wildcard.R;

public abstract class SettingsProvider extends Observable {

    private static Impl sImpl;

    public static synchronized SettingsProvider get() {
        if (sImpl == null) sImpl = new Impl();
        return sImpl;
    }

    public abstract boolean enabled(Context context);

    public abstract void setEnable(Context context, boolean value);

    public abstract boolean gridView(Context context);

    public abstract void setGridView(Context context, boolean value);

    public abstract boolean powerSave(Context context);

    public abstract void setPowerSave(Context context, boolean enabled);

    public abstract long guardInterval(Context context);

    public abstract int verifyStrategy(Context context);

    public abstract void setVerifyStrategy(Context context, int value);

    public abstract int sessionTimeout(Context context);

    public abstract void setSessionTimeout(Context context, int value);

    public abstract boolean startOnBoot(Context context);

    public abstract void setStartOnBoot(Context context, boolean value);

    public abstract int themeColor(Context context);

    public abstract void setThemeColor(Context context, int color);

    public abstract boolean backHooked(Context context);

    public abstract void setBackHooked(Context context, boolean value);

    public abstract boolean homeHooked(Context context);

    public abstract void setHomeHooked(Context context, boolean value);

    public interface NeedVerifyAfter {
        int BOOT = 0;
        int SCREEN_ON = 1;
        int EVERY_TIME = 2;
        int TIMEOUT = 3;
    }

    private static class Impl extends SettingsProvider {

        private static final String KEY_ENABLED = "settings.app.enabled";
        private static final String KEY_GRID_VIEW = "settings.view.grid";
        private static final String KEY_GUARD_POWER_SAVE = "settings.guard.power.save";
        private static final String KEY_GUARD_STRATEGY = "settings.guard.verify.strategy";
        private static final String KEY_GUARD_TIMEOUT = "settings.guard.verify.timeout";
        private static final String KEY_START_ON_BOOT = "settings.start.on.boot";
        private static final String KEY_THEME_COLOR = "settings.theme.color";
        private static final String KEY_HOOK_KEY_BACK = "settings.key.hook.back";
        private static final String KEY_HOOK_KEY_HOME = "settings.key.hook.home";


        @Override
        public boolean enabled(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_ENABLED, true);
        }

        @Override
        public void setEnable(Context context, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_ENABLED, value)
                    .apply();
            setChanged();
            notifyObservers(KEY_ENABLED);
        }

        @Override
        public boolean gridView(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_GRID_VIEW, false);
        }

        @Override
        public void setGridView(Context context, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_GRID_VIEW, value)
                    .apply();
            setChanged();
            notifyObservers(KEY_GRID_VIEW);
        }

        @Override
        public boolean powerSave(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_GUARD_POWER_SAVE, false);
        }

        @Override
        public void setPowerSave(Context context, boolean enabled) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_GUARD_POWER_SAVE, enabled)
                    .apply();
            setChanged();
            notifyObservers(KEY_GUARD_POWER_SAVE);
        }

        @Override
        public long guardInterval(Context context) {
            return context.getResources()
                    .getInteger(powerSave(context) ? R.integer.guard_interval_low
                            : R.integer.guard_interval_normal);
        }

        @Override
        public int verifyStrategy(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(KEY_GUARD_STRATEGY, NeedVerifyAfter.EVERY_TIME);
        }

        @Override
        public void setVerifyStrategy(Context context, int value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putInt(KEY_GUARD_STRATEGY, value)
                    .apply();
            setChanged();
            notifyObservers(KEY_GUARD_STRATEGY);
        }

        @Override
        public int sessionTimeout(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(KEY_GUARD_TIMEOUT,
                            context.getResources().getInteger(R.integer.guard_timeout));
        }

        @Override
        public void setSessionTimeout(Context context, int value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putInt(KEY_GUARD_TIMEOUT, value)
                    .apply();
            setChanged();
            notifyObservers(KEY_GUARD_TIMEOUT);
        }

        @Override
        public boolean startOnBoot(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_START_ON_BOOT, true);
        }

        @Override
        public void setStartOnBoot(Context context, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_START_ON_BOOT, value)
                    .apply();
        }

        @Override
        public int themeColor(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(KEY_THEME_COLOR,
                            ContextCompat.getColor(context, R.color.primary));
        }

        @Override
        public void setThemeColor(Context context, int color) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putInt(KEY_THEME_COLOR, color)
                    .apply();
            setChanged();
            notifyObservers(KEY_THEME_COLOR);
        }

        @Override
        public boolean backHooked(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_HOOK_KEY_BACK, true);
        }

        @Override
        public void setBackHooked(Context context, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_HOOK_KEY_BACK, value)
                    .apply();
        }

        @Override
        public boolean homeHooked(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_HOOK_KEY_HOME, true);
        }

        @Override
        public void setHomeHooked(Context context, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(KEY_HOOK_KEY_HOME, value)
                    .apply();
        }
    }
}
