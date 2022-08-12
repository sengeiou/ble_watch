package com.szip.healthy.View;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.szip.healthy.R;


/**
 * colorful arc progress bar
 * Created by shinelw on 12/4/15.
 */

public class ColorArcProgressBar extends View
{

    private int mWidth;
    private int mHeight;

    private int diameter = 500;  //直径

    private Paint allArcPaintStep;
    private Paint allArcPaintDistance;
    private Paint allArcPaintCalorie;
    private Paint progressPaintStep;
    private Paint progressPaintDistance;
    private Paint progressPaintCalorie;


    private RectF bgRect,bgRectCalorie;

    private ValueAnimator  progressAnimator;
    private PaintFlagsDrawFilter mDrawFilter;

    private int colorStep;
    private int colorStepBackground;
    private int colorDistance;
    private int colorDistanceBackground;
    private int colorCalorie;
    private int colorCalorieBackground;

    private float startAngleStep   = 190;//开始角度(0°与控件X轴平行)
    private float sweepAngleStep   = 160;//弧形扫过的区域
    private float startAngleDistance   = -190;//开始角度(0°与控件X轴平行)
    private float sweepAngleDistance   = -160;//弧形扫过的区域
    private float startAngleCalorie   = -90;//开始角度(0°与控件X轴平行)
    private float sweepAngleCalorie   = 360;//弧形扫过的区域
    private float currentAngleStep = 0;
    private float currentAngleDistance = 0;
    private float currentAngleCalorie = 0;
    private float lastAngleStep;
    private float lastAngleDistance;
    private float lastAngleCalorie;

    private float maxValuesStep     = 6000;
    private float maxValuesDistance     = 6000;
    private float maxValuesCalorie     = 6000;
    private float currentValuesStep = 0;
    private float currentValuesDistance = 0;
    private float currentValuesCalorie = 0;
    private float bgArcWidth    = dipToPx(10);
    private float progressWidth = dipToPx(10);
    private int   aniSpeed      = 1000;//动画时长


    // sweepAngle / maxValues 的值
    private float kStep,kDistance,kCalorie;


    public ColorArcProgressBar(Context context)
    {
        super(context, null);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs)
    {
        super(context, attrs, 0);
        initConfig(context, attrs);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initConfig(context, attrs);
        initView();
    }

    /**
     * 初始化布局配置
     *
     * @param context
     * @param attrs
     */
    private void initConfig(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorArcProgressBar);

        colorStep = a.getColor(R.styleable.ColorArcProgressBar_step_color, Color.GREEN);
        colorStepBackground = a.getColor(R.styleable.ColorArcProgressBar_step_color_background, Color.GREEN);
        colorDistance = a.getColor(R.styleable.ColorArcProgressBar_distance_color, Color.GREEN);
        colorDistanceBackground = a.getColor(R.styleable.ColorArcProgressBar_distance_color_background, Color.GREEN);
        colorCalorie = a.getColor(R.styleable.ColorArcProgressBar_calorie_color, Color.GREEN);
        colorCalorieBackground = a.getColor(R.styleable.ColorArcProgressBar_calorie_color_background, Color.GREEN);
        bgArcWidth = a.getDimension(R.styleable.ColorArcProgressBar_bg_arc_width, dipToPx(10));
        progressWidth = a.getDimension(R.styleable.ColorArcProgressBar_front_width, dipToPx(10));

        setCurrentValues(0,0,0);
        setMaxValues(maxValuesStep,maxValuesCalorie);

        a.recycle();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        Log.v("ColorArcProgressBar", "onSizeChanged: mWidth:" + mWidth + " mHeight:" + mHeight);

        diameter = (int) (Math.min(mWidth, mHeight) - 2 * (progressWidth / 2));

        Log.v("ColorArcProgressBar", "onSizeChanged: diameter:" + diameter);

        //弧形的矩阵区域
        bgRect = new RectF();
        bgRect.top = progressWidth / 2;
        bgRect.left = progressWidth / 2;
        bgRect.right = diameter + ( progressWidth / 2 );
        bgRect.bottom = diameter + (progressWidth / 2 );

        bgRectCalorie = new RectF();
        bgRectCalorie.top = progressWidth / 2+progressWidth*2;
        bgRectCalorie.left = progressWidth / 2+progressWidth*2;
        bgRectCalorie.right = diameter + ( progressWidth / 2 )-progressWidth*2;
        bgRectCalorie.bottom = diameter + (progressWidth / 2 )-progressWidth*2;

        Log.v("ColorArcProgressBar", "initView: " + diameter);

    }

    private void initView() {

        //整个弧形画笔
        allArcPaintStep = new Paint();
        allArcPaintStep.setAntiAlias(true);
        allArcPaintStep.setStyle(Paint.Style.STROKE);
        allArcPaintStep.setStrokeWidth(bgArcWidth);
        allArcPaintStep.setColor((colorStepBackground));
        allArcPaintStep.setStrokeCap(Paint.Cap.ROUND);

        allArcPaintDistance = new Paint();
        allArcPaintDistance.setAntiAlias(true);
        allArcPaintDistance.setStyle(Paint.Style.STROKE);
        allArcPaintDistance.setStrokeWidth(bgArcWidth);
        allArcPaintDistance.setColor((colorDistanceBackground));
        allArcPaintDistance.setStrokeCap(Paint.Cap.ROUND);

        allArcPaintCalorie = new Paint();
        allArcPaintCalorie.setAntiAlias(true);
        allArcPaintCalorie.setStyle(Paint.Style.STROKE);
        allArcPaintCalorie.setStrokeWidth(bgArcWidth);
        allArcPaintCalorie.setColor((colorCalorieBackground));
        allArcPaintCalorie.setStrokeCap(Paint.Cap.ROUND);

        //当前进度的弧形画笔
        progressPaintStep = new Paint();
        progressPaintStep.setAntiAlias(true);
        progressPaintStep.setStyle(Paint.Style.STROKE);
        progressPaintStep.setStrokeCap(Paint.Cap.ROUND);
        progressPaintStep.setStrokeWidth(progressWidth);
        progressPaintStep.setColor(colorStep);

        progressPaintDistance = new Paint();
        progressPaintDistance.setAntiAlias(true);
        progressPaintDistance.setStyle(Paint.Style.STROKE);
        progressPaintDistance.setStrokeCap(Paint.Cap.ROUND);
        progressPaintDistance.setStrokeWidth(progressWidth);
        progressPaintDistance.setColor(colorDistance);

        progressPaintCalorie = new Paint();
        progressPaintCalorie.setAntiAlias(true);
        progressPaintCalorie.setStyle(Paint.Style.STROKE);
        progressPaintCalorie.setStrokeCap(Paint.Cap.ROUND);
        progressPaintCalorie.setStrokeWidth(progressWidth);
        progressPaintCalorie.setColor(colorCalorie);

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);

        //整个弧
        canvas.drawArc(bgRect, startAngleStep, sweepAngleStep, false, allArcPaintStep);
        canvas.drawArc(bgRect, startAngleDistance, sweepAngleDistance, false, allArcPaintDistance);
        canvas.drawArc(bgRectCalorie, startAngleCalorie, sweepAngleCalorie, false, allArcPaintCalorie);


        //当前进度
        canvas.drawArc(bgRect, startAngleStep, currentAngleStep, false, progressPaintStep);
        canvas.drawArc(bgRect, startAngleDistance, currentAngleDistance, false, progressPaintDistance);
        canvas.drawArc(bgRectCalorie, startAngleCalorie, currentAngleCalorie, false, progressPaintCalorie);
        invalidate();

    }

    /**
     * 设置最大值
     *
     * @param maxValues
     */
    public void setMaxValues(float maxValues,float maxCalorie) {
        this.maxValuesStep = maxValues;
        kStep = sweepAngleStep / maxValues;
        this.maxValuesDistance = 172*0.45f*maxValues/100*1.5f;
        kDistance = sweepAngleDistance/maxValuesDistance;
        this.maxValuesCalorie = maxCalorie*1000;
        kCalorie = sweepAngleCalorie/maxValuesCalorie;
    }

    /**
     * 设置当前值
     *
     * @param step
     */
    public void setCurrentValues(float step,float distance,float calorie) {
        if(step > maxValuesStep)
        {
            step = maxValuesStep;
        }
        if(step < 0)
        {
            step = 0;
        }
        this.currentValuesStep = step;
        lastAngleStep = currentAngleStep;
        setAnimation(lastAngleStep, step * kStep, aniSpeed);

        if(distance > maxValuesDistance)
        {
            distance = maxValuesDistance;
        }
        if(distance < 0)
        {
            distance = 0;
        }
        this.currentValuesDistance = distance;
        lastAngleDistance = currentAngleDistance;
        setAnimationDistance(lastAngleDistance, distance * kDistance, aniSpeed);

        if(calorie > maxValuesCalorie)
        {
            calorie = maxValuesCalorie;
        }
        if(calorie < 0)
        {
            calorie = 0;
        }
        this.currentValuesCalorie = calorie;
        lastAngleCalorie = currentAngleCalorie;
        setAnimationCalorie(lastAngleCalorie, calorie * kCalorie, aniSpeed);
    }

    /**
     * 为进度设置动画
     *
     * @param last
     * @param current
     */
    private void setAnimation(float last, float current, int length)
    {
        progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngleStep);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {

            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                currentAngleStep = (float) animation.getAnimatedValue();
                currentValuesStep = currentAngleStep / kStep;
            }
        });
        progressAnimator.start();
    }

    /**
     * 为进度设置动画
     *
     * @param last
     * @param current
     */
    private void setAnimationDistance(float last, float current, int length)
    {
        progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngleDistance);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {

            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                currentAngleDistance = (float) animation.getAnimatedValue();
                currentValuesDistance = currentAngleDistance / kDistance;
            }
        });
        progressAnimator.start();
    }

    /**
     * 为进度设置动画
     *
     * @param last
     * @param current
     */
    private void setAnimationCalorie(float last, float current, int length)
    {
        progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngleCalorie);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {

            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                currentAngleCalorie = (float) animation.getAnimatedValue();
                currentValuesCalorie = currentAngleCalorie / kCalorie;
            }
        });
        progressAnimator.start();
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private int dipToPx(float dip)
    {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

}
