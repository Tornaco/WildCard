package dev.nick.app.wildcard.repo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class PackageProvider extends ContentProvider {


    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;
    private static final int PKG = 2;

    static {
        MATCHER.addURI("dev.nick.app.wildcard.packageProvider", "pkg", PKGS);
        MATCHER.addURI("dev.nick.app.wildcard.packageProvider", "pkg/#", PKG);
    }

    private SqlHelper dbOpenHelper;

    @Override
    public boolean onCreate() {
        this.dbOpenHelper = new SqlHelper(this.getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        switch (MATCHER.match(uri)) {
            case PKGS:
                return db.query(SqlHelper.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

            case PKG:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                return db.query(SqlHelper.TABLE_NAME, projection, where, selectionArgs, null,
                        null, sortOrder);

            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case PKGS:
                return "vnd.android.cursor.dir/pkg";

            case PKG:
                return "vnd.android.cursor.item/pkg";

            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case PKGS:
                long rowid = db.insert(SqlHelper.TABLE_NAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;

            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {
            case PKGS:
                count = db.delete(SqlHelper.TABLE_NAME, selection, selectionArgs);
                return count;

            case PKG:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                count = db.delete(SqlHelper.TABLE_NAME, where, selectionArgs);
                return count;

            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {
            case PKGS:
                count = db.update(SqlHelper.TABLE_NAME, values, selection, selectionArgs);
                return count;
            case PKG:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                count = db.update(SqlHelper.TABLE_NAME, values, where, selectionArgs);
                return count;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }


}
