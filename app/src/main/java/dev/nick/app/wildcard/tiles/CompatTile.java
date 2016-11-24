package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

public class CompatTile extends QuickTile {

    public CompatTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.settings_compat_mode;
        this.summaryRes = R.string.summary_compat_mode;
        this.iconRes = R.drawable.ic_android;

        this.tileView = new SwitchTileView(getContext()) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().compatMode(getContext()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setCompatMode(getContext(), checked);
            }
        };
    }
}
