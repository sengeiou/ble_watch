package com.szip.user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter  extends RecyclerView.Adapter<ProductAdapter.Holder> {
    private Context mContext;

    private List<SportWatchAppFunctionConfigDTO> mDataList = new ArrayList<>();

    public ProductAdapter(Context context) {
        mContext = context;
    }

    public void setDataList(List<SportWatchAppFunctionConfigDTO> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_adapter_device_list, null);
        final Holder holder = new Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final SportWatchAppFunctionConfigDTO result = mDataList.get(position);
        holder.watchTv.setText(result.appName);
        Glide.with(mContext).load(result.productImg)
                .error(R.mipmap.adddevice_circle)
                .into(holder.watchIv);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private TextView watchTv;
        private ImageView watchIv;
        private View fruitView;  //表示我们自定义的控件的视图

        public Holder(View itemView) {
            super(itemView);
            fruitView = itemView;
            watchTv = itemView.findViewById(R.id.nameTv);
            watchIv = itemView.findViewById(R.id.watchIv);
        }
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
