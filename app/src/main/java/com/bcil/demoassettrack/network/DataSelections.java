package com.bcil.demoassettrack.network;


import android.content.Context;
import android.support.v4.app.FragmentActivity;

import org.ksoap2.serialization.PropertyInfo;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Ninad Gawankar on 10-Jul-2017.
 */

public class DataSelections {

    public static String checkLogin(String username, String password, Context applicationContext) throws IOException, XmlPullParserException {
        PropertyInfo[] propertyInfo = new PropertyInfo[2];
        PropertyInfo piUserId = new PropertyInfo();
        piUserId.setName("strUserName");
        piUserId.setValue(username);
        piUserId.setType(Object.class);
        propertyInfo[0] = piUserId;
        PropertyInfo piPassword = new PropertyInfo();
        piPassword.setName("strPassword");
        piPassword.setValue(password);
        piPassword.setType(Object.class);
        propertyInfo[1] = piPassword;
        return  WebServiceGrabber.invokeWebService("ValidateUserLogin", propertyInfo,applicationContext).toString();
    }


    public static String fetchAssetIdAndDesc(String mode, String username, String assetid, String rfid, String status, FragmentActivity activity) throws IOException, XmlPullParserException {
        PropertyInfo[] propertyInfo = new PropertyInfo[5];
        PropertyInfo piMode = new PropertyInfo();
        piMode.setName("strMode");
        piMode.setValue(mode);
        piMode.setType(String.class);
        propertyInfo[0] = piMode;
        PropertyInfo piUserName = new PropertyInfo();
        piUserName.setName("strUserName");
        piUserName.setValue(username);
        piUserName.setType(String.class);
        propertyInfo[1] = piUserName;
        PropertyInfo piAssetId = new PropertyInfo();
        piAssetId.setName("strAssetId");
        piAssetId.setValue(assetid);
        piAssetId.setType(String.class);
        propertyInfo[2] = piAssetId;
        PropertyInfo piRfid = new PropertyInfo();
        piRfid.setName("strRfid");
        piRfid.setValue(rfid);
        piRfid.setType(String.class);
        propertyInfo[3] = piRfid;
        PropertyInfo piStatus = new PropertyInfo();
        piStatus.setName("strStatus");
        piStatus.setValue(status);
        piStatus.setType(String.class);
        propertyInfo[4] = piStatus;
        return  WebServiceGrabber.invokeWebService("MappingActivity", propertyInfo, activity).toString();

    }

    public static String assignLocation(String strMode, String getUserName, String assetId, String location, FragmentActivity activity) throws IOException, XmlPullParserException {
        PropertyInfo[] propertyInfo = new PropertyInfo[4];
        PropertyInfo piMode = new PropertyInfo();
        piMode.setName("strMode");
        piMode.setValue(strMode);
        piMode.setType(Object.class);
        propertyInfo[0] = piMode;
        PropertyInfo piUserId = new PropertyInfo();
        piUserId.setName("strUserName");
        piUserId.setValue(getUserName);
        piUserId.setType(Object.class);
        propertyInfo[1] = piUserId;
        PropertyInfo piAssetId = new PropertyInfo();
        piAssetId.setName("strAssetId");
        piAssetId.setValue(assetId);
        piAssetId.setType(Object.class);
        propertyInfo[2] = piAssetId;
        PropertyInfo piLoc = new PropertyInfo();
        piLoc.setName("strLoc");
        piLoc.setValue(location);
        piLoc.setType(Object.class);
        propertyInfo[3] = piLoc;
        return  WebServiceGrabber.invokeWebService("AssignLocation", propertyInfo, activity).toString();
    }

    public static String savePhysicalAudit(String getUserName, String strData, FragmentActivity activity) throws IOException, XmlPullParserException{
        PropertyInfo[] propertyInfo = new PropertyInfo[2];
        PropertyInfo piUserId = new PropertyInfo();
        piUserId.setName("strUserName");
        piUserId.setValue(getUserName);
        piUserId.setType(Object.class);
        propertyInfo[0] = piUserId;
        PropertyInfo piData = new PropertyInfo();
        piData.setName("strData");
        piData.setValue(strData);
        piData.setType(Object.class);
        propertyInfo[1] = piData;
        return  WebServiceGrabber.invokeWebService("PhysicalAudit", propertyInfo, activity).toString();
    }

    public static String validateRfid(String mode, String getUserName, String scanRfid, String strData, String module, String reason, FragmentActivity activity) throws IOException, XmlPullParserException {
        PropertyInfo[] propertyInfo = new PropertyInfo[5];
        PropertyInfo piMode = new PropertyInfo();
        piMode.setName("strMode");
        piMode.setValue(mode);
        piMode.setType(String.class);
        propertyInfo[0] = piMode;
        PropertyInfo piUserName = new PropertyInfo();
        piUserName.setName("strUserName");
        piUserName.setValue(getUserName);
        piUserName.setType(String.class);
        propertyInfo[1] = piUserName;
        PropertyInfo piRfid = new PropertyInfo();
        piRfid.setName("strRfid");
        piRfid.setValue(scanRfid);
        piRfid.setType(String.class);
        propertyInfo[2] = piRfid;
        PropertyInfo piData = new PropertyInfo();
        piData.setName("strData");
        piData.setValue(strData);
        piData.setType(String.class);
        propertyInfo[3] = piData;
        PropertyInfo piReason = new PropertyInfo();
        piReason.setName("strReason");
        piReason.setValue(reason);
        piReason.setType(String.class);
        propertyInfo[4] = piReason;
        if(module.equals("SCRAPPING")){
            return  WebServiceGrabber.invokeWebService("ScrapScanning", propertyInfo, activity).toString();
        }else {
            return  WebServiceGrabber.invokeWebService("SoldScanning", propertyInfo, activity).toString();
        }

    }

    public static String validateRfidInLocTrans(String mode, String getUserName, String scanRfid, String location, String strData, FragmentActivity activity) throws IOException, XmlPullParserException {
        PropertyInfo[] propertyInfo = new PropertyInfo[5];
        PropertyInfo piMode = new PropertyInfo();
        piMode.setName("strMode");
        piMode.setValue(mode);
        piMode.setType(String.class);
        propertyInfo[0] = piMode;
        PropertyInfo piUserName = new PropertyInfo();
        piUserName.setName("strUserName");
        piUserName.setValue(getUserName);
        piUserName.setType(String.class);
        propertyInfo[1] = piUserName;
        PropertyInfo piRfid = new PropertyInfo();
        piRfid.setName("strRfid");
        piRfid.setValue(scanRfid);
        piRfid.setType(String.class);
        propertyInfo[2] = piRfid;
        PropertyInfo piLoc = new PropertyInfo();
        piLoc.setName("strLoc");
        piLoc.setValue(location);
        piLoc.setType(String.class);
        propertyInfo[3] = piLoc;
        PropertyInfo piData = new PropertyInfo();
        piData.setName("strData");
        piData.setValue(strData);
        piData.setType(String.class);
        propertyInfo[4] = piData;
        return  WebServiceGrabber.invokeWebService("LocationTransfer", propertyInfo, activity).toString();
    }

}
