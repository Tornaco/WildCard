package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class BootTile extends QuickTile {

    BootTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.settings_boot;
        this.iconRes = R.drawable.ic_play_arrow;
        this.tileView = new SwitchTileView(getContext()) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().startOnBoot(getContext()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setStartOnBoot(getContext(), checked);
            }
        };
    }
}
