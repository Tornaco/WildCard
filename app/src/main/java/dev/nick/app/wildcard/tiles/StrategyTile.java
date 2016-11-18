package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;

class StrategyTile extends QuickTile {

    private String[] mSts = null;

    StrategyTile(@NonNull Context context) {

        super(context, null);

        this.titleRes = R.string.settings_verify_st;
        this.iconRes = R.drawable.ic_key;

        this.mSts = getContext().getResources().getStringArray(R.array.sts);

        final int st = SettingsProvider.get().verifyStrategy(getContext());
        this.summary = mSts[st];

        this.tileView = new DropDownTileView(getContext()) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(st, false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mSts);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                SettingsProvider.get().setVerifyStrategy(getContext(), position);
                getSummaryTextView().setText(mSts[position]);
            }
        };
    }
}
