package com.bcil.demoassettrack.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class RfidListAdapter extends RecyclerView.Adapter<RfidListAdapter.MyViewHolder> {
    public ArrayList<AssetInfo> assetInfoList = new ArrayList<>();
    private Context context;
    private RfidListAdapter.OnItemClickListener onItemClickListener;
    private View itemView;
    private List<String> list = new ArrayList<>();

    //    private ArrayList<InventoryListItem> searchItemsList = new ArrayList<>();
    public RfidListAdapter(Context context, ArrayList<AssetInfo> assetInfoList) {
        this.context = context;
        this.assetInfoList=assetInfoList;

    }

    public RfidListAdapter(Context context){
        this.context = context;
        assetInfoList.addAll(MyApp.tagsReadInventory1);
    }

    @NonNull
    @Override
    public RfidListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rfid_list_adapter, parent, false);
        return new RfidListAdapter.MyViewHolder(itemView);
    }
    public void setOnItemClickListener(RfidListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public void onBindViewHolder(@NonNull RfidListAdapter.MyViewHolder holder, int position) {
        AssetInfo assetInfo = assetInfoList.get(position);
        if(assetInfo!=null){
            if(assetInfo.getRfid()!=null){
                holder.tvRfid.setText(assetInfo.getRfid());
            }else {
                holder.tvRfid.setText(AppConstants.EMPTY);
            }
        }


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

    public void add(String rfid) {
        if (assetInfoList != null) {
            assetInfoList.add(new AssetInfo(rfid));
            /*list.add(rfid);
            Set<String> set = new HashSet<String>(list);
            list.clear();
            list.addAll(set);
            for(String str:list){
                Log.d(RfidListAdapter.class.getSimpleName(), "checklist"+str);
            }*/
        }

    }


    public void clear() {
        if (assetInfoList != null)
            assetInfoList.clear();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvRfid;

        MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvRfid = (TextView) itemView.findViewById(R.id.tvRfid);
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

    public ArrayList<AssetInfo> getAssetInfoList() {
        return assetInfoList;
    }
}





