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


    // draws grey square
    public void drawOriginalPath(Path path) {
        float x0 = CanvasActivityPresenter.width / 5f;
        float y0 = CanvasActivityPresenter.height / 3f;
        float x1 = x0*4;
        float y1 = y0 + x0*3;

        path.moveTo(x0,y0);
        path.lineTo(x1,y0);
        path.moveTo(x1,y0);
        path.lineTo(x1,y1);
        path.moveTo(x1,y1);
        path.lineTo(x0,y1);
        path.moveTo(x0,y1);
        path.lineTo(x0,y0);
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
    public void getDrawnDotsCoordinates(long start) {
        List<Object> dot;
        for(int idx = 0; idx < drawnDots.size(); idx++) {
            dot = drawnDots.get(idx);

            // JSON
            JSONObject drawnDot = new JSONObject();
            try {
                drawnDot.put("timestamp", dot.get(0));
                drawnDot.put("x", dot.get(1));
                drawnDot.put("y", dot.get(2));
                drawn.put(drawnDot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // gets the avg error, max error, sd error and time for the drawing
    public List<Double> getSquareResults(int counter, long start, long finish) {

        // variables for error calculation based on screen resolution
        float xMax = CanvasActivityPresenter.width;
        float yMax = CanvasActivityPresenter.height;
        float x1 = xMax/5f;
        float x2 = xMax*0.8f;
        float y1 = yMax/3f;
        float y2 = yMax/3f + xMax*0.6f;
        float mid1 = yMax/3f - xMax*0.2f;
        float mid2 = yMax/3f + xMax*0.8f;

        double errorSum = 0;
        double errorMax = 0;
        double sdSum = 0;
        List<Double> listError = new ArrayList<>();

        // Calculating the error for every dot one by one
        for (int i=0; i<drawnDots.size(); i++) {
            float x = (float) drawnDots.get(i).get(1);
            float y = (float) drawnDots.get(i).get(2);
            double error=0;

            // depending on a dot location
            if ((0<=x) && (x<x1)) {
                if ((0<=y) && (y<y1)) error = Math.sqrt(Math.pow((y1 - y),2) + Math.pow((x1 - x),2));
                if ((y1<=y) && (y<=y2)) error = x1 - x;
                if ((y2< y) && (y<=yMax)) error = Math.sqrt(Math.pow((y2 - y),2) + Math.pow((x1 - x),2));
            }

            if ((x1<=x) && (x<=x2)) {
                if ((0<=y) && (y<=y1)) error = y1 - y;
                if ((y2<=y) && (y<=yMax)) error = y - y2;
                if ((y1<y) && (y<y2)) {
                    if (((y-x) <= mid1) && ((y+x) < mid2)) error = y - y1;
                    if (((y-x) < mid1) && ((y+x) >= mid2)) error = x2 - x;
                    if (((y-x) >= mid1) && ((y+x) > mid2)) error = y2 - y;
                    if (((y-x) > mid1) && ((y+x) <= mid2)) error = x - x1;
                }
            }

            if ((x2<x) && (x<=xMax)) {
                if ((0<=y) && (y<y1)) error = Math.sqrt(Math.pow((y1 - y),2) + Math.pow((x2 - x),2));
                if ((y1<=y) && (y<=y2)) error = x - x2;
                if ((y2<y) && (y<=yMax)) error = Math.sqrt(Math.pow((y2 - y),2) + Math.pow((x2 - x),2));
            }

            listError.add(error);
            errorSum += error;
            if (error>errorMax) errorMax = error;
        }

        // average error calculation
        double error = errorSum/counter;

        // standard deviation calculation
        for (int i=0; i<counter; i++) {
            sdSum += Math.pow(listError.get(i) - error, 2);
        }
        double sd = Math.sqrt(sdSum/counter);

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
    public void saveData(Context context, long start, int counter) {

        try {
            data.put("userame", CanvasActivityPresenter.username);
            data.put("game_type", "square");
            data.put("device_x_res", CanvasActivityPresenter.width);
            data.put("device_y_res", CanvasActivityPresenter.height);
            data.put("counter", counter);
            data.put("results", results);
            data.put("dots_drawn", drawn);
//            data.put("dots_original", origin);
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
