package com.ubicomp.stopit.stopit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DrawSpiralCanvas extends View{

    public LayoutParams params;
    public Path path= new Path();
    public Paint brush=new Paint();

    public DrawSpiralCanvas(Context context) {
        super(context);

    brush.setAntiAlias(true);
    brush.setColor(Color.BLUE);
    brush.setStyle(Paint.Style.STROKE);
    brush.setStrokeJoin(Paint.Join.ROUND);
    brush.setStrokeWidth(3f);
    params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        float pointX=event.getX();
        float pointY=event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            default:
                return false;
        }

        postInvalidate();
        return false;
        }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path,brush);
    }
}
