package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.ubicomp.stopit.stopit.R;

public class MainActivityPresenter extends AppCompatActivity {

    private String username = "empty_name";
    private EditText et_username;

    private static final String PACKAGE_NAME = "com.ubicomp.stopit.stopit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);

        // Setting up Aware preferences
        Aware.isBatteryOptimizationIgnored(getApplicationContext(), PACKAGE_NAME);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.DEBUG_DB_SLOW, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.WEBSERVICE_SILENT, true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        et_username = findViewById(R.id.usernameText);
    }

    public void spiralTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this, CanvasActivityPresenter.class);
        intent.putExtra("shape","spiral");
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void shapeTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this, CanvasActivityPresenter.class);
        intent.putExtra("shape","square");
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
