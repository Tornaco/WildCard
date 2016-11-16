package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

public class GridTile extends QuickTile {

    public GridTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.settings_grid;
        this.iconRes = R.drawable.ic_menu_dash;
        this.tileView = new SwitchTileView(getContext()) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().gridView(getContext()));
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setGridView(getContext(), checked);
            }
        };
    }
}
