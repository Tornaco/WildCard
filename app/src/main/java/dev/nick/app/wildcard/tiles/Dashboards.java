package dev.nick.app.wildcard.tiles;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.wildcard.R;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.logger.LoggerManager;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;

public class Dashboards extends DashboardFragment implements PackageTile.OnCheckedChangeListener {

    private DataCallback mDataCallback;

    private List<WildPackage> mWorkingList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataCallback = (DataCallback) getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    protected void onCreateDashCategories(List<Category> categories) {
        super.onCreateDashCategories(categories);

        Category third = new Category();
        third.titleRes = R.string.app_installed;
        for (WildPackage p : mDataCallback.getInstalledPackages()) {
            PackageTile packageTile = new PackageTile(getActivity(), p, this);
            third.addTile(packageTile);
        }
        categories.add(third);

        Category system = new Category();
        system.titleRes = R.string.app_system;
        for (WildPackage p : mDataCallback.getSystemPackages()) {
            PackageTile packageTile = new PackageTile(getActivity(), p, this);
            system.addTile(packageTile);
        }
        categories.add(system);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.picker, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDataCallback.apply(mWorkingList);
        return true;
    }

    @Override
    public void onCheckChanged(PackageTile tile, boolean checked) {
        LoggerManager.getLogger(getClass()).debug(tile.getPackage() + "-isChecked:" + tile.isChecked());
        synchronized (this) {
            if (mWorkingList == null) {
                mWorkingList = new ArrayList<>();
            }
            if (checked && !mWorkingList.contains(tile.getPackage())) {
                mWorkingList.add(tile.getPackage());
            } else if (!checked) {
                mWorkingList.remove(tile.getPackage());
            }
        }
    }

    public interface DataCallback {
        List<WildPackage> getInstalledPackages();

        List<WildPackage> getSystemPackages();

        void apply(List<WildPackage> workingList);
    }

}