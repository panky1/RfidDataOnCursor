package com.bcil.demoassettrack.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import com.bcil.demoassettrack.model.AssetInfoNew;

import java.util.List;

/**
 * Created by Dev6 on 5/5/2018.
 */
@Dao
public interface DaoAccess {


    @Insert
    void insertOnlySingleRecord(AssetInfoNew assetInfo);

    @Insert
    void insertAssetIdAndDesc(AssetInfoNew assetInfo);

    @Query("SELECT * FROM AssetInfoNew")
    List<AssetInfoNew> fetchAllData();

    /*@Query("SELECT * FROM AssetInfoNew WHERE rfid =:urfid")
    AssetInfoNew getScanTagList(String urfid);*/

    @Query("SELECT * FROM AssetInfoNew WHERE rfid =:urfid")
    AssetInfoNew getRfidDataExist(String urfid);


    @Query("DELETE FROM AssetInfoNew WHERE rfid =:urfid")
    void deleteRfidData(String urfid);


    @Query("DELETE FROM AssetInfoNew")
    void deleteAllScanTagList();
}
