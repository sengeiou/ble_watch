package com.szip.blewatch.base.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseLazyLoadingFragment extends Fragment {
    protected View mRootView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        if(mRootView == null){
            mRootView = inflater.inflate(getLayoutId(), container, false);
            initView(mRootView);
            isViewCreated = true;
            if (!isHidden() && getUserVisibleHint())
                dispatchVisibleState(true);
//        }
        return mRootView;
    }

    protected abstract int getLayoutId();
    protected abstract void initView(View root);

    private boolean isViewCreated = false;//View是否已经被创建出来

    private boolean isFirstVisible = true;//当前Fragment是否是首次可见

    private boolean currentVisibleState = false;//当前真正的可见状态
    /**
     * 当第一次可见的时候(此方法，在View的一次生命周期中只执行一次)
     * 如果Fragment经历了onDestroyView，那么整个方法会再次执行
     * 重写此方法时，对Fragment全局变量进行 初始化
     * 具体的参照github demo
     */
    protected void onFragmentFirstVisible() {

    }

    /**
     * 当fragment变成可见的时候(可能会多次)
     */
    protected void onFragmentResume() {

    }

    /**
     * 当fragment变成不可见的时候(可能会多次)
     */
    protected void onFragmentPause() {

    }

    void dispatchVisibleState(boolean isVisible) {
        //为了兼容内嵌ViewPager的情况,分发时，还要判断父Fragment是不是可见
        if (isVisible == currentVisibleState) return;//如果目标值，和当前值相同，那就别费劲了
        currentVisibleState = isVisible;//更新状态值
        if (isVisible) {//如果可见
            //那就区分是第一次可见，还是非第一次可见
            if (isFirstVisible) {
                isFirstVisible = false;
                onFragmentFirstVisible();
            }
            onFragmentResume();
        } else {
            onFragmentPause();
        }
    }


    @Override
    public void onDestroyView() {//相对应的，当View被销毁的时候，isViewCreated要变为false
        super.onDestroyView();
        reset();
    }

    private void reset() {
        isViewCreated = false;
        isFirstVisible = true;//还原成初始值
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isFirstVisible) {//只有在不是第一次可见的时候，才进入逻辑,由于isFirstVisible默认是true，所以，第一次进入onResume不会执行true分发
            if (!isHidden() && !currentVisibleState && getUserVisibleHint())//没有隐藏，当前状态为不可见，系统的可见hint为true 同时满足
                // 这个会发生在 Activity1 中 是ViewPager+Fragment时，如果从某个Fragment跳转到activity2，再跳回Activity1，那么 Activity1中的多个Fragment会同时执行onResume，
                //但是不会所有的fragment都是可见的，所以我只需要对可见的Fragment进行true分发
                dispatchVisibleState(true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentVisibleState && getUserVisibleHint()) {
            dispatchVisibleState(false);
        }
    }
    /**
     * 此方法和生命周期无关，由外部调用，只是作为一个可见不可见的参考
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //因为只有在Fragment的View已经被创建的前提下，UI处理才有意义，所以
        if (isViewCreated) {
            //为了逻辑严谨，必须当目前状态值和目标相异的时候，才去执行UI可见分发
            if (currentVisibleState && !isVisibleToUser) {
                dispatchVisibleState(false);
            } else if (!currentVisibleState && isVisibleToUser) {
                dispatchVisibleState(true);
            }
        }

    }

    /**
     * 这个，是在Fragment被hide/show的时候被调用
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            dispatchVisibleState(false);
        } else {
            dispatchVisibleState(true);
        }
    }
}
