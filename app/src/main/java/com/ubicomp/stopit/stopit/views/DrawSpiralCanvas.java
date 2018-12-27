package com.ubicomp.stopit.stopit.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.ubicomp.stopit.stopit.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class DrawSpiralCanvas extends View {

    public LayoutParams params;
    public Path path = new Path();
    public Paint brush = new Paint();
    int counter = 0;
    boolean drawEnable = true;
    boolean stopClicked = false;
    public List<List<Float>> listOrigin = new ArrayList<>();
    public List<List<Float>> listDrawn = new ArrayList<>();

    public DrawSpiralCanvas(Context context) {
        super(context);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(3f);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(drawEnable) {
            float pointX = event.getX();
            float pointY = event.getY();
            List<Float> listItem = new ArrayList<>();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    Log.d("Drawing", "Down X: " + pointX + " Y: " + pointY);
                    counter++;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    Log.d("Drawing", "Move X: " + pointX + " Y: " + pointY);
                    counter++;
                    break;
                default:
                    return false;
            }
            listDrawn.add(listItem);

            Log.d("Drawing", "Counter: " + counter);
            postInvalidate();

            if(stopClicked) {
                drawEnable = false;
                double thetaStepSize = InitializeBackground.thetaStepSize * 90 * InitializeBackground.nbrTurns / counter;
                InitializeBackground.getPath(counter, thetaStepSize, listOrigin, false);
            }

            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, brush);
    }
}
