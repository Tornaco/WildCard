package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.logger.LoggerManager;
import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.QuickTile;

class TimeoutTile extends QuickTile {

    TimeoutTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.settings_timeout;
        this.iconRes = R.drawable.ic_access_time;
        this.summary = getContext().getString(R.string.settings_timeout_hint,
                String.valueOf(SettingsProvider.get().sessionTimeout(getContext())));

        this.tileView = new EditTextTileView(getContext()) {

            @Override
            protected int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected CharSequence getHint() {
                return getContext().getString(R.string.settings_timeout_hint,
                        String.valueOf(SettingsProvider.get().sessionTimeout(getContext())));
            }

            @Override
            protected CharSequence getDialogTitle() {
                return getContext().getString(R.string.settings_timeout_dialog_title);
            }

            @Override
            protected CharSequence getPositiveButton() {
                return getContext().getString(android.R.string.ok);
            }

            @Override
            protected CharSequence getNegativeButton() {
                return getContext().getString(android.R.string.cancel);
            }

            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                if (getEditText().toString() == null) return;
                SettingsProvider.get().setSessionTimeout(getContext(),
                        Integer.parseInt(getEditText().getText().toString()));
                getSummaryTextView().setText(getContext().getString(R.string.settings_timeout_hint,
                        String.valueOf(SettingsProvider.get().sessionTimeout(getContext()))));
            }
        };

        updateState();

        SettingsProvider.get().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                updateState();
            }
        });
    }

    private void updateState() {
        int st = SettingsProvider.get().verifyStrategy(getContext());
        LoggerManager.getLogger(getClass()).debug("updateState, st:" + st);
        switch (st) {
            case SettingsProvider.NeedVerifyAfter.TIMEOUT:
                getTileView().getSummaryTextView().setText(getContext().getString(R.string.settings_timeout_hint,
                        String.valueOf(SettingsProvider.get().sessionTimeout(getContext()))));
                setEnabled(true);
                break;
            default:
                getTileView().getSummaryTextView().setText(R.string.settings_timeout_not_available);
                setEnabled(false);
                break;
        }
    }
}
