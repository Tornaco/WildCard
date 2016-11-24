package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;

import com.nick.scalpel.core.opt.SharedExecutor;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.app.wildcard.service.GuardService;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class ToggleTile extends QuickTile {

    ToggleTile(@NonNull Context context, final Callback callback) {
        super(context, null);

        this.titleRes = R.string.title_enable;
        this.iconRes = R.drawable.ic_verified_on;

        this.tileView = new SwitchTileView(getContext()) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().enabled(getContext()));
                getImageView().setColorFilter(ContextCompat.getColor(getContext(), isChecked() ? R.color.accent : R.color.tile_icon_tint));
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                if (checked)
                    callback.onEnabled();
                else
                    callback.onDisabled();
                getImageView().setColorFilter(ContextCompat.getColor(getContext(), checked ? R.color.accent : R.color.tile_icon_tint));
                SharedExecutor.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        SettingsProvider.get().setEnable(getContext(), checked);
                        if (checked) {
                            getContext().startService(new Intent(getContext(), GuardService.class));
                        }
                    }
                });
            }
        };
    }

    interface Callback {
        void onEnabled();

        void onDisabled();
    }
}
