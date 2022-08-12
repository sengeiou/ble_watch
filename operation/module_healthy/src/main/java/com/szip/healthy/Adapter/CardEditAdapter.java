package com.szip.healthy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;
import com.szip.healthy.R;

import java.util.List;

public class CardEditAdapter extends ArrayAdapter<HealthyCardData> {

    private List<HealthyCardData> list;
    private List<HealthyCardData> tagList;

    public CardEditAdapter(Context context, List<HealthyCardData> list,List<HealthyCardData> tagList) {
        super(context, 0, list);
        this.list = list;
        this.tagList = tagList;
    }

    public List<HealthyCardData> getList(){
        return list;
    }

    @Nullable
    @Override
    public HealthyCardData getItem(int position) {
        return list.get(position);
    }

    @Override
    public boolean isEnabled(int position) {
        if(tagList.contains(getItem(position))){
            //如果是分组标签，返回false，不能选中，不能点击
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(tagList.contains(getItem(position))){
            //如果是分组标签，就加载分组标签的布局文件，两个布局文件显示效果不同
            view = LayoutInflater.from(getContext()).inflate(R.layout.healthy_card_adapter_tag, null);
            TextView textView = view.findViewById(R.id.tagTv);
            if (list.get(position).state){
                textView.setText(getContext().getString(R.string.healthy_card_visiable));
            }else {
                textView.setText(getContext().getString(R.string.healthy_card_gone));
            }
        }else{
            //如果是正常数据项标签，就加在正常数据项的布局文件
            view = LayoutInflater.from(getContext()).inflate(R.layout.healthy_adapter_card_edit, null);
            TextView cardTv = view.findViewById(R.id.cardTv);
            ImageView cardIv = view.findViewById(R.id.cardIv);
            HealthyCardData healthyCardData = list.get(position);
            if (healthyCardData.type == HealthyConst.HEART){
                cardIv.setImageResource(R.mipmap.state_hr);
                cardTv.setText(getContext().getString(R.string.healthy_heart));
            }else if (healthyCardData.type == HealthyConst.BLOOD_OXYGEN){
                cardIv.setImageResource(R.mipmap.state_spo2);
                cardTv.setText(getContext().getString(R.string.healthy_blood_oxygen));
            }else if (healthyCardData.type == HealthyConst.BLOOD_PRESSURE){
                cardIv.setImageResource(R.mipmap.state_bp);
                cardTv.setText(getContext().getString(R.string.healthy_blood_pressure));
            }else if (healthyCardData.type == HealthyConst.TEMPERATURE){
                cardIv.setImageResource(R.mipmap.state_temperature);
                cardTv.setText(getContext().getString(R.string.healthy_temp));
            }else if (healthyCardData.type == HealthyConst.SLEEP){
                cardIv.setImageResource(R.mipmap.state_sleep);
                cardTv.setText(getContext().getString(R.string.healthy_sleep));
            }else if (healthyCardData.type == HealthyConst.STEP){
                cardIv.setImageResource(R.mipmap.state_steps_32);
                cardTv.setText(getContext().getString(R.string.healthy_step));
            }
        }
        return view;
    }
}
