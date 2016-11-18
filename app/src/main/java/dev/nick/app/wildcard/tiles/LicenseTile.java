package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.github.jjobes.htmldialog.HtmlDialog;

import dev.nick.app.wildcard.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;

class LicenseTile extends QuickTile {

    private FragmentManager fragmentManager;

    LicenseTile(@NonNull Context context, FragmentManager manager) {
        super(context, null);
        this.fragmentManager = manager;
        this.titleRes = R.string.title_app_license;
        this.iconRes = R.drawable.ic_help;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                showInfo();
            }
        };
    }

    private void showInfo() {
        new HtmlDialog.Builder(fragmentManager)
                .setHtmlResId(R.raw.licenses)
                .setTitle(getContext().getString(R.string.title_licenses))
                .setShowNegativeButton(true)
                .setShowPositiveButton(true)
                .setPositiveButtonText(getContext().getString(android.R.string.ok))
                .build()
                .show();
    }
}
