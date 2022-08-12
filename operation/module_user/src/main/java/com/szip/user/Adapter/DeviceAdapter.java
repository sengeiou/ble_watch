package com.szip.user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    private Context mContext;

    private List<String> mDataList = new ArrayList<>();
    private String pictureUrl;

    public DeviceAdapter(Context context,String pictureUrl) {
        mContext = context;
        this.pictureUrl = pictureUrl;
    }

    public void setDataList(List<String> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView name;
        ImageView watchIv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.user_adapter_device_list, null, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.nameTv);
            holder.watchIv = convertView.findViewById(R.id.watchIv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String result = (String) getItem(position);

        holder.name.setText(result);
        Glide.with(mContext).load(pictureUrl)
                .error(R.mipmap.adddevice_circle)
                .into(holder.watchIv);

        return convertView;
    }
}
