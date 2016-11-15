package dev.nick.app.wildcard;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.tiles.Dashboards;

public class PackagePickerActivity extends TransactionSafeActivity implements Dashboards.DataCallback {

    private List<WildPackage> mInstalledPackages, mSystemPackages, mWildPackages;

    private boolean mSmartFilter = true;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_picker);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                readPackages();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                findViewById(R.id.progress).setVisibility(View.GONE);
                placeFragment(R.id.container, new Dashboards(), null, false);
            }
        }.execute();
    }

    void readPackages() {

        WildcardApp wildcardApp = (WildcardApp) getApplication();
        mWildPackages = wildcardApp.getProviderService().read();

        if (mInstalledPackages == null) mInstalledPackages = new ArrayList<>();
        if (mSystemPackages == null) mSystemPackages = new ArrayList<>();

        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo packageInfo : packages) {
            WildPackage tmpInfo = new WildPackage();
            tmpInfo.setName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
            tmpInfo.setPkgName(packageInfo.packageName);

            if (mWildPackages.contains(tmpInfo)) continue;
            if (mSmartFilter && isIme(packageInfo)) continue;

            tmpInfo.setIcon(packageInfo.applicationInfo.loadIcon(getPackageManager()));
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                mInstalledPackages.add(tmpInfo);
            } else {
                mSystemPackages.add(tmpInfo);
            }
        }
    }

    boolean isIme(PackageInfo info) {
        boolean isInputMethodApp = false;
        ServiceInfo[] sInfo = info.services;
        if (sInfo != null) {
            for (ServiceInfo serviceInfo : sInfo) {
                if (serviceInfo.permission != null &&
                        serviceInfo.permission.equals(Manifest.permission.BIND_INPUT_METHOD)) {
                    isInputMethodApp = true;
                    break;
                }
            }
        }
        return isInputMethodApp;
    }

    @Override
    public List<WildPackage> getInstalledPackages() {
        return mInstalledPackages;
    }

    @Override
    public List<WildPackage> getSystemPackages() {
        return mSystemPackages;
    }

    @Override
    public void apply(List<WildPackage> workingList) {
        WildcardApp wildcardApp = (WildcardApp) getApplication();
        for (WildPackage p : workingList) {
            wildcardApp.getProviderService().add(p);
        }
        finish();
    }
}
