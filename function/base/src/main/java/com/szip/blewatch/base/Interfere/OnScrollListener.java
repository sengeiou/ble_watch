package com.szip.blewatch.base.Interfere;

public interface OnScrollListener{
    /**
     * 回调方法， 返回MyScrollView滑动的Y方向距离
     * @param scrollY
     *              、
     */
    void onScroll(int scrollY);
    void onLoadMost();
}
