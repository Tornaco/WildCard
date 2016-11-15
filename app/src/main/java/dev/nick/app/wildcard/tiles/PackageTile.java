package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.tiles.tile.CheckboxTileView;
import dev.nick.tiles.tile.QuickTile;

class PackageTile extends QuickTile {

    private WildPackage mPackage;

    PackageTile(@NonNull Context context, WildPackage pkg, final OnCheckedChangeListener listener) {
        super(context, null);
        this.title = pkg.getName();
        this.iconDrawable = pkg.getIcon();
        this.mPackage = pkg;
        this.tileView = new CheckboxTileView(context) {

            @Override
            protected void onViewInflated(View view) {
                super.onViewInflated(view);
                ImageView iconView = getImageView();
                ViewGroup.LayoutParams params = iconView.getLayoutParams();
                int size = getContext().getResources().getDimensionPixelSize(R.dimen.package_icon_size);
                params.width = size;
                params.height = size;
                iconView.setLayoutParams(params);
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                listener.onCheckChanged(PackageTile.this, checked);
            }

            @Override
            protected boolean isIconTintEnabled() {
                return false;
            }
        };
    }

    public WildPackage getPackage() {
        return mPackage;
    }

    public boolean isChecked() {
        return ((CheckboxTileView) getTileView()).isChecked();
    }

    public interface OnCheckedChangeListener {
        void onCheckChanged(PackageTile tile, boolean checked);
    }
}
