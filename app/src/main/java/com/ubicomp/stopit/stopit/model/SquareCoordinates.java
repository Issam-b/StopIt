package com.ubicomp.stopit.stopit.model;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.util.Base64;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.ubicomp.stopit.stopit.presenter.CanvasActivityPresenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SquareCoordinates {

    //drawing dots saving variables
    private List<List<Object>> drawnDots = new ArrayList<>();
    private JSONObject data = new JSONObject();
    private JSONArray drawn = new JSONArray();
    private JSONArray origin = new JSONArray();
    private JSONObject results = new JSONObject();

    // coordinates for square path and coordinate calculations
    private float xMax = CanvasActivityPresenter.width;
    private float yMax = CanvasActivityPresenter.height;
    private float x1 = xMax/5f;
    private float x2 = xMax*0.8f;
    private float y1 = yMax/3f;
    private float y2 = yMax/3f + xMax*0.6f;
    private float mid1 = yMax/3f - xMax*0.2f;
    private float mid2 = yMax/3f + xMax*0.8f;


    // draws grey square
    public void drawOriginalPath(Path path) {
        path.moveTo(x1,y1);
        path.lineTo(x2,y1);
        path.moveTo(x2,y1);
        path.lineTo(x2,y2);
        path.moveTo(x2,y2);
        path.lineTo(x1,y2);
        path.moveTo(x1,y2);
        path.lineTo(x1,y1);
    }

    // store drawn dots in a buffer list
    public void appendDrawnDot(long time, float pointX, float pointY) {
        List<Object> dot = new ArrayList<>();
        dot.add(time);
        dot.add(pointX);
        dot.add(pointY);
        drawnDots.add(dot);
    }

    // reset values on "reset" click
    public void resetCalculation() {
        drawnDots = new ArrayList<>();
        data = new JSONObject();
        drawn = new JSONArray();
        origin = new JSONArray();
        results = new JSONObject();
    }

    // transform list of drawn dots to JSON array
    public void getDrawnDotsCoordinates() {
        List<Object> dot;
        for(int i = 1; i <= drawnDots.size(); i++) {
            dot = drawnDots.get(i-1);

            // JSON
            JSONObject drawnDot = new JSONObject();
            try {
                drawnDot.put("id", i);
                drawnDot.put("timestamp", dot.get(0));
                drawnDot.put("x", dot.get(1));
                drawnDot.put("y", dot.get(2));
                drawn.put(drawnDot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // calculates the coordinates of original dots in the path
    public void getOriginalDotsCoordinates() {
        float xDot, yDot, xOrig, yOrig;

        for (int i=1; i<=drawnDots.size(); i++) {
            xDot = (float) drawnDots.get(i-1).get(1);
            yDot = (float) drawnDots.get(i-1).get(2);
            xOrig = xDot;
            yOrig = yDot;

            // detecting original coordinates based on drawn dot location
            // schema at imgur.com/nqnEdaz.png
            if ((0<=xDot) && (xDot<x1)) {
                xOrig = x1;                                                         // schema block #2
                if ((0<=yDot) && (yDot<y1)) yOrig = y1;                             // #1
                if ((y2< yDot) && (yDot<=yMax)) yOrig = y2;                         // #3
            }

            if ((x1<=xDot) && (xDot<=x2)) {
                if ((0<=yDot) && (yDot<=y1)) yOrig = y1;                            // #7
                if ((y2<=yDot) && (yDot<=yMax)) yOrig = y2;                         // #8
                if ((y1<yDot) && (yDot<y2)) {
                    if (((yDot-xDot) <= mid1) && ((yDot+xDot) < mid2)) yOrig = y1;  // #9
                    if (((yDot-xDot) < mid1) && ((yDot+xDot) >= mid2)) xOrig = x2;  // #10
                    if (((yDot-xDot) >= mid1) && ((yDot+xDot) > mid2)) yOrig = y2;  // #11
                    if (((yDot-xDot) > mid1) && ((yDot+xDot) <= mid2)) xOrig = x1;  // #12
                }
            }

            if ((x2<xDot) && (xDot<=xMax)) {
                xOrig = x2;                                                         // #5
                if ((0<=yDot) && (yDot<y1)) yOrig = y1;                             // #4
                if ((y2<yDot) && (yDot<=yMax)) yOrig = y2;                          // #6
            }

            // JSON
            JSONObject originDot = new JSONObject();
            try {
                originDot.put("id", i);
                originDot.put("x", xOrig);
                originDot.put("y", yOrig);
                origin.put(originDot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // gets the avg error, max error, sd error and time for the drawing
    public List<Double> getSquareResults(long start, long finish) {
        double errorSum = 0;
        double errorMax = 0;
        double sdSum = 0;
        List<Double> listError = new ArrayList<>();

        // Calculating the error for every dot one by one
        for (int i=0; i<drawnDots.size(); i++) {
            float x = (float) drawnDots.get(i).get(1);
            float y = (float) drawnDots.get(i).get(2);
            double error=0;

            // detecting error based on drawn dot location
            // schema at imgur.com/nqnEdaz.png
            if ((0<=x) && (x<x1)) {
                if ((0<=y) && (y<y1)) error = Math.sqrt(Math.pow((y1 - y),2) + Math.pow((x1 - x),2));       // schema block #1
                if ((y1<=y) && (y<=y2)) error = x1 - x;                                                     // #2
                if ((y2< y) && (y<=yMax)) error = Math.sqrt(Math.pow((y2 - y),2) + Math.pow((x1 - x),2));   // #3
            }

            if ((x1<=x) && (x<=x2)) {
                if ((0<=y) && (y<=y1)) error = y1 - y;                                                      // #7
                if ((y2<=y) && (y<=yMax)) error = y - y2;                                                   // #8
                if ((y1<y) && (y<y2)) {
                    if (((y-x) <= mid1) && ((y+x) < mid2)) error = y - y1;                                  // #9
                    if (((y-x) < mid1) && ((y+x) >= mid2)) error = x2 - x;                                  // #10
                    if (((y-x) >= mid1) && ((y+x) > mid2)) error = y2 - y;                                  // #11
                    if (((y-x) > mid1) && ((y+x) <= mid2)) error = x - x1;                                  // #12
                }
            }

            if ((x2<x) && (x<=xMax)) {
                if ((0<=y) && (y<y1)) error = Math.sqrt(Math.pow((y1 - y),2) + Math.pow((x2 - x),2));       // #4
                if ((y1<=y) && (y<=y2)) error = x - x2;                                                     // #5
                if ((y2<y) && (y<=yMax)) error = Math.sqrt(Math.pow((y2 - y),2) + Math.pow((x2 - x),2));    // #6
            }

            listError.add(error);
            errorSum += error;
            if (error>errorMax) errorMax = error;
        }

        // average error calculation
        double error = errorSum/drawnDots.size();

        // standard deviation calculation
        for (int i=0; i<drawnDots.size(); i++) {
            sdSum += Math.pow(listError.get(i) - error, 2);
        }
        double sd = Math.sqrt(sdSum/drawnDots.size());

        // time calculation and record to db
        double time = (double) (finish - start)/1000;

        // JSON
        try {
            results.put("error", Double.valueOf(String.format(Locale.ENGLISH,"%.3f", error)));
            results.put("error_max", Double.valueOf(String.format(Locale.ENGLISH,"%.3f", errorMax)));
            results.put("sd", Double.valueOf(String.format(Locale.ENGLISH,"%.3f", sd)));
            results.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // add all the results to the list for showing it in a view
        List<Double> result = new ArrayList<>();
        result.add(error);
        result.add(errorMax);
        result.add(sd);
        result.add(time);

        return result;
    }

    // saves drawing data to the db
    public void saveData(Context context, long start) {

        try {
            data.put("userame", CanvasActivityPresenter.username);
            data.put("game_type", "square");
            data.put("device_x_res", CanvasActivityPresenter.width);
            data.put("device_y_res", CanvasActivityPresenter.height);
            data.put("counter", drawnDots.size());
            data.put("results", results);
            data.put("dots_drawn", drawn);
            data.put("dots_original", origin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Inserting data to database
        ContentValues values = new ContentValues();
        values.put(Provider.Drawing_Data.TIMESTAMP, start);
        values.put(Provider.Drawing_Data.DEVICE_ID, Aware.getSetting(context, Aware_Preferences.DEVICE_ID));
        values.put(Provider.Drawing_Data.DATA, String.valueOf(data));
        context.getContentResolver().insert(Provider.Drawing_Data.CONTENT_URI, values);
    }

    // save screenshot to the db
    public void saveScreenshot(Context context, final Bitmap bitmap, final long start) {

        // encodes the image to a string
        ByteArrayOutputStream bArrOutStr = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bArrOutStr);
        byte[] data = bArrOutStr.toByteArray();
        String image_str = Base64.encodeToString(data, Base64.DEFAULT);

        // Inserting data to database
        ContentValues values = new ContentValues();
        values.put(Provider.Screenshot_Data.TIMESTAMP, start);
        values.put(Provider.Screenshot_Data.DEVICE_ID, Aware.getSetting(context, Aware_Preferences.DEVICE_ID));
        values.put(Provider.Screenshot_Data.IMAGE, image_str);
        context.getContentResolver().insert(Provider.Screenshot_Data.CONTENT_URI, values);
    }
}
