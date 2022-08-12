package com.szip.user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.HttpModel.DialBean;
import com.szip.user.R;


import java.util.ArrayList;

public class DIYAdapter extends RecyclerView.Adapter<DIYAdapter.Holder>{

    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private Context context;
    private int select = -1;
    private int screenType = 0;

    public DIYAdapter(ArrayList<DialBean.Dial> dials, Context context) {
        this.context = context;
        this.dialArrayList = dials;
        SportWatchAppFunctionConfigDTO data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(context));
        if (data!=null)
            screenType = data.screenType;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (screenType == 0)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_adaper_diy, null);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_adaper_diy_06, null);
        final Holder holder = new Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select = holder.getAdapterPosition();
                onItemClickListener.onItemClick(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
        return holder;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position==select){
            holder.selectView.setVisibility(View.VISIBLE);
        }else {
            holder.selectView.setVisibility(View.GONE);
        }
        Glide.with(context).load(dialArrayList.get(position).getPointerImg())
                .placeholder(R.mipmap.dial_default)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dialArrayList.size();
    }


    class Holder extends RecyclerView.ViewHolder {
        private CircularImageView imageView;
        private View selectView;
        private View fruitView;  //表示我们自定义的控件的视图

        public Holder(View itemView) {
            super(itemView);
            fruitView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            selectView = itemView.findViewById(R.id.selectView);
        }
    }


}
