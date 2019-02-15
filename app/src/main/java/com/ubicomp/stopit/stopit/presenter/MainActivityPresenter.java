package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.ubicomp.stopit.stopit.R;

public class MainActivityPresenter extends AppCompatActivity {

    private DatabaseReference mDatabase;
    static public int USERS_COUNT;
    static public String USERNAME;
    private String username = "empty_name";
    private EditText et_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException exception) {
            Log.d("StopIt", "Couldn't hide bar title");
        }

        et_username = findViewById(R.id.usernameText);
    }

    public void spiralTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this, CanvasActivityPresenter.class);
        intent.putExtra("background","spiral");
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void shapeTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this, CanvasActivityPresenter.class);
        intent.putExtra("background","square");
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
