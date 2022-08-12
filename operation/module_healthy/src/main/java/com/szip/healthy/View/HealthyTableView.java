package com.szip.healthy.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.healthy.R;
import com.szip.healthy.Model.HealthyData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HealthyTableView extends View {


    private int mWidth,mHeight;//本页面宽，高


    private Paint textXPaint = new Paint();//X坐标画笔

    private HealthyData healthyData;

    private float dpValue;


    public HealthyTableView(Context context) {
        super(context);
        initView();
    }

    public HealthyTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HealthyTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setHealthyData(HealthyData healthyData) {
        this.healthyData = healthyData;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    private void initView(){
        dpValue = MathUtil.newInstance().dip2Px(1,getContext());
        textXPaint.setTextSize(dpValue*7);
        textXPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (healthyData!=null){
            if (healthyData.getType() == HealthyConst.SLEEP){
                drawSleepView(canvas);
            }else {
                canvas.drawText("00:00", 0, mHeight-dpValue*3, textXPaint);
                float w = textXPaint.measureText("23:59");
                canvas.drawText("23:59", mWidth-w, mHeight-dpValue*3, textXPaint);
                if (healthyData.getType() == HealthyConst.HEART){
                    drawHeartView(canvas);
                }else {
                    drawBackgroundBar(canvas);
                    if (healthyData.getType() == HealthyConst.STEP){
                        drawStep(canvas);
                    }else if (healthyData.getType() == HealthyConst.BLOOD_PRESSURE){
                        drawBloodPressureView(canvas);
                    }else if (healthyData.getType() == HealthyConst.BLOOD_OXYGEN){
                        drawBloodOxygenView(canvas);
                    }else if (healthyData.getType() == HealthyConst.TEMPERATURE){
                        drawTemperature(canvas);
                    }
                }
            }
        }else {
            canvas.drawText("0", 0, mHeight-dpValue*3, textXPaint);
            float w = textXPaint.measureText("24");
            canvas.drawText("24", mWidth-w, mHeight-dpValue*3, textXPaint);
        }
    }

    private void drawTemperature(Canvas canvas) {
        if (healthyData.getAnimalHeatDataList()==null||healthyData.getAnimalHeatDataList().size()==0)
            return;
        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
        paint.setStrokeWidth(barWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        HashMap<Integer,Integer> hashMap = new HashMap<>();
        int sum = 0;
        for (int i = 0;i<healthyData.getAnimalHeatDataList().size();i++){
            int index = DateUtil.getHour(healthyData.getAnimalHeatDataList().get(i).time);
            int value = healthyData.getAnimalHeatDataList().get(i).tempData;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(healthyData.getAnimalHeatDataList().get(i-1).time);
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sum);
                sum = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sum++;

            if (i == healthyData.getAnimalHeatDataList().size()-1){
                hashMap.put(index,hashMap.get(index)/sum);
            }
        }

        float interval = (mWidth-barWidth*24)/23f;
        float barHeight = mHeight-dpValue*12-barWidth;
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            float startX = barWidth / 2 + (interval + barWidth) * entry.getKey();
            canvas.drawPoint(startX,mHeight-(entry.getValue()-350)/70f*barHeight-dpValue*12,paint);
        }
    }

    private void drawBloodOxygenView(Canvas canvas) {
        if (healthyData.getBloodOxygenDataList()==null||healthyData.getBloodOxygenDataList().size()==0)
            return;
        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
        paint.setStrokeWidth(barWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        HashMap<Integer,Integer> hashMap = new HashMap<>();
        int sum = 0;
        for (int i = 0;i<healthyData.getBloodOxygenDataList().size();i++){
            int index = DateUtil.getHour(healthyData.getBloodOxygenDataList().get(i).time);
            int value = healthyData.getBloodOxygenDataList().get(i).bloodOxygenData;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(healthyData.getBloodOxygenDataList().get(i-1).time);
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sum);
                sum = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sum++;

            if (i == healthyData.getBloodOxygenDataList().size()-1){
                hashMap.put(index,hashMap.get(index)/sum);
            }
        }

        float interval = (mWidth-barWidth*24)/23f;
        float barHeight = mHeight-dpValue*12-barWidth;
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            float startX = barWidth / 2 + (interval + barWidth) * entry.getKey();
            canvas.drawPoint(startX,mHeight-(entry.getValue()-90)/10f*barHeight-dpValue*12,paint);
        }

    }

    private void drawBloodPressureView(Canvas canvas) {
        if (healthyData.getBloodPressureDataList()==null||healthyData.getBloodPressureDataList().size()==0)
            return;
        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setStrokeWidth(barWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        HashMap<Integer,int[]>hashMap = new HashMap<>();
        int sum = 0;
        int max = 0;
        for (int i = 0;i<healthyData.getBloodPressureDataList().size();i++){
            int index = DateUtil.getHour(healthyData.getBloodPressureDataList().get(i).time);
            int value[] = new int[2];
            value[0] = healthyData.getBloodPressureDataList().get(i).sbpDate;
            value[1] = healthyData.getBloodPressureDataList().get(i).dbpDate;

            if (value[0]>max)
                max = value[0];
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(healthyData.getBloodPressureDataList().get(i-1).time);
                int oldValue[] = hashMap.get(oldIndex);
                oldValue[0]/=sum;
                oldValue[1]/=sum;
                hashMap.put(oldIndex,oldValue);
                sum = 0;
                hashMap.put(index,value);
            }else {
                int oldValue[] = hashMap.get(index);
                oldValue[0]+=value[0];
                oldValue[1]+=value[1];
                hashMap.put(index,oldValue);
            }
            sum++;

            if (i == healthyData.getBloodPressureDataList().size()-1){
                value[0]/=sum;
                value[1]/=sum;
                hashMap.put(index,value);
            }
        }
        max*=1.2;

        float interval = (mWidth-barWidth*24)/23f;
        float barHeight = mHeight-dpValue*12-barWidth;
        for (Map.Entry<Integer, int[]> entry : hashMap.entrySet()){
            float startX = barWidth / 2 + (interval + barWidth) * entry.getKey();
            paint.setColor(getContext().getResources().getColor(R.color.healthy_orange));
            canvas.drawPoint(startX,mHeight-entry.getValue()[0]/(float)max*barHeight-dpValue*12,paint);
            paint.setColor(getContext().getResources().getColor(R.color.healthy_blue));
            canvas.drawPoint(startX,mHeight-entry.getValue()[1]/(float)max*barHeight-dpValue*12,paint);
        }

    }

    private void drawHeartView(Canvas canvas) {
        if (healthyData.getHeartDataList()==null||healthyData.getHeartDataList().size()==0)
            return;
        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1f);
        Paint mPaintShader = new Paint();//阴影画笔

        canvas.drawLine(0,mHeight-dpValue*12,mWidth,mHeight-dpValue*12,paint);

        paint.setColor(getContext().getResources().getColor(R.color.healthy_red));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        HashMap<Integer,Integer> hashMap = new HashMap<>();
        int sum = 0;
        int max = 0;
        for (int i = 0;i<healthyData.getHeartDataList().size();i++){
            int index = DateUtil.getHour(healthyData.getHeartDataList().get(i).time);

            int value = healthyData.getHeartDataList().get(i).averageHeart;
            if (value>max)
                max = value;
            if (i==0){
                hashMap.put(index,value);
            }else if (hashMap.get(index)==null){
                int oldIndex = DateUtil.getHour(healthyData.getHeartDataList().get(i-1).time);
                int oldValue = hashMap.get(oldIndex);
                hashMap.put(oldIndex,oldValue/sum);
                sum = 0;
                hashMap.put(index,value);
            }else {
                hashMap.put(index,value+hashMap.get(index));
            }
            sum++;

            if (i == healthyData.getHeartDataList().size()-1){
                hashMap.put(index,hashMap.get(index)/sum);
            }
        }

        float interval = (mWidth-barWidth*24)/23f;
        float barHeight = mHeight-dpValue*12-barWidth;
        max*=1.2f;
        Path line = new Path();
        Path mPathShader = new Path();

        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0;i<24;i++){
            integers.add(0);
        }
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            integers.set(entry.getKey(),entry.getValue());
        }
        float startShader = 0;
        int lastPoint = 0;
        for (int i = 0;i<integers.size();i++){
            if (integers.get(i)==0)
                continue;
            float startX = barWidth / 2 + (interval + barWidth) * i;
            if (startShader == 0){
                startShader = startX;
                line.moveTo(startX,mHeight-integers.get(i)/(float)max*barHeight-dpValue*12);
                mPathShader.moveTo(startX,mHeight-integers.get(i)/(float)max*barHeight-dpValue*12);
            }else {
                line.lineTo(startX,mHeight-integers.get(i)/(float)max*barHeight-dpValue*12);
                mPathShader.lineTo(startX,mHeight-integers.get(i)/(float)max*barHeight-dpValue*12);
            }
            lastPoint = i;
        }
        mPathShader.lineTo(barWidth / 2 + (interval + barWidth) *lastPoint, mHeight-dpValue*12);
        mPathShader.lineTo(startShader, mHeight-dpValue*12);
        mPathShader.close();



        Shader mShader = new LinearGradient(0, 0, 0, getHeight(), 0x33FF0000,
                Color.TRANSPARENT, Shader.TileMode.MIRROR);
        mPaintShader.setShader(mShader);
        canvas.drawPath(line,paint);
        canvas.drawPath(mPathShader, mPaintShader);

    }

    private void drawSleepView(Canvas canvas) {
        if (healthyData.getDataStr()==null||healthyData.getDataStr().equals(""))
            return;
        String sleepStr[] = healthyData.getDataStr().split(",");
        int start = DateUtil.getMinue(sleepStr[0]);
        String startTime = String.format("%02d:%02d",start/60,start%60);
        int end = (start+healthyData.getData()+healthyData.getData1());
        String endTime = String.format("%02d:%02d",end%1440/60,end%1440%60);
        canvas.drawText(startTime, 0, mHeight-dpValue*3, textXPaint);
        float w = textXPaint.measureText(endTime);
        canvas.drawText(endTime, mWidth-w, mHeight-dpValue*3, textXPaint);

        int sleepData = healthyData.getData()+healthyData.getData1();
        Paint paint = new Paint();
        float startWidth = 0;
        float width;
        float top,bottom;
        for (int i = 1;i<sleepStr.length;i++){

            int value = Integer.valueOf(sleepStr[i].split(":")[0]);
            int state = Integer.valueOf(sleepStr[i].split(":")[1]);
            width = value/(float)sleepData*mWidth;
            if (state==2){//深睡
                top = (mHeight-dpValue*12)/2f;
                bottom = mHeight-dpValue*12;
                paint.setColor(getContext().getResources().getColor(R.color.healthy_ray));
            }else {
                top = 0;
                bottom = (mHeight-dpValue*12)/2f;
                paint.setColor(getContext().getResources().getColor(R.color.healthy_yellow));
            }
            RectF rectF = new RectF(startWidth,top,startWidth+width,bottom);
            canvas.drawRect(rectF,paint);
            startWidth+=width;
        }
    }

    private void drawBackgroundBar(Canvas canvas){
        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.bgColor));
        paint.setStrokeWidth(barWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float interval = (mWidth-barWidth*24)/23f;
        for (int i = 0;i<24;i++){
            float startX = barWidth / 2 + (interval + barWidth) * i;
            canvas.drawLine(startX,mHeight-dpValue*12,startX,barWidth/2,paint);
        }
    }

    private void drawStep(Canvas canvas){
        if (healthyData.getDataStr() == null||healthyData.getDataStr().equals(""))
            return;

        float barWidth = 8.0f;
        Paint paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.healthy_green));
        paint.setStrokeWidth(barWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        String [] strs = healthyData.getDataStr().split(",");
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        int maxValue = 0;
        for (String string:strs){
            String datas[] = string.split(":");
            int index = Integer.valueOf(datas[0]);
            int data = Integer.valueOf(datas[1]);
            if (data == 0)
                continue;
            hashMap.put(index,data);
            if (data>maxValue)
                maxValue = data;
        }
        maxValue *=1.2f;
        float interval = (mWidth-barWidth*24)/23f;
        float barHeight = mHeight-dpValue*12-barWidth;
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()){
            float startX = barWidth / 2 + (interval + barWidth) * entry.getKey();
            canvas.drawLine(startX,mHeight-dpValue*12,startX,mHeight-entry.getValue()/(float)maxValue*barHeight-dpValue*12,paint);
        }

    }
}
