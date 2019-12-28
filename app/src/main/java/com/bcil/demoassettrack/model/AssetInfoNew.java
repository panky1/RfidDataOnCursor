package com.bcil.demoassettrack.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class AssetInfoNew {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "assetid")
    private String assetid;
    @ColumnInfo(name = "assetdesc")
    private String assetdesc;
    @ColumnInfo(name = "rfid")
    private String rfid;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "isSelected")
    private boolean isSelected;

    public AssetInfoNew(String rfid){
        this.rfid = rfid;
    }

   /* public AssetInfoNew(String assetid, String assetdesc) {
        this.assetid = assetid;
        this.assetdesc = assetdesc;
    }*/

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


}

