package com.zaaach.citypicker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Model.ContactModel;
import com.szip.blewatch.base.Util.ContactUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.zaaach.citypicker.adapter.CityListAdapter;
import com.zaaach.citypicker.adapter.ContactListAdapter;
import com.zaaach.citypicker.adapter.InnerContactListener;
import com.zaaach.citypicker.adapter.InnerListener;
import com.zaaach.citypicker.adapter.OnContactPickListener;
import com.zaaach.citypicker.adapter.decoration.DividerItemDecoration;
import com.zaaach.citypicker.adapter.decoration.SectionContactItemDecoration;
import com.zaaach.citypicker.adapter.decoration.SectionItemDecoration;
import com.zaaach.citypicker.db.DBManager;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.view.SideIndexBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactPickerDialogFragment extends AppCompatDialogFragment implements TextWatcher,
        View.OnClickListener, SideIndexBar.OnIndexTouchedChangedListener, InnerContactListener {

    private OnContactPickListener onContactPickListener;
    private int mAnimStyle = R.style.DefaultCityPickerAnimation;

    private View mContentView;
    private RecyclerView mRecyclerView;
    private TextView mOverlayTextView,chooseTv,confirmTv;
    private SideIndexBar mIndexBar;
    private EditText mSearchBox;
    private ImageView mClearAllBtn;

    private View mEmptyView;
    private LinearLayoutManager mLayoutManager;
    private ContactListAdapter mAdapter;

    private static final String TAG_FAKE_STATUS_BAR_VIEW = "statusBarView";
    private static final String TAG_MARGIN_ADDED = "marginAdded";

    private boolean enableAnim = false;

    private ArrayList<ContactModel> mAllContacts;
    private ArrayList<ContactModel> filerContacts;
    private ArrayList<ContactModel> mResults;
    /**
     * 获取实例
     * @param enable 是否启用动画效果
     * @return
     */
    public static ContactPickerDialogFragment newInstance(boolean enable){
        final ContactPickerDialogFragment fragment = new ContactPickerDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("cp_enable_anim", enable);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("ResourceType")
    public void setAnimationStyle(@StyleRes int style){
        this.mAnimStyle = style <= 0 ? R.style.DefaultCityPickerAnimation : style;
    }

    public void setOnContactPickListener(OnContactPickListener onContactPickListener) {
        this.onContactPickListener = onContactPickListener;
    }


    public void setFilerContacts(ArrayList<ContactModel> filerContacts) {
        this.filerContacts = filerContacts;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.CityPickerStyle);

        Bundle args = getArguments();
        if (args != null) {
            enableAnim = args.getBoolean("cp_enable_anim");
        }

        mAllContacts = ContactUtil.queryContacts(getContext(),null,null);
        if (filerContacts!=null&&filerContacts.size()>0)
            mAllContacts = ContactUtil.filterContact(mAllContacts,filerContacts);
//        Collections.sort(mAllContacts);
        mResults = mAllContacts;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.cp_dialog_contact_picker, container, false);
        mRecyclerView = mContentView.findViewById(R.id.cp_city_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionContactItemDecoration(getActivity(), mAllContacts), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()), 1);
        mAdapter = new ContactListAdapter(getActivity(), mAllContacts);
        mAdapter.setInnerListener(this);
        mAdapter.setChooseNum(filerContacts.size());
        mAdapter.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        confirmTv = mContentView.findViewById(R.id.confirmTv);
        confirmTv.setOnClickListener(this);
        chooseTv = mContentView.findViewById(R.id.chooseTv);
        chooseTv.setText(String.format("已选择(%d/10)",filerContacts.size()));
        mEmptyView = mContentView.findViewById(R.id.cp_empty_view);
        mOverlayTextView = mContentView.findViewById(R.id.cp_overlay);

        mIndexBar = mContentView.findViewById(R.id.cp_side_index_bar);
        mIndexBar.setOverlayTextView(mOverlayTextView)
                .setOnIndexChangedListener(this);

        mSearchBox = mContentView.findViewById(R.id.cp_search_box);
        mSearchBox.addTextChangedListener(this);

        mContentView.findViewById(R.id.backIv).setOnClickListener(this);
        mClearAllBtn = mContentView.findViewById(R.id.cp_clear_all);
        mClearAllBtn.setOnClickListener(this);

        return mContentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if(window != null) {
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            if (enableAnim) {
                window.setWindowAnimations(mAnimStyle);
            }
        }

        translucentStatusBar(dialog,true);
        setAndroidNativeLightStatusBar(dialog,true);
        return dialog;
    }

    public void translucentStatusBar(@NonNull Dialog activity, boolean hideStatusBarBackground) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (hideStatusBarBackground) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
                ViewCompat.requestApplyInsets(mChildView);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mContentChild = mContentView.getChildAt(0);

            removeFakeStatusBarViewIfExist(activity);
            removeMarginTopOfContentChild(mContentChild, getStatusBarHeight(activity.getContext()));
            if (mContentChild != null) {
                ViewCompat.setFitsSystemWindows(mContentChild, false);
            }
        }
    }

    private void setAndroidNativeLightStatusBar(@NonNull Dialog activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * remove marginTop to simulate set FitsSystemWindow false
     */
    private void removeMarginTopOfContentChild(View mContentChild, int statusBarHeight) {
        if (mContentChild == null) {
            return;
        }
        if (TAG_MARGIN_ADDED.equals(mContentChild.getTag())) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentChild.getLayoutParams();
            lp.topMargin -= statusBarHeight;
            mContentChild.setLayoutParams(lp);
            mContentChild.setTag(null);
        }
    }

    private void removeFakeStatusBarViewIfExist(Dialog activity) {
        Window window = activity.getWindow();
        ViewGroup mDecorView = (ViewGroup) window.getDecorView();

        View fakeView = mDecorView.findViewWithTag(TAG_FAKE_STATUS_BAR_VIEW);
        if (fakeView != null) {
            mDecorView.removeView(fakeView);
        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }
        return result;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString();
        if (TextUtils.isEmpty(keyword)){
            mClearAllBtn.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            mResults = mAllContacts;
            ((SectionContactItemDecoration)(mRecyclerView.getItemDecorationAt(0))).setData(mResults);
            mAdapter.updateData(mResults);
        }else {
            mClearAllBtn.setVisibility(View.VISIBLE);
            //开始数据库查找
            if (MathUtil.newInstance().isNumeric(keyword)){
                mResults = ContactUtil.searchContactByNumber(getContext(),keyword);
            }else {
                mResults = ContactUtil.searchContactByName(getContext(),keyword);
            }

            ((SectionContactItemDecoration)(mRecyclerView.getItemDecorationAt(0))).setData(mResults);
            if (mResults == null || mResults.isEmpty()){
                mEmptyView.setVisibility(View.VISIBLE);
            }else {
                mEmptyView.setVisibility(View.GONE);
                mAdapter.updateData(mResults);
            }
        }
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backIv) {
            dismiss();
        }else if(id == R.id.cp_clear_all){
            mSearchBox.setText("");
        }else if (id == R.id.confirmTv){
            if (onContactPickListener!=null){
                dismiss();
                onContactPickListener.onPick(filerContacts);
            }
        }
    }

    @Override
    public void onIndexChanged(String index, int position) {
        mAdapter.scrollToSection(index);
    }

    @Override
    public void dismiss(int position, ContactModel data, boolean check) {
        if (check){
            filerContacts.add(data);
        }else {
            for (ContactModel contactModel:filerContacts){
                if(contactModel.getMobile().equalsIgnoreCase(data.getMobile())&&
                        contactModel.getName().equalsIgnoreCase(data.getName())){
                    data = contactModel;
                    break;
                }
            }
            filerContacts.remove(data);
        }
        mAdapter.setChooseNum(filerContacts.size());
        chooseTv.setText(String.format("已选择(%d/10)",filerContacts.size()));
    }
}
