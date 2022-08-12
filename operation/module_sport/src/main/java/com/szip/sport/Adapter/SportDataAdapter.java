package com.szip.sport.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Model.SportTypeModel;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.sport.Model.SportResultDataModel;
import com.szip.sport.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SportDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<SportResultDataModel> sportResultDataModels = new ArrayList<>();

    public SportDataAdapter(List<SportResultDataModel> sportResultDataModels) {
        this.sportResultDataModels = sportResultDataModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sport_adapter_sport_result_data, null);
        final SportDataHolder holder = new SportDataHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SportResultDataModel sportResultDataModel = sportResultDataModels.get(position);
        SportDataHolder sportHolder = (SportDataHolder) holder;
        sportHolder.dataTv.setText(sportResultDataModel.getData());
        sportHolder.unitTv.setText(sportResultDataModel.getUnit());
    }

    @Override
    public int getItemCount() {
        return sportResultDataModels.size();
    }

    class SportDataHolder extends RecyclerView.ViewHolder {

        private View fruitView;  //表示我们自定义的控件的视图
        private TextView dataTv,unitTv;
        public SportDataHolder(View itemView) {
            super(itemView);
            fruitView = itemView;
            dataTv = itemView.findViewById(R.id.dataTv);
            unitTv = itemView.findViewById(R.id.unitTv);
        }
    }
}
