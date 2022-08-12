package com.szip.blewatch.base.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.szip.blewatch.base.Interfere.OnScrollListener;
import com.szip.blewatch.base.R;
import com.szip.blewatch.base.Util.LogUtil;

/**
 * Created by Administrator on 2019/12/1.
 */

public abstract class BaseFragment extends Fragment {
    protected View mRootView;
    private TextView titleTv,titleBigTv;
    private MyScrollView myScrollView;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null){
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        afterOnCreated(savedInstanceState);
    }

    protected abstract int getLayoutId();
    protected abstract void afterOnCreated(Bundle savedInstanceState);

    protected void showToast(String string){
        Toast.makeText(getActivity(), string,Toast.LENGTH_LONG).show();
    }

    protected void setTitle(String msg){
        if(mRootView == null)
            return;

        titleTv = mRootView.findViewById(R.id.titleTv);
        titleBigTv = mRootView.findViewById(R.id.titleBigTv);
        myScrollView = mRootView.findViewById(R.id.scroll);

        if (titleBigTv!=null)
            titleBigTv.setText(msg);
        if (titleTv!=null)
            titleTv.setText(msg);

        if (titleBigTv==null&&titleTv!=null)
            titleTv.setVisibility(View.VISIBLE);

        if (myScrollView==null||titleTv==null)
            return;

        myScrollView.setOnScrollListener(listener);
    }

    private OnScrollListener listener = new OnScrollListener() {
        @Override
        public void onScroll(int scrollY) {
            if (titleTv!=null){
                if (scrollY>120)
                    titleTv.setVisibility(View.VISIBLE);
                else
                    titleTv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadMost() {

        }

    };
}
