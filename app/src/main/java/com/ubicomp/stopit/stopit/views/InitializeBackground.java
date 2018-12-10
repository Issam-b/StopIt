package com.ubicomp.stopit.stopit.views;

import android.content.Context;
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

public class InitializeBackground extends View {

    public ViewGroup.LayoutParams params;
    public Path path = new Path();
    public Paint brush = new Paint();
    public VectorDrawable vector;
    double theta;
    float width;
    float hight;

    public InitializeBackground(Context context, float width, float hight) {
        super(context);
        this.width = width;
        this.hight = hight;
        brush.setAntiAlias(true);
        brush.setColor(Color.GRAY);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vector = new VectorDrawable();
        }
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        getPath();
    }

    public void getPath() {

        float x = width;
        float y = hight;
        Log.d("Numbers", "width:" + width + ", height:" + hight);
        path.moveTo(x, y);
        for (int i = 0; i < 180; i++) {
            theta = 0.1 * i;
            x = (float) (2 * (1 + theta) * Math.cos(theta) + x);
            y = (float) (2 * (1 + theta) * Math.sin(theta) + y);
            path.lineTo(x, y);
        }
        postInvalidate();

    }

   @Override
   public Drawable getBackground(){
        return new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                canvas.drawPath(path,brush);
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

