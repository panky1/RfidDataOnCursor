package com.bcil.demoassettrack.utils;

import com.bcil.demoassettrack.model.AssetInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ashish Maurya
 * @company BCIL
 * @project Viacom18AssetTrack
 * @date 18/01/2019
 * @since 0.1
 */
public class JsonController {

    public static List<AssetInfo> jsonToOneDimenStrAstIdAndDescDtls(String json) throws JSONException {
        JSONArray jsonarray = new JSONArray(json);
        List<AssetInfo> assetInfoList = new ArrayList<>();
        assetInfoList.clear();
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            AssetInfo assetInfo = new AssetInfo();
            assetInfo.setAssetid(jsonobject.getString("ASSETID"));
            assetInfo.setAssetdesc(jsonobject.getString("ASSET_DESC"));
            assetInfoList.add(assetInfo);
        }
        return assetInfoList;
    }

    public static List<AssetInfo> jsonToOneDimenStrValidateRfid(String json, List<AssetInfo> assetInfoList) throws JSONException {
        JSONArray jsonarray = new JSONArray(json);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            AssetInfo assetInfo = new AssetInfo();
            assetInfo.setAssetid(jsonobject.getString("ASSETID"));
            assetInfo.setLocation(jsonobject.getString("PUTWAY_LOC"));
            assetInfo.setRfid(jsonobject.getString("RFID"));
            assetInfo.setSelected(false);
            assetInfoList.add(assetInfo);
        }
        return assetInfoList;
    }

    public static List<AssetInfo> jsonToOneDimenStrScrapAndSold(String json, List<AssetInfo> assetInfoList) throws JSONException {
        JSONArray jsonarray = new JSONArray(json);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            AssetInfo assetInfo = new AssetInfo();
            assetInfo.setAssetid(jsonobject.getString("ASSETID"));
            assetInfo.setAssetdesc(jsonobject.getString("ASSET_DESC"));
            assetInfo.setLocation(jsonobject.getString("PUTWAY_LOC"));
            assetInfo.setRfid(jsonobject.getString("RFID"));
            assetInfo.setSelected(false);
            assetInfoList.add(assetInfo);
        }
        return assetInfoList;
    }

    public static String FetchMsg(String json) throws JSONException {
        JSONArray jsonarray = new JSONArray(json);
        JSONObject jsonobject = jsonarray.getJSONObject(0);
        return jsonobject.getString("MSG");
    }

}
