package com.szip.healthy.View;

import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.szip.healthy.Activity.CalendarFragment;


/**
 * Created by Administrator on 2019/12/31.
 */

public class CalendarPicker {

    private static final String TAG = "CalendarPicker";

    private static CalendarPicker mInstance;

    private String date;
    private int flag;

    private CalendarPicker(){}

    public static CalendarPicker getInstance(){
        if (mInstance == null){
            synchronized (CalendarPicker.class){
                if (mInstance == null){
                    mInstance = new CalendarPicker();
                }
            }
        }
        return mInstance;
    }

    private FragmentManager mFragmentManager;

    private boolean enableAnim;
    private int mAnimStyle;
    private CalendarListener calendarListener;

    public CalendarPicker setFragmentManager(FragmentManager fm) {
        this.mFragmentManager = fm;
        return this;
    }

    /**
     * 设置动画效果
     * @param animStyle
     * @return
     */
    public CalendarPicker setAnimationStyle(@StyleRes int animStyle) {
        this.mAnimStyle = animStyle;
        return this;
    }


    public CalendarPicker setDate(String date){
        this.date = date;
        return this;
    }

    public CalendarPicker setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    /**
     * 启用动画效果，默认为false
     * @param enable
     * @return
     */
    public CalendarPicker enableAnimation(boolean enable){
        this.enableAnim = enable;
        return this;
    }

    /**
     * 设置选择结果的监听器
     * @param listener
     * @return
     */
    public CalendarPicker setCalendarListener(CalendarListener listener){
        this.calendarListener = listener;
        return this;
    }

    public void  show(){
        if (mFragmentManager == null){
            throw new UnsupportedOperationException("CityPicker：method setFragmentManager() must be called.");
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        final Fragment prev = mFragmentManager.findFragmentByTag(TAG);
        if (prev != null){
            ft.remove(prev).commit();
            ft = mFragmentManager.beginTransaction();
        }
        ft.addToBackStack(null);
        final CalendarFragment calendarFragment =
                CalendarFragment.newInstance(enableAnim);
        calendarFragment.setAnimationStyle(mAnimStyle);
        calendarFragment.setCalendarListener(calendarListener);
        calendarFragment.setsetInitializeDate(date);
        calendarFragment.setFlag(flag);
        calendarFragment.show(ft, TAG);
    }

    public interface CalendarListener {
        void onClickDate(String date);
    }

}
