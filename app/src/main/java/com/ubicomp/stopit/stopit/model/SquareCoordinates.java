package com.ubicomp.stopit.stopit.model;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ubicomp.stopit.stopit.presenter.CanvasActivityPresenter;
import com.ubicomp.stopit.stopit.views.DrawCanvas;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SquareCoordinates {

    private DatabaseReference mDatabase;
    private List<List<Float>> drawnDots = new ArrayList<>();
    private final String SQUARE_MODEL_TAG = "STOPIT_Square";

    public SquareCoordinates() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // draws grey line
    public void drawOriginalPath(Path path) {
        float x0 = CanvasActivityPresenter.width / 5f;   // Puts the square in the middle of the screen
        float y0 = CanvasActivityPresenter.height / 3f;
        float x1=x0*4;
        float y1=y0 + x0*3;

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
    public void appendDrawnDot(float pointX, float pointY) {
        List<Float> dot = new ArrayList<>();
        dot.add(pointX);
        dot.add(pointY);
        drawnDots.add(dot);
    }

    public List<List<Float>> getDrawnDots() {
        return drawnDots;
    }

    // save drawn dots coordinates to db
    public void saveDrawnDotsCoordinates(long start) {
        float pointX;
        float pointY;
        List<Float> dot;
        for(int idx = 0; idx < drawnDots.size(); idx++) {
            dot = drawnDots.get(idx);
            pointX = dot.get(0);
            pointY = dot.get(1);

            mDatabase.child("users")
                    .child(CanvasActivityPresenter.username)
                    .child("square")
                    .child(String.valueOf(start))
                    .child("drawnDots")
                    .child(String.valueOf(idx))
                    .child("x")
                    .setValue(pointX);
            mDatabase.child("users")
                    .child(CanvasActivityPresenter.username)
                    .child("square")
                    .child(String.valueOf(start))
                    .child("drawnDots")
                    .child(String.valueOf(idx))
                    .child("y")
                    .setValue(pointY);
        }
    }

    // gets the avg error, max error, sd error and time for the drawing
    public List<Double> getSquareResults(List<List<Float>> drawn, int counter, long start, long finish) {
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
        for (int i=0; i<drawn.size(); i++) {
            float x = drawn.get(i).get(0);
            float y = drawn.get(i).get(1);
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

        // counter record to db
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("square")
                .child(String.valueOf(start))
                .child("counter")
                .setValue(counter);

        // average error calculation and record to db
        double error = errorSum/counter;
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("square")
                .child(String.valueOf(start))
                .child("results")
                .child("error")
                .setValue(Double.valueOf(String.format(Locale.ENGLISH,"%.3f", error)));

        // max error record to db
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("square")
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
                .child("square")
                .child(String.valueOf(start))
                .child("results")
                .child("sd")
                .setValue(Double.valueOf(String.format(Locale.ENGLISH,"%.3f", sd)));

        // time calculation and record to db
        double time = (double) (finish - start)/1000;
        mDatabase.child("users")
                .child(CanvasActivityPresenter.username)
                .child("square")
                .child(String.valueOf(start))
                .child("results")
                .child("time")
                .setValue(time);

        // add all the results to the list for showing it in a view
        List<Double> result = new ArrayList<>();
        result.add(error);
        result.add(sd);
        result.add(errorMax);
        result.add(time);

        return result;
    }

    // saves original and drawn paths image to the db
    public void saveScreenshotToDb(final Bitmap bitmap, final long start) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously().addOnSuccessListener(new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                final StorageReference imageRef = storageRef.child("users")
                        .child(CanvasActivityPresenter.username)
                        .child("square")
                        .child(String.valueOf(start) + ".jpg");

                ByteArrayOutputStream bArrOutStr = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bArrOutStr);
                byte[] data = bArrOutStr.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mDatabase.child("users")
                                        .child(CanvasActivityPresenter.username)
                                        .child("square")
                                        .child(String.valueOf(start))
                                        .child("imageUrl")
                                        .setValue(String.valueOf(uri));

                                DrawCanvas.updateDialog(true);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDatabase.child("users")
                                .child(CanvasActivityPresenter.username)
                                .child("square")
                                .child(String.valueOf(start))
                                .child("imageUrl")
                                .setValue("upload_error");

                        DrawCanvas.updateDialog(false);
                    }
                });
            }
        });
    }
}