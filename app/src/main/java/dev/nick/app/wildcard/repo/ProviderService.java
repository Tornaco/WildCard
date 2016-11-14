package dev.nick.app.wildcard.repo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
        values.put("accessTimes", wildPackage.getAccessTimes());
        values.put("lastAccessTime", wildPackage.getLastAccessTime());
        Uri uri = contentResolver.insert(insertUri, values);
        mLogger.debug("inserted uri:" + uri);
    }

    @Override
    public void remove(@NonNull WildPackage wildPackage) {
        mLogger.info(wildPackage);
        ContentResolver contentResolver = mContext.getContentResolver();
        int id = wildPackage.getId();
        String idStr = "content://dev.nick.app.wildcard.packageProvider/pkg/#" + id;
        Uri delUri = Uri.parse(idStr);
        int row = contentResolver.delete(delUri, null, null);
        mLogger.debug("removed index:" + row);
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
            do {
                WildPackage wildPackage = new WildPackage();
                wildPackage.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                wildPackage.setPkgName(cursor.getString(cursor.getColumnIndex("pkgName")));
                out.add(wildPackage);
                mLogger.info("Adding to out:" + wildPackage);
            } while (cursor.moveToNext());
        }
        return out;
    }
}
