package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.wildcard.LocalPwdResetter;
import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

class ComplexPwdTile extends QuickTile {

    ComplexPwdTile(@NonNull final Context context) {

        super(context, null);

        this.titleRes = R.string.settings_pwd_complex;
        this.iconRes = R.drawable.ic_dots;

        this.tileView = new SwitchTileView(getContext()) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().complexPwd(context));
                SettingsProvider.get().addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object o) {
                        boolean complex = SettingsProvider.get().complexPwd(getContext());
                        setChecked(complex);
                    }
                });
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);

                Intent intent = new Intent(context, LocalPwdResetter.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("complex", checked);
                context.startActivity(intent);

                // Hooked.
                setChecked(!checked);
            }
        };
    }
}
