package com.szip.healthy.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.healthy.Model.DateBean;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.Locale;

import static com.szip.blewatch.base.Const.CalendarConst.TYPE_DATE_BLANK;
import static com.szip.blewatch.base.Const.CalendarConst.TYPE_DATE_NORMAL;
import static com.szip.blewatch.base.Const.CalendarConst.TYPE_DATE_TITLE;

public class DateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DateBean> dateBeans = new ArrayList<>();
    private CalendarListener calendarListener;


    public void setDateBeans(ArrayList<DateBean> dateBeans) {
        if (this.dateBeans.size()==0)
            this.dateBeans = dateBeans;
        else
            this.dateBeans.addAll(0,dateBeans);
        notifyDataSetChanged();
    }

    public void setCalendarListener(CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        final RecyclerView.ViewHolder holder;
        if (viewType == TYPE_DATE_BLANK){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_item_calendar_blank, null);
            holder= new MonthHolder(view);
        } else if (viewType == TYPE_DATE_TITLE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_item_calendar_title, null);
            holder= new MonthHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.healthy_item_calendar_date, null);
            holder= new DayHolder(view);
        }


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DATE_NORMAL){
            DayHolder dayHolder = (DayHolder) holder;

            dayHolder.dayTv.setText(String.format(Locale.ENGLISH,"%d",dateBeans.get(position).getDay()));
            if (dateBeans.get(position).isChooseDay())
                dayHolder.dayTv.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.healthy_calendar_choose));
            else if (dateBeans.get(position).isToday())
                dayHolder.dayTv.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.healthy_calendar_today));
            else
                dayHolder.dayTv.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),R.color.bgColor));
            //对加载的子项注册监听事件
            dayHolder.fruitView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (calendarListener!=null)
                        calendarListener.onClickDate(dateBeans.get(holder.getAdapterPosition()).getDate());
                }
            });
        }else if (getItemViewType(position) == TYPE_DATE_TITLE){
            MonthHolder monthHolder = (MonthHolder) holder;
            monthHolder.monthTv.setText(dateBeans.get(position).getGroupName());
        }
    }


    @Override
    public int getItemCount() {
        return dateBeans.size();
    }

    public class DayHolder extends RecyclerView.ViewHolder {
        private View fruitView;
        private TextView dayTv;
        public DayHolder(@NonNull View itemView) {
            super(itemView);
            fruitView = itemView;
            dayTv = itemView.findViewById(R.id.dayTv);
        }
    }

    public class MonthHolder extends RecyclerView.ViewHolder {
        private TextView monthTv;
        public MonthHolder(@NonNull View itemView) {
            super(itemView);
            monthTv = itemView.findViewById(R.id.monthTv);
        }
    }

    public interface CalendarListener{
        void onClickDate(String date);
    }
}
