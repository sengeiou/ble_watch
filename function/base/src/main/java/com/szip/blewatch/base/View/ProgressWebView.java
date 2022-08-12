package com.szip.blewatch.base.View;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProgressWebView extends WebView {

    private ProgressBar progressBar;

    public ProgressWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        progressBar = new ProgressBar(context,null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,10,0,0));
        progressBar.setBackgroundColor(Color.BLACK);
        addView(progressBar);
        setWebChromeClient(new WebChromeClient());
    }

    private class WebChromeClient extends android.webkit.WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress==100){
                progressBar.setVisibility(GONE);
            }else {
                if (progressBar.getVisibility()==GONE)
                    progressBar.setVisibility(VISIBLE);
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);

        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
