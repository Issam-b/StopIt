package com.ubicomp.stopit.stopit.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    String shape;


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
            mDatabase.child("users")
                    .child(SpiralActivityPresenter.username)
                    .child(shape)
                    .child(String.valueOf(start))
                    .child("drawnDots")
                    .child(String.valueOf(counter))
                    .child("x")
                    .setValue(pointX);
            mDatabase.child("users")
                    .child(SpiralActivityPresenter.username)
                    .child(shape)
                    .child(String.valueOf(start))
                    .child("drawnDots")
                    .child(String.valueOf(counter))
                    .child("y")
                    .setValue(pointY);
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
        start = 0;
        finish = 0;
        listDrawn.clear();
        listOrigin.clear();
        drawEnable = true;
        invalidate();
    }

    public void doneDrawing() {

        if (counter == 0) {
            Toast.makeText(getContext(),"Draw the line first", Toast.LENGTH_SHORT).show();

        } else {
            drawEnable = false;

            // getting list of corresponding dots in the original spiral
            listOrigin = drawPathCoordinates.getGreyCoordinates(counter, shape, start);

            // getting result based on list of drawn dots coordinates
            List<Double> result = drawPathCoordinates.getSpiralResults(listDrawn, counter, start, finish);
            double error = result.get(0);
            double sd = result.get(1);
            double maxError = result.get(2);
            double time = result.get(3);

            // dialog to show results
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Result")
                    .setMessage("Error: " + String.format(Locale.ENGLISH,"%.3f", error) + " px" +
                            "\nSD: " + String.format(Locale.ENGLISH,"%.3f", sd) + " px" +
                            "\nMax error: " + String.format(Locale.ENGLISH,"%.3f", maxError) + " px" +
                            "\nTime: " + time + " sec")
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SpiralActivityPresenter activity = (SpiralActivityPresenter) getContext();
                            activity.finish();
                        }
                    })
                    .show();
        }
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
