package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RelativeLayout;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.colorpicker.ColorPickerDialog;
import dev.nick.app.wildcard.colorpicker.ColorPickerSwatch;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;

class ColorTile extends QuickTile {

    ColorTile(@NonNull Context context, final FragmentManager manager) {
        super(context, null);
        this.titleRes = R.string.settings_color;
        this.iconRes = R.drawable.ic_theme;

        this.tileView = new QuickTileView(getContext(), this) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
            }

            @Override
            public void onClick(final View v) {
                super.onClick(v);
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                        getResources().getIntArray(R.array.material_ringers), 0, 3, 80);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        SettingsProvider.get().setThemeColor(getContext(), color);
                    }
                });
                colorPickerDialog.show(manager, "colorPickerDialog");
            }
        };
    }
}
