package dev.nick.app.wildcard;

import android.os.Bundle;
import android.support.annotation.Nullable;

import dev.nick.app.wildcard.tiles.SettingsDashboards;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class SettingsActivity extends TransactionSafeActivity {

    private Logger mLogger;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        placeFragment(R.id.container, new SettingsDashboards(), null, true);
    }
}
