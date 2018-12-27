package com.ubicomp.stopit.stopit.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ubicomp.stopit.stopit.R;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.getString;

public class InitializeBackground extends View {

    public ViewGroup.LayoutParams params;
    static private Path path = new Path();
    private Paint brush = new Paint();
    public VectorDrawable vector;
    public List<List<Float>> list = new ArrayList<>();
    static float height;
    static float width;
    static int nbrTurns = 2;
    static double thetaStepSize = 0.1;

    public InitializeBackground(Context context, int width, int height) {
        super(context);
        this.height = height;
        this.width = width;
        brush.setAntiAlias(true);
        brush.setColor(Color.GRAY);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vector = new VectorDrawable();
        }
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getPath(90 * nbrTurns, thetaStepSize, list, true);
        postInvalidate();
    }

    static public void getPath(int size, double thetaStepSize, List<List<Float>> list, boolean drawPath) {
        float x = width / 2;
        float y = height / 2;
        double theta;
        List<Float> listItem = new ArrayList<>();
        Log.d("Numbers", "width:" + width + ", height:" + height);
        if(drawPath)
            path.moveTo(x, y);

        for (int i = 0; i < size; i++) {
            listItem.clear();
            theta = thetaStepSize * i;
            x = (float) (2 * (1 + theta) * Math.cos(theta) + x);
            y = (float) (2 * (1 + theta) * Math.sin(theta) + y);
            Log.d("Numbers", "X: " + x + " Y:" + y);
            if(drawPath)
                path.lineTo(x, y);
            listItem.add(x);
            listItem.add(y);
            list.add(listItem);
        }
    }

   @Override
   public Drawable getBackground(){
        return new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                canvas.drawPath(path, brush);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.OPAQUE;
            }
        };
   }
}

