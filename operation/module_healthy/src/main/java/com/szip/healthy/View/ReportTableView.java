package com.szip.healthy.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportTableView extends View {
    private int mWidth,mHeight;//本页面宽，高
    private List<ReportData> stepDayList;
    private List<ReportData> stepWeekList;
    private List<ReportData> stepMonthList;
    private List<ReportData> sleepDayList;
    private List<ReportData> sleepWeekList;
    private List<ReportData> sleepMonthList;
    private List<ReportData> heartDayList;
    private List<ReportData> heartWeekList;
    private List<ReportData> heartMonthList;
    private List<ReportData> oxygenDayList;
    private List<ReportData> oxygenWeekList;
    private List<ReportData> oxygenMonthList;
    private List<ReportData> pressureDayList;
    private List<ReportData> pressureWeekList;
    private List<ReportData> pressureMonthList;
    private List<ReportData> tempDayList;
    private List<ReportData> tempWeekList;
    private List<ReportData> tempMonthList;
    private float maxValue = 60;
    private int startTime,allTime;
    private float dpValue;
    private float viewMargin;
    private boolean isTouchAble = false;
    private int index;
    private float barWidth = 18f;

    private Paint textYPaint = new Paint();//Y坐标画笔
    private Paint textXPaint = new Paint();//Y坐标画笔
    private Paint pointPaint,squareBackPaint, touchDataTextPaint,timeTextPaint;
    private List<ReportData> touchDataList;

    public ReportTableView(Context context) {
        super(context);
        initView();
    }

    public ReportTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ReportTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        viewMargin = 18*dpValue;
        textYPaint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
        textYPaint.setTextSize(dpValue*6);

        textXPaint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
        textXPaint.setTextSize(dpValue*7);

        squareBackPaint = new Paint();
        squareBackPaint.setColor(Color.BLACK);
        touchDataTextPaint = new Paint();
        touchDataTextPaint.setColor(Color.WHITE);
        touchDataTextPaint.setTextSize(dpValue*16);
        timeTextPaint = new Paint();
        timeTextPaint.setTextSize(dpValue*10);
        timeTextPaint.setColor(Color.WHITE);
        pointPaint = new Paint();
        pointPaint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stepDayList!=null){
            DrawYView(canvas);
            DrawDXMsg(canvas);
            DrawDayStep(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,true);
            return;
        }

        if (stepWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeekStep(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }

        if (stepMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthStep(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }

        if (sleepDayList!=null){
            DrawSleepXMsg(canvas);
            DrawDaySleep(canvas);
            return;
        }

        if (sleepWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeepSleep(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }

        if (sleepMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthSleep(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }

        if (heartDayList!=null){
            DrawYView(canvas);
            DrawDXMsg(canvas);
            DrawDayHeart(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,true);
            return;
        }

        if (heartWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeekHeart(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }
        if (heartMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthHeart(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
            return;
        }
        if (oxygenDayList!=null){
            DrawYView(canvas);
            DrawDXMsg(canvas);
            DrawDayOxygen(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,true);
        }
        if (oxygenWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeekOxygen(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
        if (oxygenMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthOxygen(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
        if (tempDayList!=null){
            DrawYView(canvas);
            DrawDXMsg(canvas);
            DrawDayTemp(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,true);
        }
        if (tempWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeekTemp(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
        if (tempMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthTemp(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
        if (pressureDayList!=null){
            DrawYView(canvas);
            DrawDXMsg(canvas);
            DrawDayPressure(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,true);
        }
        if (pressureWeekList!=null){
            DrawYView(canvas);
            DrawWXMsg(canvas);
            DrawWeekPressure(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
        if (pressureMonthList!=null){
            DrawYView(canvas);
            DrawMXMsg(canvas);
            DrawMonthPressure(canvas);
            if (isTouchAble)
                DrawTouchRect(canvas,false);
        }
    }

    /**
     * 画纵坐标
     * */
    private void DrawYView(Canvas canvas){

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
        paint.setStrokeWidth(0.5f);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{8,8},0);
        paint.setPathEffect(dashPathEffect);
        textYPaint.setTextAlign(Paint.Align.LEFT);
        float diffCoordinate = (mHeight-viewMargin)*2f/3f/5f;
        String[] yMsg = getYMsg();
        for(int i = 0; i<6; i++) {
            float levelCoordinate = mHeight-viewMargin-diffCoordinate*i;
            Path dashPath = new Path();
            dashPath.moveTo(viewMargin, levelCoordinate);
            dashPath.lineTo(mWidth-viewMargin, levelCoordinate);
            canvas.drawText(yMsg[i], dpValue*3,
                    levelCoordinate+dpValue*4, textYPaint);
            canvas.drawPath(dashPath, paint);
        }
    }


    private String[] getYMsg(){
        String[] yMsg= new String[]{"","","","","","",""};
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getContext()));
        if (userModel==null)
            return yMsg;
        int interval = (int) (maxValue/5);
        for (int i = 0;i<6;i++){
            if (sleepWeekList!=null||sleepMonthList!=null){
                yMsg[i] = String.format(Locale.ENGLISH,"%d",interval*i/60);
            }else if (oxygenDayList!=null||oxygenMonthList!=null||oxygenWeekList!=null){
                yMsg[i] = String.format(Locale.ENGLISH,"%d",interval*i+85);
            }else if (tempDayList!=null||tempWeekList!=null||tempMonthList!=null){
                if (userModel.tempUnit==0)
                    yMsg[i] = String.format(Locale.ENGLISH,"%d",(interval*i+340)/10);
                else
                    yMsg[i] = String.format(Locale.ENGLISH,"%.1f",MathUtil.newInstance().c2f((interval*i+340)/10));
            }else {
                if (interval*i>1000)
                    yMsg[i] = String.format(Locale.ENGLISH,"%.1fk",interval*i/1000f);
                else
                    yMsg[i] = String.format(Locale.ENGLISH,"%d",interval*i);
            }

        }
        return yMsg;
    }



    private void DrawDXMsg(Canvas canvas){
        String[] xMsg= new String[2];
        xMsg[0] = "00:00";
        xMsg[1] = "23:59";
        textXPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(xMsg[0],viewMargin,mHeight-5*dpValue,textXPaint);
        canvas.drawText(xMsg[1],mWidth-viewMargin-textXPaint.measureText(xMsg[1]),mHeight-5*dpValue,textXPaint);
    }

    private void DrawWXMsg(Canvas canvas){
        String[] xMsg= new String[2];
        xMsg[0] = getContext().getString(R.string.healthy_sun);
        xMsg[1] = getContext().getString(R.string.healthy_sta);
        textXPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(xMsg[0],viewMargin,mHeight-5*dpValue,textXPaint);
        canvas.drawText(xMsg[1],mWidth-viewMargin-textXPaint.measureText(xMsg[1]),mHeight-5*dpValue,textXPaint);
    }

    private void DrawMXMsg(Canvas canvas){
        String[] xMsg= new String[2];
        xMsg[0] = "1";
        xMsg[1] = String.format("%d",touchDataList.size());
        textXPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(xMsg[0],viewMargin,mHeight-5*dpValue,textXPaint);
        canvas.drawText(xMsg[1],mWidth-viewMargin-textXPaint.measureText(xMsg[1]),mHeight-5*dpValue,textXPaint);
    }

    private void DrawSleepXMsg(Canvas canvas){
        String[] xMsg= new String[2];
        xMsg[0] = String.format("%02d:%02d",startTime/60,startTime%60);
        xMsg[1] = String.format("%02d:%02d",(startTime+allTime)%1440/60,(startTime+allTime)%1440%60);
        textXPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(xMsg[0],viewMargin,mHeight,textXPaint);
        canvas.drawText(xMsg[1],mWidth-viewMargin-textXPaint.measureText(xMsg[1]),mHeight,textXPaint);
    }

    private void DrawDayStep(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_green));
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float interval = (mWidth-2*viewMargin-24*barWidth)/23;
        for (int i = 0;i<stepDayList.size();i++){
            ReportData data = stepDayList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            float startX = viewMargin+barWidth / 2 + (interval + barWidth) * hour;
            float startY = mHeight-viewMargin-barWidth/2;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getAverageData()/maxValue+barWidth/2;
            if (stopY>startY)
                continue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
        }
    }



    private void DrawWeekStep(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_green));
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<stepWeekList.size();i++){
            ReportData data = stepWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-barWidth/2;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getAverageData()/maxValue+barWidth/2;
            if (stopY>startY)
                continue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
        }
    }

    private void DrawMonthStep(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_green));
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<stepMonthList.size();i++){
            ReportData data = stepMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-barWidth/2;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getAverageData()/maxValue+barWidth/2;
            if (stopY>startY)
                continue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
        }
    }

    private void DrawDaySleep(Canvas canvas){
        Paint paint = new Paint();
        float start = viewMargin;
        float width;
        float top,bottom;
        for (ReportData reportData:sleepDayList){
            width = (reportData.getTime()/(float)allTime)*(mWidth-2*viewMargin);
            if (reportData.getAverageData()==2){//深睡
                top = mHeight-viewMargin-(mHeight-viewMargin)/2f;
                bottom = mHeight-viewMargin;
                paint.setColor(getContext().getResources().getColor(R.color.healthy_ray));
            }else {
                top = 0;
                bottom = mHeight-viewMargin-(mHeight-viewMargin)/2f;
                paint.setColor(getContext().getResources().getColor(R.color.healthy_yellow));
            }
            RectF rectF = new RectF(start,top,start+width,bottom);
            canvas.drawRect(rectF,paint);
            start+=width;
        }
    }

    private void DrawWeepSleep(Canvas canvas){
        Paint paint = new Paint();

        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<sleepWeekList.size();i++){
            ReportData data = sleepWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-barWidth/2;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getAverageData()/maxValue+barWidth/2;
            LogUtil.getInstance().logd("data******","总睡眠 = "+data.getAverageData()+"startY = "+startY+"stopY = "+stopY);
            if (stopY>startY)
                stopY = startY;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_yellow));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            startY = mHeight-viewMargin-barWidth/2;
            stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue+barWidth/2;
            LogUtil.getInstance().logd("data******","浅睡眠 = "+data.getMinData()+"startY = "+startY+"stopY = "+stopY);
            if (stopY>startY){
                stopY = startY;
                paint.setColor(getContext().getResources().getColor(R.color.healthy_ray));
                canvas.drawLine(startX,startY,startX,
                        stopY, paint);
                continue;
            }
            paint.setColor(getContext().getResources().getColor(R.color.healthy_ray));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
            canvas.drawRect(startX,stopY,startX,stopY, paint);
        }
    }

    private void DrawMonthSleep(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<sleepMonthList.size();i++){
            ReportData data = sleepMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-barWidth/2;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getAverageData()/maxValue+barWidth/2;
            if (stopY>startY)
                stopY = startY;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_yellow));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            startY = mHeight-viewMargin-barWidth/2;
            stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue+barWidth/2;
            if (stopY>startY)
                continue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_ray));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
            if (data.getMinData()!=data.getAverageData())
                canvas.drawRect(startX,stopY,startX,stopY, paint);
        }
    }

    private void DrawDayHeart(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(4f);
        paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Paint mPaintShader = new Paint();//阴影画笔



        float interval = (mWidth-2*viewMargin-barWidth*24)/23f;
        float barHeight = (mHeight-viewMargin)*2/3;
        Path line = new Path();
        Path mPathShader = new Path();

        float startShader = 0;
        int lastPoint = 0;
        for (int i = 0;i<heartDayList.size();i++){
            ReportData reportData = heartDayList.get(i);
            if (reportData.getAverageData()==0)
                continue;
            float startX = viewMargin+barWidth / 2 + (interval + barWidth) * i;
            if (startShader == 0){
                startShader = startX;
                line.moveTo(startX,mHeight-reportData.getAverageData()/(float)maxValue*barHeight-viewMargin);
                mPathShader.moveTo(startX,mHeight-reportData.getAverageData()/(float)maxValue*barHeight-viewMargin);
            }else {
                line.lineTo(startX,mHeight-reportData.getAverageData()/(float)maxValue*barHeight-viewMargin);
                mPathShader.lineTo(startX,mHeight-reportData.getAverageData()/(float)maxValue*barHeight-viewMargin);
            }
            lastPoint = i;
            canvas.drawPoint(startX,mHeight-reportData.getAverageData()/(float)maxValue*barHeight-viewMargin,paint);
        }
        mPathShader.lineTo(viewMargin+barWidth / 2 + (interval + barWidth) *lastPoint, mHeight-viewMargin);
        mPathShader.lineTo(startShader, mHeight-viewMargin);
        mPathShader.close();



        Shader mShader = new LinearGradient(0, 0, 0, getHeight(), 0x33FF0000,
                Color.TRANSPARENT, Shader.TileMode.MIRROR);
        mPaintShader.setShader(mShader);
        canvas.drawPath(line,paint);
        canvas.drawPath(mPathShader, mPaintShader);

    }

    private void DrawWeekHeart(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<heartWeekList.size();i++){
            ReportData data = heartWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_red_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
            startY = mHeight-viewMargin-data.getAverageData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }

    private void DrawMonthHeart(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<heartMonthList.size();i++){
            ReportData data = heartMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_red_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
            startY = mHeight-viewMargin-data.getAverageData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }


    private void DrawDayOxygen(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-2*viewMargin-barWidth*24)/23f;
        float barHeight = (mHeight-viewMargin)*2/3;

        for (int i = 0;i<oxygenDayList.size();i++){
            ReportData reportData = oxygenDayList.get(i);
            if (reportData.getAverageData()==0)
                continue;
            float startX = viewMargin+barWidth / 2 + (interval + barWidth) * i;
            LogUtil.getInstance().logd("data******","start = "+startX+" height = "+(mHeight-(reportData.getAverageData()-85)/(float)maxValue*barHeight-dpValue*15));
            canvas.drawPoint(startX,mHeight-(reportData.getAverageData()-85)/(float)maxValue*barHeight-viewMargin,paint);
        }

    }

    private void DrawWeekOxygen(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<oxygenWeekList.size();i++){
            ReportData data = oxygenWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMinData()-85)/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMaxData()-85)/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_red_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
            startY = mHeight-viewMargin-(data.getAverageData()-85)/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }

    private void DrawMonthOxygen(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<oxygenMonthList.size();i++){
            ReportData data = oxygenMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMinData()-85)/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMaxData()-85)/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_red_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
            startY = mHeight-viewMargin-(data.getAverageData()-85)/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }

    private void DrawDayTemp(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-2*viewMargin-barWidth*24)/23f;
        float barHeight = (mHeight-viewMargin)*2/3;

        for (int i = 0;i<tempDayList.size();i++){
            ReportData reportData = tempDayList.get(i);
            if (reportData.getAverageData()==0)
                continue;
            float startX = viewMargin+barWidth / 2 + (interval + barWidth) * i;
            canvas.drawPoint(startX,mHeight-(reportData.getAverageData()-340)/(float)maxValue*barHeight-viewMargin,paint);
        }
    }

    private void DrawWeekTemp(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<tempWeekList.size();i++){
            ReportData data = tempWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMinData()-340)/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMaxData()-340)/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            startY = mHeight-viewMargin-(data.getAverageData()-340)/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
        }
    }

    private void DrawMonthTemp(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<tempMonthList.size();i++){
            ReportData data = tempMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMinData()-340)/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*(data.getMaxData()-340)/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            startY = mHeight-viewMargin-(data.getAverageData()-340)/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }

    private void DrawDayPressure(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-2*viewMargin-barWidth*24)/23f;
        float barHeight = (mHeight-viewMargin)*2/3;

        for (int i = 0;i<pressureDayList.size();i++){
            ReportData reportData = pressureDayList.get(i);
            if (reportData.getAverageData()==0)
                continue;
            float startX = viewMargin+barWidth / 2 + (interval + barWidth) * i;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange));
            canvas.drawPoint(startX,mHeight-(reportData.getAverageData())/(float)maxValue*barHeight-viewMargin,paint);
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            canvas.drawPoint(startX,mHeight-(reportData.getAverageDbpData())/(float)maxValue*barHeight-viewMargin,paint);
        }
    }

    private void DrawWeekPressure(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-7*barWidth)/6f;
        for (int i = 0;i<pressureWeekList.size();i++){
            ReportData data = pressureWeekList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange));
            startY = mHeight-viewMargin-data.getAverageData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinDbpData()/maxValue;
            stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxDbpData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            startY = mHeight-viewMargin-data.getAverageDbpData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);
        }
    }

    private void DrawMonthPressure(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setStyle(Paint.Style.STROKE);

        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        for (int i = 0;i<pressureMonthList.size();i++){
            ReportData data = pressureMonthList.get(i);
            if (data.getAverageData()==0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getTime()*1000);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            float startX = viewMargin+barWidth/2 + (interval + barWidth) * (day-1);
            float startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinData()/maxValue;
            float stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange));
            startY = mHeight-viewMargin-data.getAverageData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            startY = mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMinDbpData()/maxValue;
            stopY =mHeight-viewMargin-(mHeight-viewMargin)*2f/3f*data.getMaxDbpData()/maxValue;
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue_background));
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            startY = mHeight-viewMargin-data.getAverageDbpData()/maxValue*(mHeight-viewMargin)*2f/3f;
            stopY =startY+2*dpValue;
            canvas.drawLine(startX,startY,startX,
                    stopY, paint);

        }
    }

    private void DrawTouchRect(Canvas canvas,boolean isHour){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.healthy_gray_text));
        paint.setStrokeWidth(0.5f);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{8,8},0);
        paint.setPathEffect(dashPathEffect);
        float interval = (mWidth-2*viewMargin-touchDataList.size()*barWidth)/(touchDataList.size()-1);
        float x = viewMargin+barWidth/2 + (interval + barWidth) * index;
        Path line = new Path();
        line.moveTo(x,mHeight/3-viewMargin);
        line.lineTo(x,mHeight-viewMargin);
        canvas.drawPath(line,paint);

        if (x<50*dpValue)
            x = 50*dpValue;
        if (x>mWidth-50*dpValue)
            x = mWidth-50*dpValue;

        canvas.drawRoundRect(x-50*dpValue,10*dpValue,x+50*dpValue,mHeight/3-viewMargin,10*dpValue,
                10*dpValue,squareBackPaint);
        String dataStr = "",timeStr = "";
        if (sleepWeekList!=null||sleepMonthList!=null){
            dataStr = String.format("%dh%dmin", touchDataList.get(index).getAverageData()/60,touchDataList.get(index).getAverageData()%60);
        } else if (tempDayList!=null||tempWeekList!=null||tempMonthList!=null){
            UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getContext()));
            if (userModel==null)
                return;
            if (userModel.tempUnit==0)
                dataStr = touchDataList.get(index).getAverageData()==0?
                                "-- ℃":
                                String.format("%.1f ℃", touchDataList.get(index).getAverageData()/10f);
            else
                dataStr = touchDataList.get(index).getAverageData()==0?
                        "-- ℉":
                        String.format("%.1f ℉", MathUtil.newInstance().c2f(touchDataList.get(index).getAverageData()/10f));
        }else if (pressureDayList!=null||pressureWeekList!=null||pressureMonthList!=null){
            dataStr =
                    touchDataList.get(index).getAverageData()==0?
                            "--/--":
                            String.format("%d/%d", touchDataList.get(index).getAverageData(),touchDataList.get(index).getAverageDbpData());
        }else
            dataStr = touchDataList.get(index).getAverageData()==0?
                    "--":
                    String.format("%d", touchDataList.get(index).getAverageData());
        timeStr = DateUtil.getStringDateFromSecond(touchDataList.get(index).getTime(),isHour?"MM-dd HH:mm":"yyyy/MM/dd EE");
        float touchDataTextWidth = touchDataTextPaint.measureText(dataStr);
        float timeTextWidth = timeTextPaint.measureText(timeStr);
        canvas.drawText(dataStr, (x-(touchDataTextWidth)/2),
                mHeight/3-23*dpValue, touchDataTextPaint);
        canvas.drawText(timeStr,(x-timeTextWidth/2),
                10*dpValue+18*dpValue,timeTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchDataList==null||touchDataList.size()==0)
            return super.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                isTouchAble = true;
                if (x<viewMargin){
                    index = 0;
                }else if (x>mWidth-viewMargin){
                    index = touchDataList.size()-1;
                }else {
                    for (int i = 0, j = i+1; i< touchDataList.size()-1; i++,j++){
                        if ((x-(dpValue*19+(mWidth-19*dpValue*2)/(touchDataList.size()-1)*i))*
                                (x-(dpValue*19+(mWidth-19*dpValue*2)/(touchDataList.size()-1)*j))<0){
                            index = Math.abs((x-(dpValue*19+(mWidth-19*dpValue*2)/(touchDataList.size()-1)*i)))>
                                    Math.abs((x-(dpValue*19+(mWidth-19*dpValue*2)/(touchDataList.size()-1)*j)))?j:i;
                            break;
                        }
                    }
                }
                break;
        }
        postInvalidate();
        return super.onTouchEvent(event);
    }


    public void setStepMonthList(List<ReportData> stepMonthList,int maxValue) {
        this.stepMonthList = stepMonthList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = stepMonthList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setStepWeekList(List<ReportData> stepWeekList, int maxValue) {
        this.stepWeekList = stepWeekList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = stepWeekList;
        barWidth = 32f;
        postInvalidate();
    }

    public void setStepDayList(List<ReportData> stepDayList, int maxValue) {
        this.stepDayList = stepDayList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = stepDayList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setSleepDayList(List<ReportData> sleepDayList,int startTime,int allTime) {
        this.sleepDayList = sleepDayList;
        this.startTime = startTime;
        this.allTime = allTime;
        postInvalidate();
    }

    public void setSleepWeekList(List<ReportData> sleepWeekList,int maxValue) {
        this.sleepWeekList = sleepWeekList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = sleepWeekList;
        barWidth = 32f;
        postInvalidate();
    }

    public void setSleepMonthList(List<ReportData> sleepMonthList,int maxValue) {
        this.sleepMonthList = sleepMonthList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = sleepMonthList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setHeartDayList(List<ReportData> heartDayList,int maxValue) {
        this.heartDayList = heartDayList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = heartDayList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setHeartWeekList(List<ReportData> heartWeekList,int maxValue) {
        this.heartWeekList = heartWeekList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = heartWeekList;
        barWidth = 52f;
        postInvalidate();
    }

    public void setHeartMonthList(List<ReportData> heartMonthList,int maxValue) {
        this.heartMonthList = heartMonthList;
        this.maxValue = maxValue*1.2f;
        this.touchDataList = heartMonthList;
        barWidth = 18f;
        postInvalidate();
    }

    public void setOxygenDayList(List<ReportData> oxygenDayList) {
        this.oxygenDayList = oxygenDayList;
        maxValue = 15;
        touchDataList = oxygenDayList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setOxygenWeekList(List<ReportData> oxygenWeekList) {
        this.oxygenWeekList = oxygenWeekList;
        maxValue = 15;
        touchDataList = oxygenWeekList;
        barWidth = 52f;
        postInvalidate();
    }

    public void setOxygenMonthList(List<ReportData> oxygenMonthList) {
        this.oxygenMonthList = oxygenMonthList;
        maxValue = 15;
        touchDataList = oxygenMonthList;
        barWidth = 18f;
        postInvalidate();
    }

    public void setTempDayList(List<ReportData> tempDayList) {
        this.tempDayList = tempDayList;
        maxValue = 100;
        touchDataList = tempDayList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setTempWeekList(List<ReportData> tempWeekList) {
        this.tempWeekList = tempWeekList;
        maxValue = 100;
        touchDataList = tempWeekList;
        barWidth = 52f;
        postInvalidate();
    }

    public void setTempMonthList(List<ReportData> tempMonthList) {
        this.tempMonthList = tempMonthList;
        maxValue = 100;
        touchDataList = tempMonthList;
        barWidth = 18f;
        postInvalidate();
    }

    public void setPressureDayList(List<ReportData> pressureDayList) {
        this.pressureDayList = pressureDayList;
        maxValue = 180;
        touchDataList = pressureDayList;
        barWidth = 16f;
        postInvalidate();
    }

    public void setPressureWeekList(List<ReportData> pressureWeekList) {
        this.pressureWeekList = pressureWeekList;
        maxValue = 180;
        touchDataList = pressureWeekList;
        barWidth = 52f;
        postInvalidate();
    }

    public void setPressureMonthList(List<ReportData> pressureMonthList) {
        this.pressureMonthList = pressureMonthList;
        maxValue = 180;
        touchDataList = pressureMonthList;
        barWidth = 18f;
        postInvalidate();
    }
}
