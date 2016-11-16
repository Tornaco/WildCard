package dev.nick.app.wildcard.tiles;

import java.util.List;

import dev.nick.app.wildcard.R;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;

public class SettingsDashboards extends DashboardFragment {

    @Override
    protected void onCreateDashCategories(List<Category> categories) {
        super.onCreateDashCategories(categories);

        Category view = new Category();
        view.titleRes = R.string.category_view;

        view.addTile(new GridTile(getContext()));

        categories.add(view);
    }
}