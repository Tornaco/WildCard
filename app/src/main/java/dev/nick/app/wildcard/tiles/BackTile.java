package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class BackTile extends QuickTile {

    BackTile(@NonNull Context context) {

        super(context, null);

        this.titleRes = R.string.settings_btn_back;
        this.iconRes = R.drawable.ic_visibility_off;

        this.tileView = new SwitchTileView(getContext()) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().backHooked(getContext()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setBackHooked(getContext(), checked);
            }
        };
    }
}
