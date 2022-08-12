package com.szip.healthy.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;

public class RectProgressBar extends View {
    private float ratio = 0;
    private int mWidth;
    private int mHeight;
    private float dpValue;

    private Paint paint;
    public RectProgressBar(Context context) {
        super(context);
        initView();
    }

    public RectProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RectProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(32f);

        dpValue = MathUtil.newInstance().dip2Px(1,getContext());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.parseColor("#f4f4f6"));
        canvas.drawLine(0,mHeight/2,mWidth,mHeight/2,paint);
        paint.setColor(Color.parseColor("#a1e510"));
        canvas.drawLine(0,mHeight/2,mWidth*ratio,mHeight/2,paint);
    }

    public void setRatio(float ratio) {
        LogUtil.getInstance().logd("data******","ratio = "+ratio);
        this.ratio = ratio/100f;
        postInvalidate();
    }
}
