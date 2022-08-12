package com.necer.helper;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.necer.calendar.BaseCalendar;
import com.necer.enumeration.CalendarType;
import com.necer.painter.CalendarAdapter;
import com.necer.painter.CalendarPainter;
import com.necer.utils.CalendarUtil;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class CalendarHelper {

    private int mLineNum;//行数
    protected LocalDate mPagerInitialDate;//当前页面的初始化日期
    private BaseCalendar mCalendar;

    private CalendarType mCalendarType;

    protected RectF mBgRectF;//日历背景
    private List<LocalDate> mAllSelectListDate;//当前页面选中的日期
    protected List<LocalDate> mDateList;//页面的数据集合
    protected List<RectF> mRectFList;
    private GestureDetector mGestureDetector;

    public CalendarHelper(BaseCalendar calendar, LocalDate pagerInitialDate, CalendarType calendarType) {
        this.mCalendar = calendar;
        this.mCalendarType = calendarType;
        this.mPagerInitialDate = pagerInitialDate;
        mDateList = calendarType == CalendarType.MONTH
                ? CalendarUtil.getMonthCalendar(mPagerInitialDate, mCalendar.getFirstDayOfWeek(), mCalendar.isAllMonthSixLine())
                : CalendarUtil.getWeekCalendar(mPagerInitialDate, mCalendar.getFirstDayOfWeek());

        mLineNum = mDateList.size() / 7;

        mRectFList = getLocationRectFList();

        mAllSelectListDate = mCalendar.getAllSelectDateList();

        mBgRectF = new RectF(0f, 0f, calendar.getMeasuredWidth(), calendar.getMeasuredHeight());

        mGestureDetector = new GestureDetector(calendar.getContext(), mSimpleOnGestureListener);
    }

    /**
     * 分配空间
     * @return
     */
    private List<RectF> getLocationRectFList() {
        List<RectF> rectFList = new ArrayList<>();
        for (int i = 0; i < mDateList.size(); i++) {
            rectFList.add(new RectF());
        }
        return rectFList;
    }


    /**
     * 获取当前的位置，拉伸日历时会变化，其他状态不会变
     * @param lineIndex
     * @param columnIndex
     * @return
     */
    public RectF getRealRectF(int lineIndex, int columnIndex) {
        int index = lineIndex * 7 + columnIndex;
        return resetRectFSize(mRectFList.get(index), lineIndex, columnIndex);
    }

    /**
     * 重新确定位置，拉伸日历用到
     */
    public void resetRectFSize() {
        for (int i = 0; i < mLineNum; i++) {
            for (int j = 0; j < 7; j++) {
                int index = i * 7 + j;
                resetRectFSize(mRectFList.get(index), i, j);
            }
        }
    }

    public RectF resetRectFSize(RectF rectF, int lineIndex, int columnIndex) {
        //矩形重新确定确定位置
        float width = mCalendar.getMeasuredWidth();
        float height = mCalendar.getMeasuredHeight();
        //为每个矩形确定位置
        if (mLineNum == 5 || mLineNum == 1) {
            //5行的月份，5行矩形平分view的高度  mLineNum==1是周的情况
            float rectHeight = height / mLineNum;
            rectF.set(columnIndex * width / 7, lineIndex * rectHeight, columnIndex * width / 7 + width / 7, lineIndex * rectHeight + rectHeight);
        } else {
            //6行的月份，要第一行和最后一行矩形的中心分别和和5行月份第一行和最后一行矩形的中心对齐
            //5行一个矩形高度 mHeight/5, 画图可知,4个5行矩形的高度等于5个6行矩形的高度  故：6行的每一个矩形高度是  (mHeight/5)*4/5
            float rectHeight5 = height / 5;
            float rectHeight6 = (height / 5) * 4 / 5;
            rectF.set(columnIndex * width / 7, lineIndex * rectHeight6 + (rectHeight5 - rectHeight6) / 2, columnIndex * width / 7 + width / 7, lineIndex * rectHeight6 + rectHeight6 + (rectHeight5 - rectHeight6) / 2);
        }
        return rectF;
    }

    public int getLineNum() {
        return mLineNum;
    }

    public LocalDate getPagerInitialDate() {
        return mPagerInitialDate;
    }

    public CalendarType getCalendarType() {
        return mCalendarType;
    }

    public int getCalendarHeight() {
        return mCalendar.getMeasuredHeight();
    }


    public RectF getBgRectF() {
        return mBgRectF;
    }

    public CalendarPainter getCalendarPainter() {
        return mCalendar.getCalendarPainter();
    }

    public CalendarAdapter getCalendarAdapter() {
        return mCalendar.getCalendarAdapter();
    }

    public List<LocalDate> getAllSelectListDate() {
        return mAllSelectListDate;
    }

    public List<LocalDate> getDateList() {
        return mDateList;
    }

    public LocalDate getMiddleLocalDate() {
        return mDateList.get((mDateList.size() / 2) + 1);
    }

    /**
     * localDate 到顶部的距离
     * @param localDate
     * @return
     */
    public int getDistanceFromTop(LocalDate localDate) {
        int monthCalendarOffset;
        //选中的是第几行   对于没有选中的默认折叠中心是第一行，有选中的默认折叠中心是选中的第一个日期
        int selectIndex = mDateList.indexOf(localDate) / 7;
        if (mLineNum == 5) {
            //5行的月份
            monthCalendarOffset = mCalendar.getMeasuredHeight() / 5 * selectIndex;
        } else {
            int rectHeight6 = (mCalendar.getMeasuredHeight() / 5) * 4 / 5;
            monthCalendarOffset = rectHeight6 * selectIndex;
        }
        return monthCalendarOffset;
    }

    /**
     * 获取中心点 ，即月周切换的中心日期
     * @return
     */
    public LocalDate getPivotDate() {
        LocalDate today = new LocalDate();
        if (getCurrentSelectDateList().size() != 0) {
            return getCurrentSelectDateList().get(0);
        } else if (mDateList.contains(today)) {
            return today;
        } else {
            return mDateList.get(0);
        }
    }

    /**
     * 中心点到顶部的距离
     * @return
     */
    public int getPivotDistanceFromTop() {
        return getDistanceFromTop(getPivotDate());
    }

    public List<LocalDate> getCurrentSelectDateList() {
        List<LocalDate> currentSelectDateList = new ArrayList<>();
        for (int i = 0; i < mDateList.size(); i++) {
            LocalDate localDate = mDateList.get(i);
            if (mAllSelectListDate != null && mAllSelectListDate.contains(localDate)) {
                currentSelectDateList.add(localDate);
            }
        }
        return currentSelectDateList;
    }

    public List<LocalDate> getCurrentDateList() {
        return mDateList;
    }

    public void dealClickDate(LocalDate localDate) {
        if (mCalendarType == CalendarType.MONTH && CalendarUtil.isLastMonth(localDate, mPagerInitialDate)) {
            mCalendar.onClickLastMonthDate(localDate);
        } else if (mCalendarType == CalendarType.MONTH && CalendarUtil.isNextMonth(localDate, mPagerInitialDate)) {
            mCalendar.onClickNextMonthDate(localDate);
        } else {
            mCalendar.onClickCurrectMonthOrWeekDate(localDate);
        }
    }


    public LocalDate getFirstDate() {
        if (mCalendarType == CalendarType.MONTH) {
            return new LocalDate(mPagerInitialDate.getYear(), mPagerInitialDate.getMonthOfYear(), 1);
        } else {
            return mDateList.get(0);
        }
    }

    public boolean isAvailableDate(LocalDate localDate) {
        return mCalendar.isAvailable(localDate);
    }

    public boolean isCurrentMonthOrWeek(LocalDate localDate) {
        if (mCalendarType == CalendarType.MONTH) {
            return CalendarUtil.isEqualsMonth(localDate, mPagerInitialDate);
        } else {
            return mDateList.contains(localDate);
        }
    }


    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < mRectFList.size(); i++) {
                RectF rectF = mRectFList.get(i);
                if (rectF.contains((int) e.getX(), (int) e.getY())) {
                    LocalDate clickDate = mDateList.get(i);
                    dealClickDate(clickDate);
                    break;
                }
            }
            return true;
        }
    };

    /**
     * 背景的初始位置为月日历的高度-周日的高度
     *
     * @return
     */
    public int getInitialDistance() {
        return mCalendar.getMeasuredHeight() * 4 / 5;
    }


}
