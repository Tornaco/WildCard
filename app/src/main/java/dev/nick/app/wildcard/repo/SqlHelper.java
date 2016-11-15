package dev.nick.app.wildcard.repo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class SqlHelper extends SQLiteOpenHelper {

    static final String TABLE_NAME = "pkgs";
    private static final String DATABASE_NAME = "pkgs.db";
    private static final int DATABASE_VERSION = 1;

    SqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + TABLE_NAME
                + " (_id integer primary key autoincrement, pkgName TEXT, name TEXT, accessTimes INTEGER, lastAccessTime LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
