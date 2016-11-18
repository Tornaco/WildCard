package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.app.wildcard.service.GuardService;
import dev.nick.app.wildcard.service.SharedExecutor;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class ToggleTile extends QuickTile {

    ToggleTile(@NonNull Context context, final Callback callback) {
        super(context, null);

        this.titleRes = R.string.title_enable;
        this.iconRes = R.drawable.ic_verified;

        this.tileView = new SwitchTileView(getContext()) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().enabled(getContext()));
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                if (checked)
                    callback.onEnabled();
                else
                    callback.onDisabled();
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
