package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class PowerSaveTile extends QuickTile {

    PowerSaveTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.settings_power_save;
        this.iconRes = R.drawable.ic_battery_low;
        this.summaryRes = R.string.summary_power_save;

        this.tileView = new SwitchTileView(getContext()) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().powerSave(getContext()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setPowerSave(getContext(), checked);
            }
        };
    }
}
