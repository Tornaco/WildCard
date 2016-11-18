package dev.nick.app.wildcard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.app.wildcard.tiles.SettingsDashboards;

public class SettingsActivity extends TransactionSafeActivity {

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        placeFragment(R.id.container, new SettingsDashboards(), null, true);

        SettingsProvider.get().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                applyTheme();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
