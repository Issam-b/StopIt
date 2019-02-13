package com.ubicomp.stopit.stopit.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ubicomp.stopit.stopit.model.DrawPathCoordinates;
import com.ubicomp.stopit.stopit.presenter.SpiralActivityPresenter;;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DrawSpiralCanvas extends View {

    // drawing variables
    private Path path = new Path();
    private Paint brush = new Paint();
    private int counter = 0;
    boolean drawEnable = true;
    long start = 0;
    long finish = 0;

    // path coordinate variables
    DrawPathCoordinates drawPathCoordinates = new DrawPathCoordinates();
    private DatabaseReference mDatabase;
    private List<List<Float>> listOrigin = new ArrayList<>();
    private List<List<Float>> listDrawn = new ArrayList<>();
    private List<Double> listBuffer = new ArrayList<>();
    private List<Double> listAngle = new ArrayList<>();
    private List<Double> listError = new ArrayList<>();
    double buffer = 0;
    double errorSum = 0;
    double errorMax = 0;
    double sd = 0;
    double sdSum = 0;


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
            float x0 = SpiralActivityPresenter.width / 2f;
            float y0 = SpiralActivityPresenter.height / 2f;
            float pointX = event.getX();
            float pointY = event.getY();
            List<Float> listItem = new ArrayList<>();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    counter++;
                    if (start==0) start = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(pointX, pointY);
                    listItem.add(pointX);
                    listItem.add(pointY);
                    counter++;
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    finish = System.currentTimeMillis();
                    return true;
                default:
                    return false;
            }

            listDrawn.add(listItem);
            mDatabase.child("users").child(SpiralActivityPresenter.USERNAME).child("drawnDots").child(String.valueOf(counter)).child("x").setValue(pointX);
            mDatabase.child("users").child(SpiralActivityPresenter.USERNAME).child("drawnDots").child(String.valueOf(counter)).child("y").setValue(pointY);
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
            }

            // 3) Calculate R0 for the corresponding dot in grey line based on received angle
            double r0 = DrawPathCoordinates.turnsDistance*angle;

            // 4) Find the error for the dot as an absolute value of R and R0
            double error = Math.abs(r - r0);
            listError.add(error);
            errorSum += error;
            if (error>errorMax) errorMax = error;

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
        start = 0;
        finish = 0;
        errorSum = 0;
        sd = 0;
        sdSum = 0;
        errorMax = 0;
        listDrawn.clear();
        listOrigin.clear();
        listAngle.clear();
        listBuffer.clear();
        listError.clear();
        drawEnable = true;
        invalidate();
    }

    public void doneDrawing() {

        if (counter == 0) {
            Toast.makeText(getContext(),"Draw the line first", Toast.LENGTH_SHORT).show();

        } else {
            drawEnable = false;
            mDatabase.child("users").child(SpiralActivityPresenter.USERNAME).child("counter").setValue(counter);
            listOrigin = drawPathCoordinates.getGreyCoordinates(counter);

            // error calculation
            double error = errorSum/counter;
            double time = (double) (finish - start)/1000;

            // try error calculations with dots, not angle
            double error2, errorSum2 = 0;
            for (int i=0; i<counter; i++) {
                error2 = Math.sqrt(Math.pow(listDrawn.get(i).get(0) - listOrigin.get(i).get(0), 2) +
                        Math.pow(listDrawn.get(i).get(1) - listOrigin.get(i).get(1), 2));
                errorSum2 += error2;
            }
            error2 = errorSum2/counter;

            // standard deviation calculation
            for (int i=0; i<counter; i++) {
                sdSum += Math.pow(listError.get(i) - error, 2);
            }
            sd = Math.sqrt(sdSum/counter);

            // dialog to show results
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Result")
                    .setMessage("Error in px with dots: " + String.format(Locale.ENGLISH, "%.3f", error2) +
                            "\nError in px with angle: " + String.format(Locale.ENGLISH,"%.3f", error)  +
                            "\nSD: " + String.format(Locale.ENGLISH,"%.3f", sd) +
                            "\nMax error: " + String.format(Locale.ENGLISH,"%.3f", errorMax) +
                            "\nTime: " + time + " sec")
                    .setNegativeButton("Dismiss", null)
                    .show();
        }
    }
}
