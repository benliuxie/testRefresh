package benliu.com.test.currencyconverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by beliu on 2017-06-15.
 */

public class CustomSqliteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "CustomSqliteOpenHelper";

    private static CustomSqliteOpenHelper instance;

    private SQLiteDatabase db;
    public static CustomSqliteOpenHelper getInstance(Context context) {
        if(instance == null){
            instance = new CustomSqliteOpenHelper(context);
            instance.db = instance.getWritableDatabase();
        }
        return instance;
    }


    public CustomSqliteOpenHelper(Context context) {
        super(context, "db.db", null, 1);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TableItems.CREATE_TABLE);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TableItems.DROP_TABLE);
        onCreate(db);
    }

    public void clearTable(String table){
        this.db.delete(table, null,null);
    }
}
