package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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


        //mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    public void spiralTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","spiral");
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void shapeTest(View view) {
        if (!et_username.getText().toString().equals(""))
            username = et_username.getText().toString();

        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","square");
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
