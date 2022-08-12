package com.szip.healthy.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.List;

public class ReportInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ReportInfoData> reportInfoData = new ArrayList<>();

    public ReportInfoAdapter(List<ReportInfoData> reportInfoData) {
        this.reportInfoData = reportInfoData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_report_list, null);
        final Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportInfoData reportData = reportInfoData.get(position);
        ((Holder)holder).timeTv.setText(DateUtil.getStringDateFromSecond(reportData.getTime(),"HH:mm"));
        ((Holder)holder).dataTv.setText(reportData.getData());
    }

    @Override
    public int getItemCount() {
        return reportInfoData.size();
    }

    class Holder  extends RecyclerView.ViewHolder{

        private TextView timeTv,dataTv;

        public Holder(@NonNull View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.timeTv);
            dataTv = itemView.findViewById(R.id.dataTv);
        }
    }
}
