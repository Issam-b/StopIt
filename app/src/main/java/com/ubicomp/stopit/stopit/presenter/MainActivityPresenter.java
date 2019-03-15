package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.ubicomp.stopit.stopit.R;

public class MainActivityPresenter extends AppCompatActivity {

    private String username = "empty_name";
    private EditText et_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);

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
