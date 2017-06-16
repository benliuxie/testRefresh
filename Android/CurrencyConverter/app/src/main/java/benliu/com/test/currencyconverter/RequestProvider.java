package benliu.com.test.currencyconverter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
/**
 * Created by beliu on 2017-06-15.
 */

public class RequestProvider extends ContentProvider {

    private static final String TAG = "RequestProvider";

    private SQLiteOpenHelper mSqliteOpenHelper;
    private static final UriMatcher sUriMatcher;

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".db";

    private static final int
            TABLE_ITEMS = 0;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TableItems.TABLENAME + "/offset/" + "#", TABLE_ITEMS);
    }

    public static Uri urlForItems(int limit) {
        return Uri.parse("content://" + AUTHORITY + "/" + TableItems.TABLENAME + "/offset/" + limit);
    }

    @Override
    public boolean onCreate() {
        mSqliteOpenHelper = CustomSqliteOpenHelper.getInstance(getContext());
        return true;
    }

    @Override
    synchronized public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
        Cursor c = null;
        String offset;

        switch (sUriMatcher.match(uri)) {
            case TABLE_ITEMS: {
                sqb.setTables(TableItems.TABLENAME);
                offset = uri.getLastPathSegment();
                break;
            }

            default:
                throw new IllegalArgumentException("uri not recognized!");
        }

        int intOffset = Integer.parseInt(offset);

        String limitArg = intOffset + ", " + 200;
        Log.d(TAG, "query: " + limitArg);
        c = sqb.query(db, projection, selection, selectionArgs, null, null, "name ASC", limitArg);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".item";
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String table = "";
        switch (sUriMatcher.match(uri)) {
            case TABLE_ITEMS: {
                table = TableItems.TABLENAME;
                break;
            }
        }

        long result = mSqliteOpenHelper.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (result == -1) {
            throw new SQLException("insert with conflict!");
        }

        Uri retUri = ContentUris.withAppendedId(uri, result);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return -1;
    }
}
