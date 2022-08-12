package com.szip.healthy.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.Model.SportTypeModel;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Locale;

public class SportListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;

    private ArrayList<SportData> groupList = new ArrayList<>();
    private ArrayList<SportData> childList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public SportListAdapter(Context context) {
        mContext = context;
    }

    public void setDataList(ArrayList<SportData> groupList,ArrayList<SportData> childList) {
        this.groupList = groupList;
        this.childList = childList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        final RecyclerView.ViewHolder holder;
        if (viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_sport_time_list, null);
            holder = new GroupHolder(view);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_sport_list, null);
            holder = new ViewHolder(view);
            ((ViewHolder)holder).view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener!=null)
                        onItemClickListener.onItemClick(holder.getAdapterPosition());
                }
            });
        }
        return holder;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SportData sportData = childList.get(position);
        if (holder.getItemViewType()==0){
            ((GroupHolder)holder).timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"yyyy/MM"));
        }else {
            SportTypeModel sportTypeModel = MathUtil.newInstance().getSportType(sportData.type,mContext);

            ((ViewHolder)holder).timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"yyyy/MM/dd"));
            ((ViewHolder)holder).dataTv.setText(String.format(Locale.ENGLISH,"%.1f kcal,%d min",((sportData.calorie+55)/100)/10f,sportData.sportTime/60));
            if (sportTypeModel==null){
                ((ViewHolder)holder).typeTv.setText(mContext.getString(R.string.outrun));
                ((ViewHolder)holder).typeIv.setImageResource(R.mipmap.sport_outrun);
            }else {
                ((ViewHolder)holder).typeTv.setText(sportTypeModel.getSportStr());
                ((ViewHolder)holder).typeIv.setImageResource(sportTypeModel.getType());
            }

            if (position==childList.size()-1)
                ((ViewHolder)holder).bottomRl.setVisibility(View.VISIBLE);
            else
                ((ViewHolder)holder).bottomRl.setVisibility(View.GONE);

            if (TextUtils.isEmpty(sportData.latArray))
                ((ViewHolder)holder).gpsIv.setVisibility(View.GONE);
            else
                ((ViewHolder)holder).gpsIv.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    @Override
    public int getItemViewType(int position) {
        SportData sportData = childList.get(position);
        if (groupList.contains(sportData))
            return 0;
        else
            return 1;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        ImageView typeIv,gpsIv;
        TextView timeTv,typeTv,dataTv;
        RelativeLayout bottomRl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            timeTv = itemView.findViewById(R.id.timeTv);
            typeTv = itemView.findViewById(R.id.typeTv);
            dataTv = itemView.findViewById(R.id.dataTv);
            typeIv = itemView.findViewById(R.id.typeIv);
            gpsIv = itemView.findViewById(R.id.gpsIv);
            bottomRl = itemView.findViewById(R.id.bottomRl);
        }
    }

    private static class GroupHolder extends RecyclerView.ViewHolder{
        TextView timeTv;

        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

}
