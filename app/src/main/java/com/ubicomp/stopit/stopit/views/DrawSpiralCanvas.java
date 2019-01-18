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
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ubicomp.stopit.stopit.DrawPathCoordinates;
import com.ubicomp.stopit.stopit.MainActivity;;
import java.util.ArrayList;
import java.util.List;


public class DrawSpiralCanvas extends View {

    // drawing variables
    private Path path = new Path();
    private Paint brush = new Paint();
    private int counter = 0;
    boolean drawEnable = true;

    // path coordinate variables
    DrawPathCoordinates drawPathCoordinates = new DrawPathCoordinates();
    private DatabaseReference mDatabase;
    private List<List<Float>> listOrigin = new ArrayList<>();
    private List<List<Float>> listDrawn = new ArrayList<>();
    private List<Double> listBuffer = new ArrayList<>();
    private List<Double> listAngle = new ArrayList<>();
    double buffer = 0;
    double errorSum = 0;


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
            float x0 = MainActivity.width / 2;
            float y0 = MainActivity.height / 2;
            float pointX = event.getX();
            float pointY = event.getY();
            List<Float> listItem = new ArrayList<>();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    counter++;
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    counter++;
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    return true;
                default:
                    return false;
            }

            listDrawn.add(listItem);
            mDatabase.child("users").child(MainActivity.USERNAME).child("drawnDots").child(String.valueOf(counter)).child("x").setValue(pointX);
            mDatabase.child("users").child(MainActivity.USERNAME).child("drawnDots").child(String.valueOf(counter)).child("y").setValue(pointY);
            postInvalidate();


            // Calculating the error:
            // 1) Calculate R for the drawn dot
            double r = Math.sqrt(Math.pow(pointX-x0, 2) + Math.pow(pointY-y0, 2));

            /* 2) Calculate Θ for the drawn dot
               2.1) Every next Θ should be bigger than previous, hence every value is checked
                    and increased if needed
            */
            double angle = Math.atan((pointY - y0)/(pointX - x0));
            listBuffer.add(angle);          // stores angle values in (-pi/2 ; pi/2)
            listAngle.add(angle);           // stores angle values increasingly
            int i = listBuffer.size();
            if (i>1) {
                if (listBuffer.get(i-1) < listBuffer.get(i-2)) {
                    buffer += Math.PI;
                }
                angle += buffer;            // fixes the value of the angle
                listAngle.set(i-1, angle);  // records it to the list
                Log.d("STOP_TAG", counter + ": Angle: " + angle);
            } else {
                Log.d("STOP_TAG", counter + ": Angle: " + angle);
            }

            // 3) Calculate R0 for the corresponding dot in grey line based on received angle
            double r0 = DrawPathCoordinates.turnsDistance*angle;

            // 4) Find the error for the dot as an absolute value of R and R0
            double error = Math.abs(r - r0);
            errorSum += error;

//            Log.d("STOP_TAG", counter + ": R Orig: "+ r0);
//            Log.d("STOP_TAG", counter + ": R Drawn: "+ r);
//            Log.d("STOP_TAG", counter + ": R Error: "+ error);
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
        buffer = 0;
        errorSum = 0;
        listDrawn.clear();
        listOrigin.clear();
        listAngle.clear();
        listBuffer.clear();
        drawEnable = true;
        invalidate();
    }

    public void doneDrawing() {
        drawEnable = false;
        mDatabase.child("users").child(MainActivity.USERNAME).child("counter").setValue(counter);
        listOrigin = drawPathCoordinates.getGreyCoordinates(counter);

        double error = errorSum/counter;
        String msg = "Error in pixels: " + error;
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        Log.d("STOP_TAG", msg);
    }
}
