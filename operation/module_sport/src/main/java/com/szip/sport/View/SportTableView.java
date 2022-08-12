package com.szip.sport.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.sport.R;

import java.util.Locale;

public class SportTableView extends View {

    private int mWidth,mHeight;//本页面宽，高

    private long time;
    private Paint paint = new Paint();//表格画笔
    private Paint textYPaint = new Paint();//Y坐标画笔
    private Paint linePaint = new Paint();//折线图画笔
    private Paint mPaintShader = new Paint();//阴影画笔

    private int[]data;


    private float dpValue;
    private float maxValue = 60,minValue = 40;

    private boolean isTouchAble = false;
    private int index;
    private Paint pointPaint,squareBackPaint, touchDataTextPaint,timeTextPaint;


    public SportTableView(Context context) {
        super(context);
        initView();
    }

    public SportTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SportTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    private void initView() {

        dpValue = MathUtil.newInstance().dip2Px(1,getContext());

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.sport_green));
        paint.setStrokeWidth(1.0f);
        textYPaint.setColor(getContext().getResources().getColor(R.color.sport_gray_text));
        textYPaint.setTextSize(dpValue*7);


        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(dpValue);
        linePaint.setColor(getContext().getResources().getColor(R.color.sport_green));

        squareBackPaint = new Paint();
        squareBackPaint.setColor(Color.BLACK);
        touchDataTextPaint = new Paint();
        touchDataTextPaint.setColor(Color.WHITE);
        touchDataTextPaint.setTextSize(dpValue*16);
        timeTextPaint = new Paint();
        timeTextPaint.setTextSize(dpValue*10);
        timeTextPaint.setColor(Color.WHITE);
        pointPaint = new Paint();
        pointPaint.setColor(getContext().getResources().getColor(R.color.sport_green));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data!=null){
            DrawYView(canvas);
            DrawTable(canvas);
        }
    }

    /**
     * 画纵坐标
     * */
    private void DrawYView(Canvas canvas){

        textYPaint.setTextAlign(Paint.Align.LEFT);
        paint.setStrokeWidth(1f);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{8,8},0);
        paint.setPathEffect(dashPathEffect);
        float diffCoordinate = (mHeight-dpValue*5)*2f/3f/5f;
        String[] yMsg = getYMsg();
        for(int i = 0; i<6; i++) {
            float levelCoordinate = mHeight-dpValue*5-diffCoordinate*i;
            Path dashPath = new Path();
            dashPath.moveTo(dpValue*15, levelCoordinate);
            dashPath.lineTo(mWidth-dpValue*15, levelCoordinate);
            canvas.drawText(yMsg[i], dpValue*3,
                    levelCoordinate+dpValue*4, textYPaint);
            canvas.drawPath(dashPath, paint);
        }
    }

    /**
     * 画报告
     */
    private void DrawTable(Canvas canvas){

        if (data!=null){
            Path path = new Path();
            Path mPathShader = new Path();
            for (int i = 0; i< data.length; i++){
                float top;
                top = mHeight-dpValue*5-(mHeight-dpValue*5)*2f/3f*data[i]/maxValue;
                if (i==0){
                    path.moveTo(dpValue*15, top);
                    mPathShader.moveTo(dpValue*15, top);
                } else{
                    path.lineTo(dpValue*15+(mWidth-15*dpValue*2)/(data.length-1)*i,top);
                    mPathShader.lineTo(dpValue*15+(mWidth-15*dpValue*2)/(data.length-1)*i,top);
                    if (i == data.length - 1) {
                        mPathShader.lineTo(mWidth-dpValue*15, mHeight-dpValue*5);
                        mPathShader.lineTo(dpValue*15, mHeight-dpValue*5);
                        mPathShader.close();
                    }

                }
                Shader mShader = new LinearGradient(0, 0, 0, getHeight(), getContext().getResources().getColor(R.color.sport_green),
                        Color.TRANSPARENT, Shader.TileMode.MIRROR);
                mPaintShader.setShader(mShader);
            }

            canvas.drawPath(path, linePaint);
            canvas.drawPath(mPathShader, mPaintShader);

            if (isTouchAble){
                float x = dpValue*15+(mWidth-15*dpValue*2)/(data.length-1)*index;
                float y = mHeight-dpValue*5-(mHeight-dpValue*5)*2f/3f*data[index]/maxValue;
                canvas.drawCircle(x,y,dpValue*2,pointPaint);
                Path line = new Path();
                line.moveTo(x,mHeight/3-5*dpValue);
                line.lineTo(x,mHeight-5*dpValue);
                canvas.drawPath(line,paint);

                if (x<40*dpValue)
                    x = 40*dpValue;
                if (x>mWidth-40*dpValue)
                    x = mWidth-40*dpValue;

                canvas.drawRoundRect(x-40*dpValue,10*dpValue,x+40*dpValue,mHeight/3-15*dpValue,10*dpValue,
                        10*dpValue,squareBackPaint);
                String dataStr = "",timeStr = "";
                dataStr = String.format("%d", data[index]);
                timeStr = DateUtil.getStringDateFromSecond(time+index*10,"MM-dd HH:mm");
                float touchDataTextWidth = touchDataTextPaint.measureText(dataStr);
                float timeTextWidth = timeTextPaint.measureText(timeStr);
                canvas.drawText(dataStr, (x-(touchDataTextWidth)/2),
                        mHeight/3-23*dpValue, touchDataTextPaint);
                canvas.drawText(timeStr,(x-timeTextWidth/2),
                        10*dpValue+18*dpValue,timeTextPaint);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                isTouchAble = true;
                if (x<dpValue*15){
                    index = 0;
                }else if (x>mWidth-dpValue*15){
                    index = data.length-1;
                }else {
                    for (int i=0,j = i+1;i<data.length-1;i++,j++){
                        if ((x-(dpValue*19+(mWidth-19*dpValue*2)/(data.length-1)*i))*
                                (x-(dpValue*19+(mWidth-19*dpValue*2)/(data.length-1)*j))<0){
                            index = Math.abs((x-(dpValue*19+(mWidth-19*dpValue*2)/(data.length-1)*i)))>
                                    Math.abs((x-(dpValue*19+(mWidth-19*dpValue*2)/(data.length-1)*j)))?j:i;
                            break;
                        }
                    }
                }
                break;
        }
        postInvalidate();
        return super.onTouchEvent(event);
    }


    public void setDataList(int[] data,long time) {
        Log.i("DATA******","list = "+data.length);
        if (this.data!=null&&this.data.length!=data.length){
            index = 0;
            isTouchAble = false;
        }
        this.data = data;
        this.time = time;
        if (data!=null){
            for (int value:data){
                if (value>maxValue)
                    maxValue = value;
            }
        }
        maxValue *=1.2;
        postInvalidate();
    }

    private String[] getYMsg(){
        String[] yMsg= new String[6];
        int interval = (int) (maxValue/5);
        for (int i = 0;i<6;i++){
            yMsg[i] = String.format(Locale.ENGLISH,"%d",interval*i);
        }
        return yMsg;
    }

    public interface OnTouchListener{
        void onPosition(int index);
    }
}
