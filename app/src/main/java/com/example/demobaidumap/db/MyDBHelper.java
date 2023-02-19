package com.example.demobaidumap.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
    public MyDBHelper(Context context){
        super(context, DBConstants.TABLE_NAME, null, DBConstants.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + DBConstants.TABLE_NAME + " (" +
                            DBConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                            DBConstants.USERNAME_COLUMN + " TEXT NOT NULL, "+
                            DBConstants.PASSWORD_COLUM + " TEXT NOT NULL)";
        String CREATE_ADMIN =
                "INSERT INTO " + DBConstants.TABLE_NAME +" ( "
                + DBConstants.USERNAME_COLUMN + " , " + DBConstants.PASSWORD_COLUM
                + " ) " + " VALUES ('admin', 'admin')";
        sqLiteDatabase.execSQL(CREATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_ADMIN);
        System.out.println("Database Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + DBConstants.DB_NAME;
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}
