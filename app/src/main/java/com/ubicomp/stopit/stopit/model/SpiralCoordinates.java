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

public class SpiralCoordinates {

    //drawing dots saving variables
    private List<List<Object>> drawnDots = new ArrayList<>();
    private JSONObject data = new JSONObject();
    private JSONArray drawn = new JSONArray();
    private JSONArray origin = new JSONArray();
    private JSONObject results = new JSONObject();

    // starting point of the spiral to center of a screen
    private float x0 = CanvasActivityPresenter.width / 2f;
    private float y0 = CanvasActivityPresenter.height / 2f;

    // spiral parameters
    private final static double thetaStepSize = 0.1;
    private final static double turnsNumber = 2;
    private final static double turnFull = Math.PI * 2;
    private final static double turnsDistance = 30; // ?? What's the scaling?


    // draws grey spiral
    public void drawOriginalPath(Path path) {
        float x;
        float y;
        double theta = 0;

        // starting point of spiral
        path.moveTo(x0, y0);

        // drawing spiral
        while (theta < turnFull*turnsNumber) {
            x = (float) (turnsDistance * theta * Math.cos(theta) + x0);
            y = (float) (turnsDistance * theta * Math.sin(theta) + y0);
            path.lineTo(x, y);
            theta += thetaStepSize;
        }
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

    // calculates the coordinates of specified number of dots over the whole spiral
    public void getOriginalDotsCoordinates(int size) {
        float x;
        float y;
        double theta = 0;
        double thetaStepSize = turnsNumber*turnFull/size;

        for (int i=1; i<=size; i++) {
            x = (float) (turnsDistance * theta * Math.cos(theta) + x0);
            y = (float) (turnsDistance * theta * Math.sin(theta) + y0);
            theta += thetaStepSize;

            // JSON
            JSONObject originDot = new JSONObject();
            try {
                originDot.put("x", x);
                originDot.put("y", y);
                origin.put(originDot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // calculates the avg error, max error, sd error and time for the drawing
    public List<Double> getSpiralResults(int counter, long start, long finish) {
        List<Double> listAngle = new ArrayList<>();
        List<Double> listError = new ArrayList<>();
        double buffer = 0;
        double errorSum = 0;
        double errorMax = 0;
        double sdSum = 0;

        for (int i=0; i<drawnDots.size(); i++) {
            float x = (float) drawnDots.get(i).get(1);
            float y = (float) drawnDots.get(i).get(2);

            // Calculating the error for every dot one by one:
            // 1) Calculate R for the drawn dot
            double r = Math.sqrt(Math.pow(x-x0, 2) + Math.pow(y-y0, 2));

            /* 2) Calculate Θ for the drawn dot
               2.1) Every next Θ should be bigger than previous, hence every value is checked
                    and increased if needed
            */
            double angle = Math.atan((y - y0)/(x - x0));
            listAngle.add(angle);           // stores angle values
            int j = listAngle.size();
            if (j>1) {
                if (listAngle.get(j-2) - listAngle.get(j-1) > Math.PI/2) {
                    buffer += Math.PI;
                }
                angle += buffer;            // fixes the value of the angle
            }

            // 3) Calculate R0 for the corresponding dot in grey line based on received angle
            double r0 = SpiralCoordinates.turnsDistance*angle;
            if (angle < 0) r0 = 0;          // for cases when the initial angle is negative

            // 4) Find the error for the dot as an absolute value of R and R0
            double error = Math.abs(r - r0);
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

        // time calculation
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
            data.put("game_type", "spiral");
            data.put("device_x_res", CanvasActivityPresenter.width);
            data.put("device_y_res", CanvasActivityPresenter.height);
            data.put("counter", counter);
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
