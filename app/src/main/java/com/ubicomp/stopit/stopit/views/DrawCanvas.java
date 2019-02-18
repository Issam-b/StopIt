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
import com.ubicomp.stopit.stopit.model.SpiralCoordinates;
import com.ubicomp.stopit.stopit.presenter.CanvasActivityPresenter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DrawCanvas extends View {

    // drawing variables
    private Path path = new Path();
    private Paint brush = new Paint();
    private int counter = 0;
    boolean drawEnable = true;
    long start = 0;
    long finish = 0;

    // path coordinate variables
    SpiralCoordinates spiralCoordinates = new SpiralCoordinates();

    String shape;


    public DrawCanvas(Context context) {
        super(context);
        init(null);
    }

    public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(3);
    }

    public DrawCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(pointX, pointY);
                    spiralCoordinates.appendDrawnDot(pointX, pointY);
                    counter++;
                    if (start == 0) start = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(pointX, pointY);
                    spiralCoordinates.appendDrawnDot(pointX, pointY);
                    counter++;
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    finish = System.currentTimeMillis();
                    return true;
                default:
                    return false;
            }
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
        drawEnable = true;
        invalidate();
    }

    public void doneDrawing() {

        if (counter == 0) {
            Toast.makeText(getContext(),"Draw the line first", Toast.LENGTH_SHORT).show();

        } else {
            drawEnable = false;

            // store drawn dots coordinates to db
            spiralCoordinates.storeDrawnDotsCoordinates(shape, start);

            // getting list of corresponding dots in the original spiral
            List<List<Float>> greyDots = new ArrayList<>(); // FIXME: should't be needed to return this?
            greyDots = spiralCoordinates.getGreyCoordinates(counter, shape, start);

            // getting result based on list of drawn dots coordinates
            List<Double> result = spiralCoordinates.getSpiralResults(spiralCoordinates.getDrawnDots(), counter, start, finish, shape);
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
                            CanvasActivityPresenter activity = (CanvasActivityPresenter) getContext();
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
