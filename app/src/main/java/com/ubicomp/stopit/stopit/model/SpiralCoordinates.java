package com.ubicomp.stopit.stopit.model;

import android.graphics.Path;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ubicomp.stopit.stopit.presenter.CanvasActivityPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SpiralCoordinates {

    private DatabaseReference mDatabase;

    private final static double thetaStepSize = 0.1;
    private final static double turnsNumber = 2;
    private final static double turnFull = Math.PI * 2;
    public final static double turnsDistance = 30; // ?? What's the scaling?

    public SpiralCoordinates() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // draws grey line
    public void drawGreyPath(Path path) {
        float x0 = CanvasActivityPresenter.width / 2f;   // Starting point of the spiral
        float y0 = CanvasActivityPresenter.height / 2f;  // is always in the middle of the screen
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

    // finds the coordinates of specified number of dots over the whole spiral
    public List<List<Float>> getGreyCoordinates(int size, String shape, long start) {
        float x0 = CanvasActivityPresenter.width / 2f;   // Starting point of the spiral
        float y0 = CanvasActivityPresenter.height / 2f;  // is always in the middle of the screen
        float x;
        float y;
        double theta = 0;
        double thetaStepSize = turnsNumber*turnFull/size;
        List<List<Float>> originalDots = new ArrayList<>();

        for (int i=1; i<=size; i++) {
            x = (float) (turnsDistance * theta * Math.cos(theta) + x0);
            y = (float) (turnsDistance * theta * Math.sin(theta) + y0);
            mDatabase.child("users")
                    .child(CanvasActivityPresenter.username)
                    .child(shape)
                    .child(String.valueOf(start))
                    .child("originalDots")
                    .child(String.valueOf(i))
                    .child("x")
                    .setValue(x);
            mDatabase.child("users")
                    .child(CanvasActivityPresenter.username)
                    .child(shape)
                    .child(String.valueOf(start))
                    .child("originalDots")
                    .child(String.valueOf(i))
                    .child("y")
                    .setValue(y);

            List<Float> listItem = new ArrayList<>();
            listItem.add(x);
            listItem.add(y);
            originalDots.add(listItem);

            theta += thetaStepSize;
        }

        return originalDots;
    }

    public List<Double> getSpiralResults(List<List<Float>> drawn, int counter, long start, long finish) {
        float x0 = CanvasActivityPresenter.width / 2f;
        float y0 = CanvasActivityPresenter.height / 2f;
        List<Double> listAngle = new ArrayList<>();
        List<Double> listError = new ArrayList<>();
        double buffer = 0;
        double errorSum = 0;
        double errorMax = 0;
        double sdSum = 0;

        for (int i=0; i<drawn.size(); i++) {
            float x = drawn.get(i).get(0);
            float y = drawn.get(i).get(1);

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
                if (listAngle.get(j-1) < listAngle.get(j-2)) {
                    buffer += Math.PI;
                }
                angle += buffer;            // fixes the value of the angle
            }

            // 3) Calculate R0 for the corresponding dot in grey line based on received angle
            double r0 = SpiralCoordinates.turnsDistance*angle;

            // 4) Find the error for the dot as an absolute value of R and R0
            double error = Math.abs(r - r0);
            listError.add(error);
            errorSum += error;
            if (error>errorMax) errorMax = error;
        }



        // counter record to db
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("spiral")
                .child(String.valueOf(start))
                .child("counter")
                .setValue(counter);

        // average error calculation and record to db
        double error = errorSum/counter;
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("spiral")
                .child(String.valueOf(start))
                .child("results")
                .child("error")
                .setValue(Double.valueOf(String.format(Locale.ENGLISH,"%.3f", error)));

        // max error record to db
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("spiral")
                .child(String.valueOf(start))
                .child("results")
                .child("errorMax")
                .setValue(Double.valueOf(String.format(Locale.ENGLISH,"%.3f", errorMax)));

        // standard deviation calculation and record to db
        for (int i=0; i<counter; i++) {
            sdSum += Math.pow(listError.get(i) - error, 2);
        }
        double sd = Math.sqrt(sdSum/counter);
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("spiral")
                .child(String.valueOf(start))
                .child("results")
                .child("sd")
                .setValue(Double.valueOf(String.format(Locale.ENGLISH,"%.3f", sd)));

        // time calculation and record to db
        double time = (double) (finish - start)/1000;
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("spiral")
                .child(String.valueOf(start))
                .child("results")
                .child("time")
                .setValue(time);

        List<Double> result = new ArrayList<>();
        result.add(error);
        result.add(sd);
        result.add(errorMax);
        result.add(time);

        return result;
    }
}
