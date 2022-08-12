package com.szip.healthy.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.List;

public class ReportListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ReportInfoData> childList = new ArrayList<>();
    private List<ReportInfoData> groupList = new ArrayList<>();

    public ReportListAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        final RecyclerView.ViewHolder holder;
        if (viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_sport_time_list, null);
            holder = new GronpHolder(view);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_report_list, null);
            holder = new Holder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportInfoData reportInfoData = childList.get(position);
        if (holder.getItemViewType()==0){
            ((GronpHolder)holder).timeTv.setText(DateUtil.getStringDateFromSecond(reportInfoData.getTime(),"yyyy/MM"));
        }else {
            ((Holder)holder).timeTv.setText(DateUtil.getStringDateFromSecond(reportInfoData.getTime(),"MM/dd HH:mm"));
            ((Holder)holder).dataTv.setText(reportInfoData.getData());
            if (position == childList.size()-1)
                ((Holder)holder).bottomRl.setVisibility(View.VISIBLE);
            else
                ((Holder)holder).bottomRl.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemViewType(int position) {
        ReportInfoData reportInfoData = childList.get(position);
        if (groupList.contains(reportInfoData))
            return 0;
        else
            return 1;
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    class Holder  extends RecyclerView.ViewHolder{

        private TextView timeTv,dataTv;
        private RelativeLayout bottomRl;

        public Holder(@NonNull View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.timeTv);
            dataTv = itemView.findViewById(R.id.dataTv);
            bottomRl = itemView.findViewById(R.id.bottomRl);
        }
    }

    class GronpHolder  extends RecyclerView.ViewHolder{

        private TextView timeTv;

        public GronpHolder(@NonNull View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

    public void setList(List<ReportInfoData> childList, List<ReportInfoData> groupList) {
        this.childList = childList;
        this.groupList = groupList;
        notifyDataSetChanged();
    }

}
