package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.ubicomp.stopit.stopit.R;
import com.ubicomp.stopit.stopit.views.DrawSpiralCanvas;
import com.ubicomp.stopit.stopit.views.InitializeBackground;


public class SpiralActivityPresenter extends AppCompatActivity {

    DisplayMetrics metrics;
    static public int width = 0;
    static public int height = 0;
    static public String USERNAME = "user1";
    static public DrawSpiralCanvas drawSpiralCanvas;
    InitializeBackground spiralBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent= getIntent();
        setContentView(R.layout.spiral_activity);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels - 400;    // what is 400?
        width = metrics.widthPixels;

        final Button resetButton = findViewById(R.id.resetButton);
        final Button doneButton = findViewById(R.id.doneButton);

        spiralBackground = new InitializeBackground(this,"triangle");
        drawSpiralCanvas = findViewById(R.id.drawSpiralCanvas);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            drawSpiralCanvas.setBackground(spiralBackground.getBackground());
        }

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawSpiralCanvas.reset();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawSpiralCanvas.doneDrawing();
            }
        });
    }
}
