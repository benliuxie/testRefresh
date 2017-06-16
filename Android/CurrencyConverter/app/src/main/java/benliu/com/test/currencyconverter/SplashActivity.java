package benliu.com.test.currencyconverter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DataHelper.getInstance().fetchCurrencies(null, this, new DBUpdateListener() {
            @Override
            public void updated() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                });
            }
        });

    }
}
