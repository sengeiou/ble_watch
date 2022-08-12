package com.szip.user.Activity.search;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.szip.user.R;

public class PairTipFragment extends DialogFragment {

    private View mRootView;
    private ImageView deviceIv;

    private String imgUrl;

    public PairTipFragment(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null){
            mRootView = inflater.inflate(R.layout.user_fragment_pair_tip, container, false);
        }
        ((TextView)mRootView.findViewById(R.id.titleBigTv)).setText(getString(R.string.user_wear_tip));
        deviceIv = mRootView.findViewById(R.id.deviceIv);

        Glide.with(getActivity()).load(imgUrl).into(deviceIv);

//        if (screenType == 1){
//            deviceIv.setImageResource(R.mipmap.adddevice_wear_06);
//        }

        mRootView.findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mRootView.findViewById(R.id.finishTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        return mRootView;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if(window != null) {
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawableResource(R.color.white);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setWindowAnimations(R.style.DefaultCityPickerAnimation);
        }
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                }
                return false;
            }
        });

        return dialog;
    }

}
