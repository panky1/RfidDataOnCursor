package com.bcil.demoassettrack.network;

/**
 * Created by Ninad Gawankar on 04-Jul-2017.
 */


import android.content.Context;

import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.PreferenceManager;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;

public class WebServiceGrabber {


    private static String NAMESPACE = AppConstants.NAMESPACE;
    private static String SOAP_ACTION = AppConstants.SOAP_ACTION;
    private static HttpTransportSE androidHttpTransport;
    private static HttpsTransportSE httpsTransportSE;
    private Context context;
    private URL url;

    public WebServiceGrabber(Context context) {
        this.context = context;
    }


    public static SoapPrimitive invokeWebService(String methodName, PropertyInfo[] propertyInfo, Context applicationContext)
            throws IOException, XmlPullParserException {
        try {
//            TestX509TrustManager.allowAllSSL();
            PreferenceManager preferenceManager = new PreferenceManager(applicationContext);
            String getServerUrl = preferenceManager.getPreferenceValues(AppConstants.SERVERURL);
            SoapObject request = new SoapObject(NAMESPACE, methodName);
            for (PropertyInfo aPropertyInfo : propertyInfo) {
                request.addProperty(aPropertyInfo);
            }
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
           if(AppConstants.URL == null){
                /*URL url = new URL(getServerUrl);
                int port = url.getProtocol().equals("http") ? 80 : 443;*/
                androidHttpTransport = new HttpTransportSE(getServerUrl, 60000);
//               httpsTransportSE = new HttpsTransportSE(url.getHost(), port, url.getPath(), 60000);
           }else {
                /*URL url = new URL(AppConstants.URL);
                int port = url.getProtocol().equals("http") ? 80 : 443;*/
                androidHttpTransport = new HttpTransportSE(AppConstants.URL, 60000);
//               httpsTransportSE = new HttpsTransportSE(url.getHost(), port, url.getPath(), 60000);
            }

            androidHttpTransport.call(SOAP_ACTION + methodName, envelope);
//            httpsTransportSE.call(SOAP_ACTION + methodName, envelope);
            return (SoapPrimitive) envelope.getResponse();
        }catch (Exception e) {
            throw e;
        }

    }



}