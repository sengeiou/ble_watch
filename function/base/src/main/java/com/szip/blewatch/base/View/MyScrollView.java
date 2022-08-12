package com.szip.blewatch.base.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.szip.blewatch.base.Interfere.OnScrollListener;
import com.szip.blewatch.base.Util.LogUtil;

public class MyScrollView extends ScrollView {

    private OnScrollListener onScrollListener;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener != null) {
            onScrollListener.onScroll(t);
            View view = (View)getChildAt(getChildCount()-1);
            int d = view.getBottom();
            d -= (getHeight()+getScrollY());
            if(d==0)
            {
                onScrollListener.onLoadMost();
            }

        }
    }

    /**
     * 接口对外公开
     * @param onScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     *
     * 滚动的回调接口
     *
     * @author xiaanming
     *
     */

}
