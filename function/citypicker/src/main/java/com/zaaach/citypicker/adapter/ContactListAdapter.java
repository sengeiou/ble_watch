package com.zaaach.citypicker.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Model.ContactModel;
import com.zaaach.citypicker.R;

import java.util.List;

public class ContactListAdapter  extends RecyclerView.Adapter<ContactListAdapter.BaseViewHolder> {
    private Context mContext;
    private List<ContactModel> mData;
    private int locateState;
    private InnerContactListener mInnerListener;
    private LinearLayoutManager mLayoutManager;
    private boolean stateChanged;
    private int chooseNum = 0;

    public ContactListAdapter(Context context, List<ContactModel> data) {
        this.mData = data;
        this.mContext = context;

    }

    public void setLayoutManager(LinearLayoutManager manager){
        this.mLayoutManager = manager;
    }


    public void setChooseNum(int chooseNum) {
        this.chooseNum = chooseNum;
    }

    public void updateData(List<ContactModel> data){
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * 滚动RecyclerView到索引位置
     * @param index
     */
    public void scrollToSection(String index){
        if (mData == null || mData.isEmpty()) return;
        if (TextUtils.isEmpty(index)) return;
        int size = mData.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.equals(index.substring(0, 1), mData.get(i).getSection().substring(0, 1))){
                if (mLayoutManager != null){
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                    if (TextUtils.equals(index.substring(0, 1), "定")) {
                        //防止滚动时进行刷新
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (stateChanged) notifyItemChanged(0);
                            }
                        }, 1000);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cp_list_item_contact_layout, parent, false);
        return new DefaultViewHolder(view);

    }
    private boolean isCheck;
    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        if (holder == null) return;
        if (holder instanceof DefaultViewHolder){
            final int pos = holder.getAdapterPosition();
            final ContactModel data = mData.get(pos);
            if (data == null) return;
            ((DefaultViewHolder)holder).name.setText(data.getName());
            ((DefaultViewHolder)holder).mobile.setText(data.getMobile());
            ((DefaultViewHolder)holder).stateCb.setChecked(data.isCheck());
            isCheck  = ((DefaultViewHolder)holder).stateCb.isChecked();
            ((DefaultViewHolder) holder).relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isCheck&&chooseNum>=10)
                        return;
                    if (mInnerListener != null){
                        isCheck = !isCheck;
                        mInnerListener.dismiss(pos, data,isCheck);
                        data.setCheck(isCheck);
                        ((DefaultViewHolder)holder).stateCb.setChecked(isCheck);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setInnerListener(InnerContactListener listener){
        this.mInnerListener = listener;
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder{
        BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class DefaultViewHolder extends BaseViewHolder {
        TextView name;
        TextView mobile;
        CheckBox stateCb;
        RelativeLayout relativeLayout;
        DefaultViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTv);
            mobile = itemView.findViewById(R.id.mobileTv);
            stateCb = itemView.findViewById(R.id.stateCb);
            relativeLayout = itemView.findViewById(R.id.cp_list_rl);
        }
    }
}
