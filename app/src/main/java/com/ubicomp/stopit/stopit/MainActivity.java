package com.ubicomp.stopit.stopit;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ubicomp.stopit.stopit.views.DrawSpiralCanvas;
import com.ubicomp.stopit.stopit.views.InitializeBackground;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout;
    Canvas canvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final InitializeBackground spiralBackground = new InitializeBackground(this, 600, 800);

        DrawSpiralCanvas draw = new DrawSpiralCanvas(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            draw.setBackground(spiralBackground.getBackground());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

        setContentView(draw);
        }



    }}
