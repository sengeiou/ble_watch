package com.szip.blewatch.base.View;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

/**
 * Created by zhangmin on 2018/3/31.
 */

public class PulldownUpdateView extends LinearLayout{
    private View mAutoComeBackView;
    private Point mAutoBackOriginPos = new Point();
    private ViewDragHelper mDragHelper;
    private PulldownListener listener;

    public PulldownUpdateView(Context context) {
        super(context);
    }

    public PulldownUpdateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }



            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - mAutoComeBackView.getWidth() - getPaddingLeft();
                final int newleft = Math.min(Math.max(left,leftBound),rightBound);
                return newleft;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - mAutoComeBackView.getHeight() - getPaddingTop();
                final int newTop = Math.min(Math.max(top,topBound),bottomBound);
                return newTop;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (releasedChild == mAutoComeBackView){
                    mDragHelper.settleCapturedViewAt(mAutoBackOriginPos.x,mAutoBackOriginPos.y);
                    invalidate();
                    if (getWidth()- (Math.abs(mAutoBackOriginPos.x-mAutoComeBackView.getLeft())+ mAutoComeBackView.getWidth())<10)
                        if (listener!=null){
                            listener.updateNow();
                        }
                }
            }
        });
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)){
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mAutoBackOriginPos.x = mAutoComeBackView.getLeft();
        mAutoBackOriginPos.y = mAutoComeBackView.getTop();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    public void setListener(PulldownListener listener){
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoComeBackView = getChildAt(0);
    }

    public interface PulldownListener{
        void updateNow();
    }
}
