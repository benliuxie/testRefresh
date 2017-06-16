package benliu.com.test.currencyconverter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import static benliu.com.test.currencyconverter.R.id.editText;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private RecyclerView mRecyclerView;
    private Spinner mSpinner;
    private EditText mEditText;
    private static final String KEY_FIELD_VALUE = "FIELDVALUE";
    private static final String KEY_FIELD_SELECTED_POS = "SELECTION";
    private double ratio = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,getResources().getInteger(R.integer.columns));
        CustomCursorRecyclerViewAdapter mAdapter = new CustomCursorRecyclerViewAdapter(this, null);
        mSpinner = (Spinner)findViewById(R.id.spinner);
        mEditText = (EditText)findViewById(editText);
        mEditText.setText("1.0");

        mEditText.addTextChangedListener(filterTextWatcher);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        Currency[] curStrings = new Currency[DataHelper.getInstance().getCurrencies().size()];
        for(int i =0; i< DataHelper.getInstance().getCurrencies().size(); i++){
            curStrings[i] = DataHelper.getInstance().getCurrencies().get(i);
        }

        ArrayAdapter<Currency> nameArray= new ArrayAdapter<Currency>(MainActivity.this,android.R.layout.simple_spinner_item, curStrings);
        nameArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(nameArray);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Currency cur = (Currency)mSpinner.getSelectedItem();

                try{
                    double fieldVal = Double.parseDouble(mEditText.getText().toString().trim());
                    ratio = fieldVal/cur.getRate();
                    ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).setRatio(ratio);
                }catch (Exception e){
                    e.printStackTrace();
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        int itemsCountLocal = getItemsCountLocal();
        if (itemsCountLocal == 0) {
            DataHelper.getInstance().fetchCurrencies(null, this, new DBUpdateListener() {
                @Override
                public void updated() {
                    Log.d(TAG, "db updated in MainActivity");
                }
            });
        }


        getSupportLoaderManager().restartLoader(0, null, this);
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Currency cur = (Currency)mSpinner.getSelectedItem();

            try{
                double fieldVal = Double.parseDouble(mEditText.getText().toString().trim());
                ratio = fieldVal/cur.getRate();
                ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).setRatio(ratio);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };



    @Override
    protected void onPause() {
        DataHelper.savePreference(this, KEY_FIELD_VALUE, mEditText.getText().toString());



        super.onPause();
    }

    @Override
    protected void onResume() {
        String savedValue = DataHelper.getPreferenceById(this, KEY_FIELD_VALUE);
        if(!TextUtils.isEmpty(savedValue)) {
            mEditText.setText(savedValue);
        }
        super.onResume();

        DataHelper.getInstance().checkAndUpdateDB("", this, new DBUpdateListener() {
            @Override
            public void updated() {
                Log.d(TAG, "DB Updated should automatically call ui refresh");
            }
        });
    }

    @Override
    protected void onStart() {
        long pos = DataHelper.getPreferenceById(this,KEY_FIELD_SELECTED_POS, -1);
        if(pos >= 0) {
            mSpinner.setSelection((int) pos);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        long pos = mSpinner.getSelectedItemPosition();
        DataHelper.savePreference(this, KEY_FIELD_SELECTED_POS, pos);
        super.onStop();
    }

    private int getItemsCountLocal() {
        int itemsCount = 0;

        Cursor query = getContentResolver().query(RequestProvider.urlForItems(0), null, null, null, null);
        if (query != null) {
            itemsCount = query.getCount();
            query.close();
        }
        return itemsCount;
    }

    /*loader*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(this, RequestProvider.urlForItems(0), null, null, null, null);
            default:
                throw new IllegalArgumentException("no id handled!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0:
                Log.d(TAG, "onLoadFinished: loading MORE");

                Cursor cursor = ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).getCursor();

//                //fill all exisitng in adapter
//                MatrixCursor mx = new MatrixCursor(TableItems.Columns);
//                fillMx(cursor, mx);
//
//                //fill with additional result
//                fillMx(data, mx);

                ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).swapCursor(data);


                break;
            default:
                throw new IllegalArgumentException("no loader id handled!");
        }
    }

//    private Handler handlerToWait = new Handler();
//
//    private void fillMx(Cursor data, MatrixCursor mx) {
//        if (data == null)
//            return;
//
//        data.moveToPosition(-1);
//        while (data.moveToNext()) {
//            mx.addRow(new Object[]{
//                    data.getString(data.getColumnIndex(TableItems._ID)),
//                    data.getString(data.getColumnIndex(TableItems.NAME)),
//                    data.getFloat(data.getColumnIndex(TableItems.RATE))
//
//            });
//        }
//    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CustomCursorRecyclerViewAdapter) mRecyclerView.getAdapter()).swapCursor(null);
    }

    //

    private static final String TAG = "MainActivity";

}
