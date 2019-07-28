package com.wis.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by ybbz on 16/8/28.
 */
public class DrawImageView extends ImageView {

    private Paint paint;
    private int left, top, right, bottom = 0;

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true); // 反锯齿
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.5f);//设置线宽
        //paint.setAlpha(100);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(500);
                        postInvalidate();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setParam(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawRect(rect, paint);//绘制矩形
        Log.i("DrawImageView", "drawRect(left, top, right, bottom) = " + left + "," + top + "," + right + "," + bottom);
    }

}
