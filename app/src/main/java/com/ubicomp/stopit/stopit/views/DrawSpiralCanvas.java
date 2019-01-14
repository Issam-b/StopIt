package com.ubicomp.stopit.stopit.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ubicomp.stopit.stopit.DrawPathCoordinates;
import com.ubicomp.stopit.stopit.MainActivity;;
import java.util.ArrayList;
import java.util.List;


public class DrawSpiralCanvas extends View {

    private Path path = new Path();
    private Paint brush = new Paint();
    private int counter = 0;
    boolean drawEnable = true;
    private DatabaseReference mDatabase;
    private List<List<Float>> listOrigin = new ArrayList<>();
    private List<List<Float>> listDrawn = new ArrayList<>();
    DrawPathCoordinates drawPathCoordinates = new DrawPathCoordinates();

    public DrawSpiralCanvas(Context context) {
        super(context);
        init(null);
    }

    public DrawSpiralCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(3f);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public DrawSpiralCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {

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
                case MotionEvent.ACTION_UP:
                    performClick();
                    break;
                default:
                    return false;
            }

            listDrawn.add(listItem);
            mDatabase.child("users").child(MainActivity.USERNAME).child("DrawnDots").child("" + (counter - 1)).child("x").setValue(pointX);
            mDatabase.child("users").child(MainActivity.USERNAME).child("DrawnDots").child("" + (counter - 1)).child("y").setValue(pointY);

            Log.d("Drawing", "Counter: " + counter);
            postInvalidate();

            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, brush);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void reset() {
        path.reset();
        counter = 0;
        listDrawn.clear();
        listOrigin.clear();
        drawEnable = true;
        invalidate();
    }

    public void doneDrawing() {
        drawEnable = false;
        mDatabase.child("users").child(MainActivity.USERNAME).child("DotsCount").setValue(counter - 1);
        drawPathCoordinates.getGreyCoordinates(counter-1, listOrigin);
    }
}
