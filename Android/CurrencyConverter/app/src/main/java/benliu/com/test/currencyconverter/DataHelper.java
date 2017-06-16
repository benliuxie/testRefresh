package benliu.com.test.currencyconverter;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import static benliu.com.test.currencyconverter.CursorRecyclerViewAdapter.TAG;

/**
 * Created by beliu on 2017-06-15.
 */

public class DataHelper {
    private static DataHelper instance;
    public static final String PREFS_NAME = "Preference";

    private ArrayList<Currency> currencies = new ArrayList<Currency>();
    private DataHelper(){
    }
    public synchronized static DataHelper getInstance() {
        if(instance == null){
            instance = new DataHelper();
        }
        return instance;
    }

    public ArrayList<Currency> getCurrencies(){
        return currencies;
    }

    public static final String saveFETCHTimeKey = "UPDATEDTIME";

    public void checkAndUpdateDB(final String base, final ContextWrapper wrapper, final DBUpdateListener dbListener){
        long lastTimeFetch = getPreferenceById(wrapper, saveFETCHTimeKey, 0);
        long diff = (System.currentTimeMillis() - lastTimeFetch)/1000;
        Log.d(TAG, "Time Diff from last data fetch:"+diff);

        if(currencies.size() == 0 || diff > 1800){
            fetchCurrencies(base, wrapper, dbListener);
        }
    }

    public void fetchCurrencies(final String base, final ContextWrapper wrapper, final DBUpdateListener dbListener){
        String url = "https://api.fixer.io/latest";
        if(!TextUtils.isEmpty(base)){
            url = url + "?base="+base;
        }
        HttpListener httpListener = new HttpListener() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobj = new JSONObject(response);
                    currencies.clear();
                    String b = jobj.getString("base");
                    Currency c = new Currency(b, 1.0);
                    currencies.add(c);

                    JSONObject rates = (JSONObject)jobj.get("rates");
                    Iterator<String> iter = rates.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            double value = rates.getDouble(key);
                            Currency cur = new Currency(key, value);
                            currencies.add(cur);

                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                ContentValues[] cvArray = new ContentValues[currencies.size()];
                for (int i = 0; i < cvArray.length; i++) {
                    ContentValues cv = new ContentValues();
                    Currency cur = currencies.get(i);
                    cv.put(TableItems.NAME, cur.getName());
                    cv.put(TableItems.RATE, cur.getRate());

                    cvArray[i] = cv;
                }
                CustomSqliteOpenHelper.getInstance(wrapper).clearTable(TableItems.TABLENAME);
                wrapper.getContentResolver().bulkInsert(RequestProvider.urlForItems(0), cvArray);

                savePreference(wrapper, saveFETCHTimeKey, System.currentTimeMillis());
                if(dbListener != null){
                    dbListener.updated();
                }

            }
        };
        HttpTask task = new HttpTask(httpListener);
        task.executeOnExecutor(HttpTask.SERIAL_EXECUTOR, url);
    }


    public static final String getPreferenceById(Context context, String preferenceId) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(preferenceId, null);
    }

    public static void savePreference(Context context,String preferenceId, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preferenceId, value);
        editor.commit();
    }

    public static final long getPreferenceById(Context context,String preferenceId, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong(preferenceId, defaultValue);
    }

    public static void savePreference(Context context,String preferenceId, long value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(preferenceId, value);
        editor.commit();
    }
}
