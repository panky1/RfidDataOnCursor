package com.bcil.demoassettrack.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.model.AssetInfo;

import java.util.ArrayList;
import java.util.List;

public class CustomAssetIdAdapter extends ArrayAdapter<AssetInfo> {
  private Context mContext;
  private List<AssetInfo> assetInfoList;
  private List<AssetInfo> assetInfoListAll;
  private int mLayoutResourceId;
  private String mode;

  public CustomAssetIdAdapter(Context mContext, int resource, List<AssetInfo> assetInfoList, String mode) {
    super(mContext, resource, assetInfoList);
    this.mContext = mContext;
    this.mLayoutResourceId = resource;
    this.assetInfoList = new ArrayList<>(assetInfoList);
    this.assetInfoListAll = new ArrayList<>(assetInfoList);
    this.mode = mode;
  }



  @Override
  public int getCount() {
    return assetInfoList.size();
  }

  @Override
  public AssetInfo getItem(int i) {
    return assetInfoList.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
    View view = convertView;
    try {
      if (convertView == null) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        view = inflater.inflate(mLayoutResourceId, viewGroup, false);
      }
      AssetInfo salesDto = getItem(position);
      TextView tvAssetId = (TextView) view.findViewById(R.id.tvAssetId);
      if(salesDto!=null){
        if(mode.equals("ASSETID")){
          tvAssetId.setText(salesDto.getAssetid());
        }else{
          tvAssetId.setText(salesDto.getAssetdesc());
        }
      }else {
        Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
      }



    }catch (Exception e){
      e.printStackTrace();
    }

    return view;
  }


  @NonNull
  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        List<AssetInfo> salesDtoList = new ArrayList<>();
        if (charSequence != null) {
          for (AssetInfo salesDto : assetInfoListAll) {
            if(mode.equals("ASSETID")){
              if (salesDto.getAssetid().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                salesDtoList.add(salesDto);
              }
            }else {
              if (salesDto.getAssetdesc().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                salesDtoList.add(salesDto);
              }
            }

          }
          filterResults.values = salesDtoList;
          filterResults.count = salesDtoList.size();
        }
        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        assetInfoList.clear();
        if (filterResults != null && filterResults.count > 0) {
          // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
          for (Object object : (List<?>) filterResults.values) {
            if (object instanceof AssetInfo) {
              assetInfoList.add((AssetInfo) object);
            }
          }
          notifyDataSetChanged();
        } else if (charSequence == null) {
          // no filter, add entire original list back in
          assetInfoList.addAll(assetInfoListAll);
          notifyDataSetInvalidated();
        }
      }

      @Override
      public String convertResultToString(Object resultValue) {
        if(mode.equals("ASSETID")){
          return ((AssetInfo) resultValue).getAssetid();
        }else {
          return ((AssetInfo) resultValue).getAssetdesc();
        }

      }
    };
  }


}
