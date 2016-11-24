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
        view.addTile(new ColorTile(getContext(), getFragmentManager()));

        Category device = new Category();
        device.titleRes = R.string.category_device;
        device.addTile(new PowerSaveTile(getContext()));
        device.addTile(new BootTile(getContext()));

        Category secure = new Category();
        secure.titleRes = R.string.category_secure;
        secure.addTile(new ComplexPwdTile(getContext()));

        Category strategy = new Category();
        strategy.titleRes = R.string.category_st;
        strategy.addTile(new StrategyTile(getContext()));
        strategy.addTile(new TimeoutTile(getContext()));
        strategy.addTile(new CompatTile(getContext()));

        Category others = new Category();
        others.titleRes = R.string.category_others;
        others.addTile(new LicenseTile(getContext(), getFragmentManager()));

        categories.add(view);
        categories.add(device);
        //categories.add(secure);
        categories.add(strategy);
        categories.add(others);
    }
}