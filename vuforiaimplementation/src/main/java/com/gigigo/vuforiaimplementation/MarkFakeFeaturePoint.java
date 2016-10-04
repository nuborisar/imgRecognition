package com.gigigo.vuforiaimplementation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;

import java.util.Random;

/**
 * Created by nubor on 30/09/2016.
 */
public class MarkFakeFeaturePoint extends View implements Runnable {

    final Bitmap fakePoint;
    boolean bDisableIfThrowException = false;
    Random randX, randY;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            invalidate();
        }

        ;
    };

    ObjectAnimator mAnimation;

    public MarkFakeFeaturePoint(Context context) {
        super(context);
        fakePoint = BitmapFactory.decodeResource(getResources(), R.drawable.ir_mark_point);
        randX = new Random();
        randY = new Random();

        new Thread(this).start();
    }

    public void setObjectAnimator(ObjectAnimator animation) {
        if (animation != null) {
            mAnimation = animation;
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.restore();
        Random rand = new Random();
        int n = rand.nextInt(5);
        for (int i = 0; i < n; i++)
            paintFakeFeaturePoint(canvas);
    }

    private void paintFakeFeaturePoint(Canvas canvas) {
        int xMax = 720;
        int yMax = 1280;
        int x, y = 0;
        try {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            xMax = screenHeight;
            yMax = screenWidth;
        } catch (Throwable tr) {
            bDisableIfThrowException = true;
        }

        Random rand = new Random();
        int n = rand.nextInt(200) + 1; // +1 for avoid 0/2
        x = randX.nextInt(xMax);
        y = randY.nextInt(yMax);

        if (mAnimation != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Random randy = new Random(); //earl!

            float valueYScanline = ((Float) (mAnimation.getAnimatedValue()));
            int valueYint = Math.round(valueYScanline);

            int max = valueYint + 100;
            int min= valueYint - 100;
            //todo set limits
            y = randy.nextInt((max - min) + 1) + min;
        }

        Paint p = new Paint();
        if ((n % 2) == 0)
            canvas.drawBitmap(fakePoint, x, y, p);
    }

    public void run() {
        while (true) {
            try {
                if (bDisableIfThrowException) break;
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }
    }
}
