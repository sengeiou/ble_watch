package com.zaaach.citypicker;


import com.szip.blewatch.base.Model.ContactModel;
import com.zaaach.citypicker.adapter.OnContactPickListener;
import com.zaaach.citypicker.adapter.OnPickListener;

import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

/**
 * @Author: Bro0cL
 * @Date: 2018/2/6 17:52
 */
public class Picker {
    private static final String TAG = "CityPicker";

    private static Picker mInstance;

    private Picker(){}

    public static Picker getInstance(){
        if (mInstance == null){
            synchronized (Picker.class){
                if (mInstance == null){
                    mInstance = new Picker();
                }
            }
        }
        return mInstance;
    }

    private FragmentManager mFragmentManager;
    private Fragment mTargetFragment;

    private boolean enableAnim;
    private int mAnimStyle;
    private OnPickListener mOnPickListener;
    private OnContactPickListener onContactPickListener;

    public Picker setFragmentManager(FragmentManager fm) {
        this.mFragmentManager = fm;
        return this;
    }

    public Picker setTargetFragment(Fragment targetFragment) {
        this.mTargetFragment = targetFragment;
        return this;
    }

    /**
     * 设置动画效果
     * @param animStyle
     * @return
     */
    public Picker setAnimationStyle(@StyleRes int animStyle) {
        this.mAnimStyle = animStyle;
        return this;
    }


    /**
     * 启用动画效果，默认为false
     * @param enable
     * @return
     */
    public Picker enableAnimation(boolean enable){
        this.enableAnim = enable;
        return this;
    }

    /**
     * 设置选择结果的监听器
     * @param listener
     * @return
     */
    public Picker setOnPickListener(OnPickListener listener){
        this.mOnPickListener = listener;
        return this;
    }

    public Picker setOnContactPickListener(OnContactPickListener onContactPickListener) {
        this.onContactPickListener = onContactPickListener;
        return this;
    }

    public void show(){
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
        final CityPickerDialogFragment cityPickerFragment =
                CityPickerDialogFragment.newInstance(enableAnim);
        cityPickerFragment.setAnimationStyle(mAnimStyle);
        cityPickerFragment.setOnPickListener(mOnPickListener);
        if (mTargetFragment != null){
            cityPickerFragment.setTargetFragment(mTargetFragment, 0);
        }
        cityPickerFragment.show(ft, TAG);
    }

    public void showContact(ArrayList<ContactModel> contactModels){
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
        final ContactPickerDialogFragment contactFragment =
                ContactPickerDialogFragment.newInstance(enableAnim);
        contactFragment.setAnimationStyle(mAnimStyle);
        contactFragment.setOnContactPickListener(onContactPickListener);
        contactFragment.setFilerContacts(contactModels);
        if (mTargetFragment != null){
            contactFragment.setTargetFragment(mTargetFragment, 0);
        }
        contactFragment.show(ft, TAG);
    }
}
