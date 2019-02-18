package com.ubicomp.stopit.stopit.presenter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import com.ubicomp.stopit.stopit.R;
import com.ubicomp.stopit.stopit.views.DrawCanvas;
import com.ubicomp.stopit.stopit.views.InitializeBackground;


public class CanvasActivityPresenter extends AppCompatActivity {

    DisplayMetrics metrics;
    static public int width = 0;
    static public int height = 0;
    public static String username;
    static public DrawCanvas drawCanvas;
    InitializeBackground spiralBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String initializeBackground = intent.getStringExtra("background");
        username = intent.getStringExtra("username");
        setContentView(R.layout.canvas_activity);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;

        final Button resetButton = findViewById(R.id.resetButton);
        final Button doneButton = findViewById(R.id.doneButton);

        spiralBackground = new InitializeBackground(this,initializeBackground);
        drawCanvas = findViewById(R.id.drawCanvas);
        drawCanvas.setShape(initializeBackground);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            drawCanvas.setBackground(spiralBackground.getBackground());
        }

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawCanvas.reset();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawCanvas.doneDrawing();
            }
        });
    }
}
