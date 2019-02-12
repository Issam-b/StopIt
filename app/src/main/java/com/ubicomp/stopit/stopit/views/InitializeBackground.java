package com.ubicomp.stopit.stopit.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.ubicomp.stopit.stopit.model.DrawPathCoordinates;
import com.ubicomp.stopit.stopit.model.DrawTriangleCoordinates;


public class InitializeBackground extends View {

    private Path path = new Path();
    private Paint brush = new Paint();

    public InitializeBackground(Context context, String background) {
        super(context);
        brush.setAntiAlias(true);
        brush.setColor(Color.GRAY);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);
        switch (background) {
            case "spiral": {
                DrawPathCoordinates drawPathCoordinates = new DrawPathCoordinates();
                drawPathCoordinates.drawGreyPath(path);
                break;
            }
            case"triangle":{
                DrawTriangleCoordinates drawTriangleCoordinates= new DrawTriangleCoordinates();
                drawTriangleCoordinates.drawGreyPath(path);
                break;
            }
            default: break;

        }
        postInvalidate();
    }

    @Override
    public Drawable getBackground() {
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

