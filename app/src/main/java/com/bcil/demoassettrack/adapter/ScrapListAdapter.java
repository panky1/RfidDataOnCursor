package com.bcil.demoassettrack.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.utils.AppConstants;

import java.util.List;

public class ScrapListAdapter extends RecyclerView.Adapter<ScrapListAdapter.MyViewHolder> {
    private List<AssetInfo> assetInfoList;
    private Context context;
    private ScrapListAdapter.OnItemClickListener onItemClickListener;
    private View itemView;
    private AssetInfo assetInfo;

    public ScrapListAdapter(Context context, List<AssetInfo> assetInfoList) {
        this.context = context;
        this.assetInfoList=assetInfoList;
    }

    @NonNull
    @Override
    public ScrapListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scrap_list_adapter, parent, false);
        return new ScrapListAdapter.MyViewHolder(itemView);
    }
    public void setOnItemClickListener(ScrapListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public void onBindViewHolder(@NonNull ScrapListAdapter.MyViewHolder holder, int position) {
		final int pos = position;
        assetInfo = assetInfoList.get(position);
        if(assetInfo!=null){
            if(assetInfo.getAssetid()!=null){
                holder.tvAssetId.setText(assetInfo.getAssetid());
            }else {
                holder.tvAssetId.setText(AppConstants.EMPTY);
            }

            if(assetInfo.getAssetdesc()!=null){
                holder.tvAssetDesc.setText(assetInfo.getAssetdesc());
            }else {
                holder.tvAssetDesc.setText(AppConstants.EMPTY);
            }

            if(assetInfo.getLocation()!=null){
                holder.tvLoc.setText(assetInfo.getLocation());
            }else {
                holder.tvLoc.setText(AppConstants.EMPTY);
            }

            if(assetInfo.getRfid()!=null){
                holder.tvRfid.setText(assetInfo.getRfid());
            }else {
                holder.tvRfid.setText(AppConstants.EMPTY);
            }
        }
		holder.cbCheck.setChecked(assetInfoList.get(position).isSelected());
        holder.cbCheck.setTag(assetInfoList.get(position));
        holder.cbCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                AssetInfo assetInfo1 = (AssetInfo) cb.getTag();

                assetInfo1.setSelected(cb.isChecked());
                assetInfoList.get(pos).setSelected(cb.isChecked());


            }
        });


        holder.itemView.setTag(assetInfo);
    }

    @Override
    public int getItemCount() {
        if(assetInfoList!=null&&assetInfoList.size()>0){
            return assetInfoList.size();
        }else {
            return 0;
        }
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvRfid,tvAssetId,tvLoc,tvAssetDesc;
        CheckBox cbCheck;

        MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvAssetId = (TextView) itemView.findViewById(R.id.tvAssetId);
            tvLoc = (TextView) itemView.findViewById(R.id.tvLoc);
            tvRfid = (TextView) itemView.findViewById(R.id.tvRfid);
            tvAssetDesc = (TextView) itemView.findViewById(R.id.tvAssetDesc);
            cbCheck = (CheckBox) itemView.findViewById(R.id.cbCheck);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, (AssetInfo) v.getTag());
            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, AssetInfo position);
    }

    public List<AssetInfo> getAssetInfoList() {
        return assetInfoList;
    }

}
