package com.szip.healthy.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.healthy.R;
import com.szip.healthy.Model.HealthyData;
import com.szip.healthy.View.HealthyTableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HealthyCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<HealthyData> healthyDataList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private Context mContext;

    public HealthyCardAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setHealthyDataList(List<HealthyData> healthyDataList) {
        this.healthyDataList = healthyDataList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_healthy_card, null);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_adapter_healthy_edit, null);
        final Holder holder = new Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener!=null)
                    onItemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0&&position!=healthyDataList.size()){
            HealthyData healthyData = healthyDataList.get(position);
            ((Holder) holder).healthyTableView.setHealthyData(healthyData);
            if (healthyData.getType() == HealthyConst.HEART){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_hr);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_heart));
                if (healthyData.getHeartDataList()!=null)
                    ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%d</big> Bpm",healthyData.getData())));
                else
                    ((Holder) holder).dataTv.setText("--");
                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }else if (healthyData.getType() == HealthyConst.STEP){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_steps_32);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_step));
                ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%d</big> steps", healthyData.getData())));
                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }else if (healthyData.getType() == HealthyConst.SLEEP){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_sleep);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_sleep));
                ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%02d</big>h<big>%02d</big>min",
             (healthyData.getData()+healthyData.getData1())/60,(healthyData.getData()+healthyData.getData1())%60)));
                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }else if (healthyData.getType() == HealthyConst.BLOOD_OXYGEN){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_spo2);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_blood_oxygen));
                if (healthyData.getBloodOxygenDataList()!=null)
                    ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%d</big>%%",healthyData.getData())));
                else
                    ((Holder) holder).dataTv.setText("--");
                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }else if (healthyData.getType() == HealthyConst.BLOOD_PRESSURE){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_bp);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_blood_pressure));
                if (healthyData.getBloodPressureDataList()!=null)
                    ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%d/%d</big>mmhg",healthyData.getData(),healthyData.getData1())));
                else
                    ((Holder) holder).dataTv.setText("--");
                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }else if (healthyData.getType() == HealthyConst.TEMPERATURE){
                ((Holder) holder).typeIv.setImageResource(R.mipmap.state_temperature);
                ((Holder) holder).typeTv.setText(mContext.getString(R.string.healthy_temp));
                UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(mContext));
                if (userModel==null)
                    return;
                if (healthyData.getAnimalHeatDataList()!=null){
                    if (userModel.tempUnit==0){
                        ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%.1f</big> ℃",healthyData.getData()/10f)));
                    }else {
                        ((Holder) holder).dataTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,"<big>%.1f</big> ℉",MathUtil.newInstance().c2f(healthyData.getData()/10f))));
                    }
                }else {
                    ((Holder) holder).dataTv.setText("--");
                }


                ((Holder) holder).timeTv.setText(DateUtil.getStringDateFromSecond(healthyData.getTime(),"yyyy/MM/dd"));
            }

        }
    }

    @Override
    public int getItemCount() {
        return healthyDataList.size()%2==0?healthyDataList.size():healthyDataList.size()+1;
    }


    public void clear(){
        healthyDataList.clear();
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        private View fruitView;  //表示我们自定义的控件的视图
        private ImageView typeIv;
        private TextView typeTv,dataTv,timeTv;
        private HealthyTableView healthyTableView;
        public Holder(View itemView) {
            super(itemView);
            fruitView = itemView;
            typeIv = itemView.findViewById(R.id.typeIv);
            typeTv = itemView.findViewById(R.id.typeTv);
            dataTv = itemView.findViewById(R.id.dataTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            healthyTableView = itemView.findViewById(R.id.healthyTableView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (healthyDataList.size()%2==1&&position == healthyDataList.size())
            return 1;
        return 0;
    }
}
