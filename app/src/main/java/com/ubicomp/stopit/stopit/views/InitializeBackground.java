package com.ubicomp.stopit.stopit.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.ubicomp.stopit.stopit.R;
import com.ubicomp.stopit.stopit.model.DrawPathCoordinates;
import com.ubicomp.stopit.stopit.model.DrawSquareCoordinates;


public class InitializeBackground extends View {

    private Path path = new Path();
    private Paint brush = new Paint();

    public InitializeBackground(Context context) {
        super(context);
    }

    public InitializeBackground(Context context, String background) {
        super(context);
        brush.setAntiAlias(true);
        brush.setColor(Color.GRAY);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(5f);

        // Preparing the corresponding instruction image depending on the selected shape
        View view = LayoutInflater.from(context).inflate(R.layout.instructions_layout, null);
        ImageView help = view.findViewById(R.id.help_image);

        switch (background) {
            case "spiral": {
                DrawPathCoordinates drawPathCoordinates = new DrawPathCoordinates();
                drawPathCoordinates.drawGreyPath(path);
                help.setImageResource(R.drawable.ic_hint_spiral);
                break;
            }
            case "square": {
                DrawSquareCoordinates drawSquareCoordinates= new DrawSquareCoordinates();
                drawSquareCoordinates.drawGreyPath(path);
                help.setImageResource(R.drawable.ic_hint_square);
                break;
            }
            default: break;
        }
        postInvalidate();

        // Checking for the condition if the instructions dialog should be shown
        // Show if necessary
        final SharedPreferences pref = getContext().getSharedPreferences("instructions", Context.MODE_PRIVATE);
        final String hint_state = "hide_" + background;
        if (!pref.getBoolean(hint_state, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(view);
            builder.setTitle("Instructions");
            builder.setMessage("Draw a line as shown in the image below");
            builder.setPositiveButton("Ok", null);
            builder.setNeutralButton("Do not show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    pref.edit().putBoolean(hint_state, true).commit();
                }
            });
            builder.show();
        }
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

