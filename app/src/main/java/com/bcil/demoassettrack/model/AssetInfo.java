package com.bcil.demoassettrack.model;

import com.bcil.demoassettrack.utils.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class AssetInfo {
    private int id;
    private String assetid;
    private String assetdesc;
    public String rfid;
    private String location;
    private boolean isSelected;
    public AssetInfo() {
    }

    public AssetInfo(String assetid, String assetdesc) {
        this.assetid = assetid;
        this.assetdesc = assetdesc;
    }

    public AssetInfo(String assetid, String rfid, String location) {
        this.assetid = assetid;
        this.rfid = rfid;
        this.location = location;
    }



    public AssetInfo(String rfid){
        this.rfid = rfid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAssetid() {
        return assetid;
    }

    public void setAssetid(String assetid) {
        this.assetid = assetid;
    }

    public String getAssetdesc() {
        return assetdesc;
    }

    public void setAssetdesc(String assetdesc) {
        this.assetdesc = assetdesc;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public JSONObject getJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("RFID", rfid);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  obj;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public JSONObject getJsonObject1() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ASSETID", assetid);
            obj.put("PUTWAY_LOC", location);
            obj.put("RFID", rfid);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  obj;
    }

    public JSONObject getJsonObject2() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("RFID", rfid);
            obj.put("ASSETID", assetid);
            obj.put("PUTWAY_LOC", location);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  obj;
    }

    public JSONObject getJsonObject3() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ASSETID", AppConstants.EMPTY);
            obj.put("PUTWAY_LOC", AppConstants.EMPTY);
            obj.put("RFID", AppConstants.EMPTY);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  obj;
    }

    public JSONObject getJsonObject4() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("RFID", AppConstants.EMPTY);
            obj.put("ASSETID", AppConstants.EMPTY);
            obj.put("PUTWAY_LOC", AppConstants.EMPTY);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  obj;
    }


}
