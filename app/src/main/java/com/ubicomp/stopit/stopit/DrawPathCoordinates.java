package com.ubicomp.stopit.stopit;

import android.graphics.Path;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;


public class DrawPathCoordinates {

    static public DatabaseReference mDatabase;

    public DrawPathCoordinates() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void getCoordinateWithPath(int size, double thetaStepSize, List<List<Float>> list, Path path, boolean drawPath) {
        float x = MainActivity.width / 2;
        float y = MainActivity.height / 2;
        double theta;
        List<Float> listItem = new ArrayList<>();

        if(drawPath)
            path.moveTo(x, y);

        for (int i = 0; i < size; i++) {
            listItem.clear();
            theta = thetaStepSize * i;
            x = (float) (2 * (1 + theta) * Math.cos(theta) + x);
            y = (float) (2 * (1 + theta) * Math.sin(theta) + y);
            Log.d("Numbers", "X: " + x + " Y:" + y);
            if(drawPath) {
                path.lineTo(x, y);
            } else {
                mDatabase.child("users").child(MainActivity.USERNAME).child("originalDots").child("" + i + 1).child("x").setValue(x);
                mDatabase.child("users").child(MainActivity.USERNAME).child("originalDots").child("" + i + 1).child("y").setValue(y);
            }
            listItem.add(x);
            listItem.add(y);
            list.add(listItem);
        }
    }

    public void getPath(int size, double thetaStepSize, List<List<Float>> list, Path path) {
        getCoordinateWithPath(size, thetaStepSize, list, path, true);
    }

    public void getCoordinates(int size, double thetaStepSize, List<List<Float>> list) {
        getCoordinateWithPath(size, thetaStepSize, list, null, false);
    }
}
