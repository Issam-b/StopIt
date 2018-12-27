package com.ubicomp.stopit.stopit;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import com.ubicomp.stopit.stopit.views.DrawSpiralCanvas;
import com.ubicomp.stopit.stopit.views.InitializeBackground;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout;
    DisplayMetrics metrics;
    int width = 0;
    int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        width = metrics.widthPixels;
        final InitializeBackground spiralBackground = new InitializeBackground(this, width, height);

        DrawSpiralCanvas draw = new DrawSpiralCanvas(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            draw.setBackground(spiralBackground.getBackground());
            setContentView(draw);
        }
    }
}
