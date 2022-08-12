package com.szip.healthy.View;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;

import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Const.CalendarConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.healthy.Adapter.DateAdapter;
import com.szip.healthy.Model.DateBean;
import com.szip.healthy.Model.MonthBean;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarView extends LinearLayout {


    private boolean startWithSunday = true;
    private int maxMonth = 2;
    private int beforeMonth = 1;
    private RecyclerView rvDate;
    private Context context;
    private DateAdapter adapter = new DateAdapter();
    private ArrayList<DateBean> mList = new ArrayList<>();
    private ArrayList<MonthBean> monthList;

    private Calendar calendar = Calendar.getInstance();
    private int currentYear = calendar.get(Calendar.YEAR);
    private int currentMonth = calendar.get(Calendar.MONTH) + 1;
    private int currentDate = calendar.get(Calendar.DATE);

    public CalendarView(Context context) {
        super(context);
        this.context = context;

    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(attrs);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs);
    }

    public void initView(AttributeSet attrs){
        rvDate = new RecyclerView(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
        boolean isShowWeek = typedArray.getBoolean(R.styleable.CalendarView_showWeek, true);
        maxMonth = typedArray.getInt(R.styleable.CalendarView_maxMonth, maxMonth);
        typedArray.recycle();
        setOrientation(VERTICAL);
        if (isShowWeek) {
            addWeekView();
        }
        initDate();
        addCalendarView();
    }


    private void addWeekView() {


        LinearLayout week = new LinearLayout(context);
        week.setBackgroundColor(ContextCompat.getColor(context, R.color.bgColor));
        LayoutParams headParams =
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        MathUtil.newInstance().dipToPx(30f,context));
        week.setLayoutParams(headParams);
        week.setOrientation(HORIZONTAL);
        week.setPadding(MathUtil.newInstance().dipToPx(15f,context), 0, MathUtil.newInstance().dipToPx(15f,context), 0);

        String[] weekDay = new String[]{context.getString(R.string.sun),context.getString(R.string.mon), context.getString(R.string.tues),
               context.getString(R.string.wed), context.getString(R.string.thus), context.getString(R.string.fri),context.getString(R.string.sta)};
        LayoutParams itemParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        itemParams.weight = 1.5f;
        for (String i : weekDay) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(itemParams);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            tv.setText(i);
            week.addView(tv);
        }
        addView(week);
    }


    private void initDate() {
        monthList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        // 控制月份本月之前显示几个月
        calendar.add(Calendar.MONTH, -(maxMonth-2));
        for (int i = 0;i<maxMonth;i++) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            MonthBean bean = new MonthBean(year, month);
            monthList.add(bean);
            calendar.add(Calendar.MONTH, 1);
        }

        for (MonthBean bean : monthList) {
            ArrayList<DateBean> dateList = new ArrayList<>();
            // 这个月的第一天
            calendar.set(bean.getYear(), bean.getMonth() - 1, 1);
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // 第一天之前空几天 起始的天数不同的话，
            int firstOffset = dayOfWeek - 1;
            //当月最后一天
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            int dayOfSum = calendar.get(Calendar.DATE);
            // 这个月起始的空占位符
            for (int i = 0;i<firstOffset;i++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        0,
                        CalendarConst.TYPE_DATE_BLANK);
                dateList.add(dateBean);
            }
            // 这个月的所有日期
            for (int i = 0;i<dayOfSum;i++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        i + 1,
                        CalendarConst.TYPE_DATE_NORMAL);
                checkIsToday(dateBean);
                dateList.add(dateBean);
            }
            // 这个月结束的空白占位符
            int lastDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int lastOffset = 7 - lastDayOfWeek;
            for (int i = 0;i<lastOffset;i++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        0,
                        CalendarConst.TYPE_DATE_BLANK
                );
                dateList.add(dateBean);
            }
            bean.setDateBeans(dateList);
        }
        // 添加头部月份title，同时完成整个月份Adapter内List的构造
        for (MonthBean bean : monthList) {
            DateBean titleBean = new DateBean(
                    bean.getYear(),
                    bean.getMonth(),
                    0,
                    CalendarConst.TYPE_DATE_TITLE
            );
            mList.add(titleBean);
            mList.addAll(bean.getDateBeans());
        }
    }

    private void updateCalendar(){

        Calendar calendar = Calendar.getInstance();
        // 控制月份本月之前显示几个月
        if(monthList!=null&&monthList.size()!=0){
            MonthBean monthBean = monthList.get(0);
            calendar.set(Calendar.YEAR,monthBean.getYear());
            calendar.set(Calendar.MONTH,monthBean.getMonth()-1);
        }
        for (int i = 0;i<2;i++) {
            calendar.add(Calendar.MONTH, -1);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            MonthBean bean = new MonthBean(year, month);
            monthList.add(0,bean);
        }

        for (int i = 0;i<2;i++) {
            MonthBean bean = monthList.get(i);
            ArrayList<DateBean> dateList = new ArrayList<>();
            // 这个月的第一天
            calendar.set(bean.getYear(), bean.getMonth() - 1, 1);
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // 第一天之前空几天 起始的天数不同的话，
            int firstOffset = dayOfWeek - 1;
            //当月最后一天
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            int dayOfSum = calendar.get(Calendar.DATE);
            // 这个月起始的空占位符
            for (int a = 0;a<firstOffset;a++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        0,
                        CalendarConst.TYPE_DATE_BLANK);
                dateList.add(dateBean);
            }
            // 这个月的所有日期
            for (int a = 0;a<dayOfSum;a++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        a + 1,
                        CalendarConst.TYPE_DATE_NORMAL);
                checkIsToday(dateBean);
                dateList.add(dateBean);
            }
            // 这个月结束的空白占位符
            int lastDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int lastOffset = 7 - lastDayOfWeek;
            for (int a = 0;a<lastOffset;a++) {
                DateBean dateBean = new DateBean(
                        currentYear,
                        currentMonth + 1,
                        0,
                        CalendarConst.TYPE_DATE_BLANK
                );
                dateList.add(dateBean);
            }
            bean.setDateBeans(dateList);
        }

        int pos = 0;
        // 添加头部月份title，同时完成整个月份Adapter内List的构造
        RecyclerView.LayoutManager layoutManager = rvDate.getLayoutManager();
        //判断是当前layoutManager是否为LinearLayoutManager
        // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            //获取最后一个可见view的位置
            pos = linearManager.findLastVisibleItemPosition()-7;
        }

        for (int i = 1;i>=0;i--) {
            MonthBean bean = monthList.get(i);
            pos+=(bean.getDateBeans().size()+1);
            DateBean titleBean = new DateBean(
                    bean.getYear(),
                    bean.getMonth(),
                    0,
                    CalendarConst.TYPE_DATE_TITLE
            );
            mList.addAll(0,bean.getDateBeans());
            mList.add(0,titleBean);
        }
        adapter.setDateBeans(mList);
        rvDate.scrollToPosition(pos);
    }

    private void addCalendarView() {
        LayoutParams rvParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 7);
        this.adapter.setDateBeans(mList);
        rvDate.setAdapter(adapter);
        rvDate.setLayoutParams(rvParams);
        rvDate.setLayoutManager(gridLayoutManager);
        rvDate.setPadding(MathUtil.newInstance().dipToPx( 15f,context), 0, MathUtil.newInstance().dipToPx(15f,context), 0);
        rvDate.setClipChildren(false);
        rvDate.setClipToPadding(false);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                return mList.get(i).getType() == CalendarConst.TYPE_DATE_TITLE? 7 : 1;
            }
        });

        addView(rvDate);
        int todayPos = -1;
        for (int i = 0;i<mList.size();i++){
            if (mList.get(i).isToday()){
                todayPos = i;
                break;
            }
        }
        if (todayPos != -1)
            rvDate.scrollToPosition(todayPos);
        else
            rvDate.scrollToPosition(mList.size() - 1);


        rvDate.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!rvDate.canScrollVertically(-1)){
                    Log.i("data******", "direction -1: false");//滑动到顶部
                    updateCalendar();
                }
            }
        });
    }

    private void  checkIsToday(DateBean bean) {
        if (bean.getYear() == currentYear && bean.getMonth() == currentMonth && bean.getDay() == currentDate) {
            bean.setToday(true);
            bean.setChooseDay(true);
        }
    }

//    public void  setDateClick(RvItemClickListener rvItemClickListener) {
//        adapter.setOnItemClickListener(rvItemClickListener);
//    }
//
//
//    fun updateSelectDay(bean: DateBean) {
//        val beforeBean = mList.find { it.isChooseDay }
//        val beforePosition = mList.indexOfFirst { it.isChooseDay }
//        if (bean == beforeBean) return
//                beforeBean?.run { isChooseDay = false }
//        bean.isChooseDay = true
//        val selectPos = mList.indexOf(bean)
//        if (beforePosition != -1) adapter.notifyItemChanged(beforePosition)
//        if (selectPos != -1) adapter.notifyItemChanged(selectPos)
//    }

    public interface RvItemClickListener{
        void onClick(DateBean bean, View view, int pos);
    }

}
