package com.szip.healthy.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;

public class HeartLevelView extends View {

    private Paint allArcPaint;

    private int mWidth;
    private int mHeight;
    private int diameter = 500;  //直径
    private RectF bgRect;
    private float progressWidth;
    private PaintFlagsDrawFilter mDrawFilter;
    private float startAngle   = -90;//开始角度(0°与控件X轴平行)
    private int[] datas;
    public HeartLevelView(Context context) {
        super(context);
        initView();
    }

    public HeartLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HeartLevelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        diameter = (int) (Math.min(mWidth, mHeight) - 2 * (progressWidth / 2));

        //弧形的矩阵区域
        bgRect = new RectF();
        bgRect.top = progressWidth / 2;
        bgRect.left = progressWidth / 2;
        bgRect.right = diameter + ( progressWidth / 2 );
        bgRect.bottom = diameter + (progressWidth / 2 );

    }


    private void initView() {
        progressWidth = MathUtil.newInstance().dip2Px(30,getContext());
        //整个弧形画笔
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeWidth(progressWidth);
        LogUtil.getInstance().logd("data******","width = "+progressWidth);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(datas==null)
            return;
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);
        float sum = 0;
        for (int value:datas){
            sum+=value;
        }
        if (sum==0)
            return;
        float intervalAngle = 0;
        int color[] = new int[6];
        color[0] = Color.BLACK;
        color[1] = Color.parseColor("#bb000000");
        color[2] = Color.parseColor("#88000000");
        color[3] = Color.parseColor("#66000000");
        color[4] = Color.parseColor("#33000000");
        color[5] = Color.parseColor("#50e034");
        //整个弧
        for (int i = 0;i<6;i++){
            allArcPaint.setColor(color[i]);
            canvas.drawArc(bgRect, startAngle+intervalAngle, datas[i]/sum*360f, false, allArcPaint);
            intervalAngle+=datas[i]/sum*360f;
        }
        invalidate();

    }

    public void setDatas(int[] datas) {
        this.datas = datas;
    }
}
