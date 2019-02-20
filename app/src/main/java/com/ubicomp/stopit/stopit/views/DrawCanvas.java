package com.ubicomp.stopit.stopit.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ubicomp.stopit.stopit.R;
import com.ubicomp.stopit.stopit.model.SpiralCoordinates;
import com.ubicomp.stopit.stopit.model.SquareCoordinates;
import com.ubicomp.stopit.stopit.presenter.CanvasActivityPresenter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DrawCanvas extends View {

    // drawing variables
    private Path path = new Path();
    private Paint brush = new Paint();
    private boolean drawEnable = true;
    private int counter = 0;
    private long start = 0;
    private long finish = 0;

    // path coordinate variables
    private SpiralCoordinates spiralCoordinates;
    private SquareCoordinates squareCoordinates;
    private String shape;

    // result dialog variables
    private static String dialogMessage;
    private static AlertDialog dialog;
    private static View progress;

    public void setShape(String input) {
        shape = input;
        if (input.equals("spiral")) spiralCoordinates = new SpiralCoordinates();
        if (input.equals("square")) squareCoordinates = new SquareCoordinates();
    }

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
                    if (shape.equals("spiral")) spiralCoordinates.appendDrawnDot(pointX, pointY);
                    if (shape.equals("square")) squareCoordinates.appendDrawnDot(pointX, pointY);
                    counter++;
                    if (start == 0) start = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(pointX, pointY);
                    if (shape.equals("spiral")) spiralCoordinates.appendDrawnDot(pointX, pointY);
                    if (shape.equals("square")) squareCoordinates.appendDrawnDot(pointX, pointY);
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

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void doneDrawing() {

        if (counter == 0) {
            Toast.makeText(getContext(),"Draw the line first", Toast.LENGTH_SHORT).show();

        } else {
            drawEnable = false;
            List<Double> result = new ArrayList<>();

            // save drawn and original dots coordinates and calculate the results
            if (shape.equals("spiral")) {
                spiralCoordinates.saveDrawnDotsCoordinates(start);
                spiralCoordinates.saveOriginalDotsCoordinates(counter, start);
                spiralCoordinates.saveScreenshotToDb(screenShot(this), start);
                result = spiralCoordinates.getSpiralResults(spiralCoordinates.getDrawnDots(), counter, start, finish);
            }

            // save drawn dots coordinates and calculate the results
            if (shape.equals("square")) {
                squareCoordinates.saveDrawnDotsCoordinates(start);
                squareCoordinates.saveScreenshotToDb(screenShot(this), start);
                result = squareCoordinates.getSquareResults(squareCoordinates.getDrawnDots(), counter, start, finish);
            }

            // getting result based on list of drawn dots coordinates
            double error = result.get(0);
            double sd = result.get(1);
            double maxError = result.get(2);
            double time = result.get(3);

            // setting up dialog components
            progress = LayoutInflater.from(getContext()).inflate(R.layout.result_progress_layout, null);
            dialogMessage = "Error: " + String.format(Locale.ENGLISH,"%.3f", error) + " px" +
                    "\nSD: " + String.format(Locale.ENGLISH,"%.3f", sd) + " px" +
                    "\nMax error: " + String.format(Locale.ENGLISH,"%.3f", maxError) + " px" +
                    "\nTime: " + time + " sec";

            // dialog to show results
            dialog = new AlertDialog.Builder(getContext()).create();
            dialog.setTitle("Result");
            dialog.setView(progress);
            dialog.setMessage(dialogMessage);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CanvasActivityPresenter activity = (CanvasActivityPresenter) getContext() ;
                    activity.finish();
                }
            });
            dialog.show();
        }
    }

    // update dialog when a screenshot is uploaded
    public static void updateDialog(boolean success) {
        if (success) dialogMessage += "\n\nResults have been saved";
        else dialogMessage += "\n\nResults save error. Please complete a test again";

        progress.setVisibility(GONE);
        dialog.setMessage(dialogMessage);
        dialog.show();
    }
}
