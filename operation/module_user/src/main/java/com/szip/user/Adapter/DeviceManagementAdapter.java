package com.szip.user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceManagementAdapter extends RecyclerView.Adapter<DeviceManagementAdapter.Holder>{

    private ArrayList<Integer> imageList;
    private ArrayList<Integer> nameList;
    private Context context;

    public DeviceManagementAdapter(Context context) {
        this.context = context;
    }

    public void update(){
        nameList = new ArrayList<>(Arrays.asList(R.string.user_find_watch,R.string.user_ble_call, R.string.user_ble_camera,R.string.user_unit
                ,R.string.user_notification,R.string.user_about
                ,R.string.user_schedule));
        imageList = new ArrayList<>(Arrays.asList(R.mipmap.my_device_findwatch,R.mipmap.my_device_btphone,R.mipmap.my_device_btcamera,
                R.mipmap.my_device_unit,R.mipmap.my_device_message,R.mipmap.my_device_about,
                R.mipmap.my_device_schedule));
        if (LoadDataUtil.newInstance().showAutoMeasure(MathUtil.newInstance().getUserId(context))){
            nameList.add(R.string.user_auto);
            imageList.add(R.mipmap.my_device_autodetect);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_adapter_menu, null);
        final Holder holder = new Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(view1 -> onItemClickListener.onItemClick(nameList.get(holder.getAdapterPosition())));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.menuTv.setText(context.getString(nameList.get(position)));
        holder.menuIv.setImageResource(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        return nameList == null ? 0 : nameList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private TextView menuTv;
        private ImageView menuIv;
        private View fruitView;  //表示我们自定义的控件的视图

        public Holder(View itemView) {
            super(itemView);
            fruitView = itemView;
            menuTv = itemView.findViewById(R.id.menuTv);
            menuIv = itemView.findViewById(R.id.menuIv);
        }
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

}
