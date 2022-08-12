package com.szip.healthy.View;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.MathUtil;
import com.szip.healthy.R;

public class SleepArcProgressBar extends View {

    private int mWidth;
    private int mHeight;

    private int diameter = 500;  //直径

    private Paint allArcPaint;
    private Paint progressPaint;
    private Paint degreePaint;
    private RectF bgRect;

    private float startAngleBg = -90;//开始角度(0°与控件X轴平行)
    private float sweepAngleBg = 360;//弧形扫过的区域
    private float startAngle = 0;//开始角度(0°与控件X轴平行)
    private float sweepAngle = 0;//弧形扫过的区域
    private float currentAngle = 0;
//    private float lastAngle;
//    private float currentValues = 0;

    private float centerX;  //圆心X坐标
    private float centerY;  //圆心Y坐标
    private float dpValue;
    private float bgArcWidth;
    private float progressWidth;
    private float longDegree;//长刻度
    private float shortDegree;//短刻度
    private int DEGREE_PROGRESS_DISTANCE;;//弧形与外层刻度的距离


//    private int   aniSpeed      = 1000;//动画时长
//    private float k;

    private PaintFlagsDrawFilter mDrawFilter;
    private ValueAnimator  progressAnimator;


    public SleepArcProgressBar(Context context) {
        super(context);
        initView();
    }

    public SleepArcProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SleepArcProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        dpValue = MathUtil.newInstance().dip2Px(1,getContext());
        DEGREE_PROGRESS_DISTANCE = (int) (4*dpValue);
        progressWidth = 15*dpValue;
        bgArcWidth = 15*dpValue;
        longDegree = 5*dpValue;
        shortDegree = 2*dpValue;
//        k = sweepAngleBg / 720f;

        degreePaint = new Paint();
        degreePaint.setColor(Color.parseColor("#e5e6eb"));


        //整个弧形画笔
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeWidth(bgArcWidth);
        allArcPaint.setColor(Color.parseColor("#e5e6eb"));
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);

        //当前进度的弧形画笔
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setColor(Color.parseColor("#534aff"));

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
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

        //圆心
        centerX = mWidth / 2;
        centerY = mHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);
        //画刻度线
        float start = centerY - diameter / 2 + progressWidth / 2 +
                DEGREE_PROGRESS_DISTANCE;
        for(int i = 0; i < 60; i++)//把整个圆划分成60小份
        {
            degreePaint.setStrokeWidth(dpValue);
            if (i%5 == 0){
                canvas.drawLine(centerX,  start, centerX, start+longDegree, degreePaint);
            }else {
                canvas.drawLine(centerX, start, centerX, start+shortDegree, degreePaint);
            }

            //每绘制一个小刻度,旋转1/90
            canvas.rotate(6, centerX, centerY);
        }
        degreePaint.setTextSize(8*dpValue);
        degreePaint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
        String str = "12";
        float textWidth = degreePaint.measureText(str);
        canvas.drawText(str,centerX-textWidth/2,start+longDegree+10*dpValue,degreePaint);

        str = "9";
        textWidth = degreePaint.measureText(str);
        canvas.drawText(str,centerX - diameter / 2 + progressWidth / 2 +
                DEGREE_PROGRESS_DISTANCE+10*dpValue,centerY+textWidth/2,degreePaint);

        str = "6";
        textWidth = degreePaint.measureText(str);
        canvas.drawText(str,centerX-textWidth/2,2*centerY-start-10*dpValue,degreePaint);

        str = "3";
        textWidth = degreePaint.measureText(str);
        canvas.drawText(str,centerX + diameter / 2 - progressWidth / 2 -
                DEGREE_PROGRESS_DISTANCE-10*dpValue-textWidth,centerY+textWidth/2,degreePaint);

        //整个弧
        canvas.drawArc(bgRect, startAngleBg, sweepAngleBg, false, allArcPaint);
        //当前进度
        canvas.drawArc(bgRect, startAngle, currentAngle, false, progressPaint);
    }

    /**
     * 设置当前值
     */
    public void setCurrentValues(float values,int startTime) {
        if(values > 1440)
        {
            values = 1440;
        }
        if(values < 0)
        {
            values = 0;
        }
//        this.currentValues = values;
        startTime%=720;
        startAngle = startTime/720f*360-90;
        currentAngle = values/720*360;
//        setAnimation(lastAngle, values * k, aniSpeed);
        postInvalidate();
    }

//    /**
//     * 为进度设置动画
//     *
//     * @param last
//     * @param current
//     */
//    private void setAnimation(float last, float current, int length)
//    {
//        progressAnimator = ValueAnimator.ofFloat(last, current);
//        progressAnimator.setDuration(length);
//        progressAnimator.setTarget(currentAngle);
//        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
//        {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation)
//            {
//                currentAngle = (float) animation.getAnimatedValue();
//                currentValues = currentAngle / k;
//            }
//        });
//        progressAnimator.start();
//    }

}
