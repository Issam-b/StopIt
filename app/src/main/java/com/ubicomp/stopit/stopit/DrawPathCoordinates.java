package com.ubicomp.stopit.stopit;

import android.graphics.Path;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;


public class DrawPathCoordinates {

    private DatabaseReference mDatabase;

    public final static double thetaStepSize = 0.1;
    public final static double turnsNumber = 2;
    public final static double turnFull = Math.PI * 2;
    public final static double turnsDistance = 30; // ?? What's the scaling?

    public DrawPathCoordinates() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // draws grey line
    public void drawGreyPath(Path path) {
        float x0 = MainActivity.width / 2;   // Starting point of the spiral
        float y0 = MainActivity.height / 2;  // is always in the middle of the screen
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
    public List<List<Float>> getGreyCoordinates(int size) {
        float x0 = MainActivity.width / 2;   // Starting point of the spiral
        float y0 = MainActivity.height / 2;  // is always in the middle of the screen
        float x;
        float y;
        double theta = 0;
        double thetaStepSize = turnsNumber*turnFull/size;
        List<List<Float>> originalDots = new ArrayList<>();

        for (int i=1; i<=size; i++) {
            x = (float) (turnsDistance * theta * Math.cos(theta) + x0);
            y = (float) (turnsDistance * theta * Math.sin(theta) + y0);
            mDatabase.child("users").child(MainActivity.USERNAME).child("originalDots").child("" + i).child("x").setValue(x);
            mDatabase.child("users").child(MainActivity.USERNAME).child("originalDots").child("" + i).child("y").setValue(y);

            List<Float> listItem = new ArrayList<>();
            listItem.add(x);
            listItem.add(y);
            originalDots.add(listItem);

            theta += thetaStepSize;
        }

        return originalDots;
    }
}
