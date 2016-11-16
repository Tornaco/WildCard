package dev.nick.app.wildcard.repo;

import android.content.Context;
import android.preference.PreferenceManager;

public abstract class SettingsProvider {

    private static Impl sImpl;

    public static synchronized SettingsProvider get() {
        if (sImpl == null) sImpl = new Impl();
        return sImpl;
    }

    public abstract boolean gridView(Context context);

    public abstract void setGridView(Context context, boolean value);

    private static class Impl extends SettingsProvider {

        private static final String KEY_GRID_VIEW = "settings.view.grid";

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
        }
    }
}
