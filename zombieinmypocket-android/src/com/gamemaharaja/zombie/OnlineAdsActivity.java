package com.gamemaharaja.zombie;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.* ;

public class OnlineAdsActivity extends Activity implements AdListener {
	String LOG_STR ="ZIMP_DEBUG_LOGS";
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.ads);
        Log.d(getClass().getName() , "onCreate Called...");
        
        //get the adview component from xml 
        AdView adView = (AdView)findViewById(R.id.zimpAdView);
        AdRequest adReq = new AdRequest();
        //adReq.setTesting(true);
        
        //To test on Emulator set TEST_EMULATOR as device else actual device id
        adReq.addTestDevice(AdRequest.TEST_EMULATOR);
        Log.d(getClass().getName() , "Raising get advertisment request");
        adView.loadAd(adReq);
        /* Add listeners for the ad response */
        adView.setAdListener(this);
    }
    
    @Override
    public void onReceiveAd(Ad ad) {
    	Log.d(LOG_STR, "Received ad ");
    }
    
    @Override
    public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
    	Log.d(LOG_STR, "failed to receive ad (" + errorCode + ")");
    }
    
    @Override
    public void onPresentScreen(Ad ad) {
    	Log.d(LOG_STR, "onPresentScreen ");
    }
    
    @Override
    public void onDismissScreen(Ad ad) {
    	Log.d(LOG_STR, "onDismissScreen ");
    }
    
    @Override
    public void onLeaveApplication(Ad ad) {
    	Log.d(LOG_STR, "onLeaveApplication ");
    } 
}