package com.szip.user.Activity.contact;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.DraggableModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Model.ContactModel;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.ble.jlBleInterface.FunctionManager;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.user.Activity.camera.CameraSetActivity;
import com.szip.user.Activity.dial.DialSelectActivity;
import com.szip.user.R;
import com.zaaach.citypicker.Picker;
import com.zaaach.citypicker.adapter.OnContactPickListener;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class ContactActivity  extends BaseActivity implements MyHandle {

    private ToActivityBroadcast toActivityBroadcast;
    private ImageView editIv;
    private ArrayList<ContactModel> contactModels = new ArrayList<>();
    private RecyclerView listView;
    private ContactAdapter contactAdapter;
    private TextView cancelTv,delTv,sortTv;
    private RelativeLayout bottomRl;
    private LinearLayout delLl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_contact);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        FunctionManager.getInstance().readContacts(getApplicationContext());
    }

    private void initView() {
        setTitle("常用联系人");
        editIv = findViewById(R.id.rightIv);
        editIv.setImageResource(R.mipmap.my_contact_icon_more);
        editIv.setVisibility(View.VISIBLE);
        cancelTv = findViewById(R.id.cancelTv);
        sortTv = findViewById(R.id.sortTv);
        delTv = findViewById(R.id.delTv);

        bottomRl = findViewById(R.id.bottomRl);
        delLl = findViewById(R.id.delLl);

        listView = findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contactAdapter = new ContactAdapter();
        contactAdapter.setUseEmpty(true);
        contactAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (contactAdapter.mode == ContactAdapter.MODE_DELETE) {
                contactAdapter.getItem(position).setSelect(!contactAdapter.getItem(position).isSelect());
                contactAdapter.notifyItemChanged(position);
            }
        });
        listView.setAdapter(contactAdapter);
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ContactModel> contacts = (ArrayList<ContactModel>) contactAdapter.getData();
                for (ContactModel contact : contacts) {
                    contact.setSelect(false);
                }
                contactModels = contacts;
                contactAdapter.setList(contacts);
                bottomRl.setVisibility(View.GONE);
                contactAdapter.setMode(ContactAdapter.MODE_NORMAL);
            }
        });

        sortTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactModels = (ArrayList<ContactModel>) contactAdapter.getData();
                ProgressHudModel.newInstance().show(ContactActivity.this,
                        getString(R.string.loading));
                FunctionManager.getInstance().setContacts(getApplicationContext(),contactModels);
                contactAdapter.setMode(ContactAdapter.MODE_NORMAL);
                bottomRl.setVisibility(View.GONE);
            }
        });

        delTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactAdapter.getSelectCount() < 1) {
                    showToast("至少需要选中一条数据");
                    return;
                }
                ArrayList<ContactModel> contacts = new ArrayList<>();
                for (ContactModel contact : contactAdapter.getData()) {
                    if (!contact.isSelect()) {
                        contacts.add(contact);
                    }
                }
                contactModels = contacts;
                ProgressHudModel.newInstance().show(ContactActivity.this,
                        getString(R.string.loading));
                contactAdapter.setMode(ContactAdapter.MODE_NORMAL);
                FunctionManager.getInstance().setContacts(getApplicationContext(),contacts);
                bottomRl.setVisibility(View.GONE);
            }
        });

        editIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });
    }


    private void showMenuDialog() {

        int width = MathUtil.newInstance().dipToPx(156,getApplicationContext());
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_menu_contact, null);
        PopupWindow popupWindow = new PopupWindow(view);
//        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(width);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        view.findViewById(R.id.ll_contact_menu_add).setOnClickListener(v -> {
            if (checkContactPermission()){
                Picker.getInstance()
                        .setFragmentManager(getSupportFragmentManager())
                        .enableAnimation(true)
                        .setAnimationStyle(R.style.CustomAnim)
                        .setOnContactPickListener(new OnContactPickListener() {
                            @Override
                            public void onPick(ArrayList<ContactModel> contactModelList) {
                                contactModels = contactModelList;
                                ProgressHudModel.newInstance().show(ContactActivity.this,
                                        getString(R.string.loading));
                                FunctionManager.getInstance().setContacts(getApplicationContext(),contactModelList);
                                Log.d("data******","选择的列表"+contactModelList.toString());
                            }
                        }).showContact(contactModels);
            }
            popupWindow.dismiss();
        });

        view.findViewById(R.id.ll_contact_menu_sort).setOnClickListener(v -> {
            popupWindow.dismiss();
            contactAdapter.setMode(ContactAdapter.MODE_SORT);
            bottomRl.setVisibility(View.VISIBLE);
            sortTv.setVisibility(View.VISIBLE);
            delLl.setVisibility(View.GONE);
        });

        view.findViewById(R.id.ll_contact_menu_delete).setOnClickListener(v -> {
            contactAdapter.setMode(ContactAdapter.MODE_DELETE);
            popupWindow.dismiss();
            bottomRl.setVisibility(View.VISIBLE);
            sortTv.setVisibility(View.GONE);
            delLl.setVisibility(View.VISIBLE);
        });

        int xOff = width - editIv.getWidth() - MathUtil.newInstance().dipToPx( 10,getApplicationContext());
        popupWindow.showAsDropDown(editIv, -xOff, -editIv.getWidth() / 2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (toActivityBroadcast == null)
            toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.CONTACT_UPDATE);
        toActivityBroadcast.registerReceive(this,getApplicationContext(),intentFilter);
    }

    @Override
    public void onReceive(Intent intent) {
        if (intent.getAction().equalsIgnoreCase(BroadcastConst.CONTACT_UPDATE)){
            ProgressHudModel.newInstance().diss();
            Bundle bundle = intent.getBundleExtra("bundle");
            ArrayList<ContactModel> contactModels = (ArrayList<ContactModel>) bundle.getSerializable("contact");
            this.contactModels = contactModels;
            contactAdapter.setList(contactModels);
        }
    }

    private boolean checkContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
                MyAlerDialog.getSingle().showAlerDialog(getString(R.string.permission_tip), getString(R.string.user_camera_permission_tip),
                        getString(R.string.agree), getString(R.string.disagree), false, new MyAlerDialog.AlerDialogOnclickListener() {
                            @Override
                            public void onDialogTouch(boolean flag) {
                                if (flag){
                                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,
                                    }, 100);
                                }else {
                                    showToast(getString(R.string.user_camera_permission_error));
                                }
                            }
                        }, ContactActivity.this);
                return false;
            }
            return true;
        }
        return true;
    }

    private class ContactAdapter extends BaseQuickAdapter<ContactModel, BaseViewHolder> implements DraggableModule {

        static final int MODE_NORMAL = 0;
        static final int MODE_SORT = 1;
        static final int MODE_DELETE = 2;
        private int mode;//0:普通模式  1:排序模式  2:删除模式

        public void setMode(int mode) {
            this.mode = mode;
            getDraggableModule().setDragEnabled(mode == MODE_SORT);
            notifyDataSetChanged();
        }

        public int getMode() {
            return mode;
        }

        public ContactAdapter() {
            super(R.layout.user_adapter_contact);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, ContactModel contact) {
            holder.setText(R.id.tv_contact_name, contact.getName());
            holder.setText(R.id.tv_contact_number, contact.getMobile());
//            holder.setGone(R.id.view_line_contact, getItemPosition(contact) == getData().size());
            holder.setVisible(R.id.iv_contact_end, this.mode != MODE_NORMAL);
            holder.getView(R.id.iv_contact_end).setSelected(contact.isSelect());
            holder.setImageResource(R.id.iv_contact_end, mode == MODE_SORT ? R.mipmap.ic_contact_drag_flag : R.drawable.ic_music_manager_chose_selector);
        }


        int getSelectCount() {
            int count = 0;
            for (ContactModel contact : getData()) {
                if (contact.isSelect()) count++;
            }
            return count;
        }

    }
}