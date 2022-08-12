package com.szip.user.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;

public class RoundProgressBar extends View {
    private float ratio = 0;
    private float barWidth;
    private int mWidth;
    private int mHeight;
    private float dpValue;

    private Paint paint;
    public RoundProgressBar(Context context) {
        super(context);
        initView();
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        dpValue = MathUtil.newInstance().dip2Px(1,getContext());
        barWidth = dpValue*4;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(barWidth);
        paint.setColor(Color.parseColor("#f4f4f6"));
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

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
        canvas.drawLine(barWidth/2,mHeight/2,mWidth-barWidth/2,mHeight/2,paint);
        paint.setColor(Color.parseColor("#a1e510"));
        canvas.drawLine(barWidth/2,mHeight/2,(mWidth-barWidth)*ratio+barWidth/2,mHeight/2,paint);
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
        postInvalidate();
    }

}
