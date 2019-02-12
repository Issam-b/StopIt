package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ubicomp.stopit.stopit.R;

public class MainActivityPresenter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_presenter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void spiralTest(View view) {
        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","spiral");
        startActivity(intent);
    }

    public void shapeTest(View view) {

        Intent intent= new Intent(this,SpiralActivityPresenter.class);
        intent.putExtra("background","square");
        startActivity(intent);
    }
}
