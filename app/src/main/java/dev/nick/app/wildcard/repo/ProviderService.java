package dev.nick.app.wildcard.repo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class ProviderService implements IProviderService<WildPackage> {

    private Context mContext;
    private Logger mLogger;

    public ProviderService(@NonNull Context context) {
        this.mContext = context;
        this.mLogger = LoggerManager.getLogger(getClass());
    }

    @Override
    public void add(@NonNull WildPackage wildPackage) {
        mLogger.info(wildPackage);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri insertUri = Uri.parse("content://dev.nick.app.wildcard.packageProvider/pkg");
        ContentValues values = new ContentValues();
        values.put("pkgName", wildPackage.getPkgName());
        values.put("name", wildPackage.getName());
        values.put("accessTimes", wildPackage.getAccessTimes());
        values.put("lastAccessTime", wildPackage.getLastAccessTime());
        Uri uri = contentResolver.insert(insertUri, values);
        mLogger.verbose("inserted uri:" + uri);
    }

    @Override
    public void remove(@NonNull WildPackage wildPackage) {
        mLogger.info(wildPackage);
        ContentResolver contentResolver = mContext.getContentResolver();
        int id = wildPackage.getId();
        Uri uri = Uri.parse("content://dev.nick.app.wildcard.packageProvider/pkg");
        String where = "_id=" + id;
        int cnt = contentResolver.delete(uri, where, null);
        mLogger.verbose("removed count:" + cnt);
    }

    @Override
    public List<WildPackage> read() {
        List<WildPackage> out = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = Uri.parse("content://dev.nick.app.wildcard.packageProvider/pkg");
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) return out;
        if (cursor.getCount() == 0) return out;
        if (cursor.moveToFirst()) {
            PackageManager pm = mContext.getPackageManager();
            do {
                WildPackage wildPackage = new WildPackage();
                wildPackage.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                wildPackage.setPkgName(cursor.getString(cursor.getColumnIndex("pkgName")));
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(wildPackage.getPkgName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                    wildPackage.setIcon(packageInfo.applicationInfo.loadIcon(pm));
                } catch (PackageManager.NameNotFoundException ignored) {

                }
                wildPackage.setName(cursor.getString(cursor.getColumnIndex("name")));
                out.add(wildPackage);
                mLogger.verbose("Adding to out:" + wildPackage);
            } while (cursor.moveToNext());
        }
        return out;
    }

    @Override
    public void observe(@NonNull final Observer observer) {
        Uri uri = Uri.parse("content://dev.nick.app.wildcard.packageProvider/pkg");
        mContext.getContentResolver().registerContentObserver(uri, true, new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                observer.onChange();
            }
        });
    }
}
