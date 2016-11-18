package dev.nick.app.wildcard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.service.SharedExecutor;
import dev.nick.app.wildcard.tiles.PickerDashboards;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class PackagePickerActivity extends TransactionSafeActivity implements PickerDashboards.Callback {

    private List<WildPackage> mInstalledPackages;
    private List<WildPackage> mSystemPackages;
    private List<WildPackage> mSuggestionPackages;

    private boolean mSmartFilter = true;

    private Logger mLogger;

    private ProgressDialog mProgressDialog;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        applyTheme();

        setContentView(R.layout.activity_package_picker);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                readPackages();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                placeFragment(R.id.container, new PickerDashboards(), null, true);
            }
        }.executeOnExecutor(SharedExecutor.get().getService());
    }

    void readPackages() {

        WildcardApp wildcardApp = (WildcardApp) getApplication();
        List<WildPackage> wildPackages = wildcardApp.getProviderService().read();

        List<String> ignoredList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.filter_ignore_list)));
        getLaunchers(ignoredList);
        getIme(ignoredList);

        List<String> suggestionList = Arrays.asList(getResources().getStringArray(R.array.filter_suggestion_list));

        mLogger.info("Prebuilt ignore list:" + ignoredList);
        mLogger.info("Prebuilt suggest list:" + suggestionList);

        if (mInstalledPackages == null) mInstalledPackages = new ArrayList<>();
        if (mSystemPackages == null) mSystemPackages = new ArrayList<>();
        if (mSuggestionPackages == null) mSuggestionPackages = new ArrayList<>();

        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo packageInfo : packages) {

            WildPackage tmpInfo = new WildPackage();
            tmpInfo.setName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
            tmpInfo.setPkgName(packageInfo.packageName);

            mLogger.verbose("Checking pkg:" + packageInfo.packageName);

            if (ignoredList.contains(tmpInfo.getPkgName())) {
                mLogger.info("Ignored in prebuilt filter list:" + tmpInfo.getName());
                continue;
            }

            if (wildPackages.contains(tmpInfo)) {
                mLogger.info("Ignored in wild list:" + tmpInfo.getName());
                continue;
            }

            boolean enabled = packageInfo.applicationInfo.enabled;

            if (!enabled) {
                mLogger.info("Ignored no enabled:" + tmpInfo.getName());
                continue;
            }

            tmpInfo.setIcon(packageInfo.applicationInfo.loadIcon(getPackageManager()));

            if (suggestionList.contains(tmpInfo.getPkgName())) {
                mSuggestionPackages.add(tmpInfo);
            } else if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                mInstalledPackages.add(tmpInfo);
            } else {
                mSystemPackages.add(tmpInfo);
            }
        }
    }

    private void getIme(List<String> packages) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm.getInputMethodList();
        if (methodInfos == null) return;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            packages.add(inputMethodInfo.getPackageName());
            mLogger.verbose("Add ime:" + inputMethodInfo.getPackageName());
        }
    }

    public void getLaunchers(List<String> packageNames) {
        PackageManager packageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                packageNames.add(resolveInfo.activityInfo.processName);
                packageNames.add(resolveInfo.activityInfo.packageName);
                mLogger.debug("Add launcher pkg:" + resolveInfo.activityInfo.packageName);
            }
        }
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
    public List<WildPackage> getSuggestionList() {
        return mSuggestionPackages;
    }

    @Override
    public void apply(final List<WildPackage> workingList) {

        if (workingList != null && workingList.size() > 0) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    mProgressDialog = new ProgressDialog(PackagePickerActivity.this);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mProgressDialog.dismiss();
                    finish();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    WildcardApp wildcardApp = (WildcardApp) getApplication();
                    for (WildPackage p : workingList) {
                        wildcardApp.getProviderService().add(p);
                    }
                    return null;
                }
            }.executeOnExecutor(SharedExecutor.get().getService());
        }
    }

    @Override
    public void onUIReady() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
