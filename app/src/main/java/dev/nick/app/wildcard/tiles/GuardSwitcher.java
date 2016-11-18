package dev.nick.app.wildcard.tiles;

import android.content.Context;

import java.util.List;

import dev.nick.app.wildcard.R;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;

public class GuardSwitcher extends DashboardFragment {

    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) getActivity();
    }

    @Override
    protected void onCreateDashCategories(List<Category> categories) {

        super.onCreateDashCategories(categories);

        Category toggle = new Category();
        toggle.addTile(new ToggleTile(getContext(), mCallback));
        categories.add(toggle);

        Category packages = new Category();
        packages.titleRes = R.string.category_apps;
        categories.add(packages);
    }

    public interface Callback extends ToggleTile.Callback {
    }

}