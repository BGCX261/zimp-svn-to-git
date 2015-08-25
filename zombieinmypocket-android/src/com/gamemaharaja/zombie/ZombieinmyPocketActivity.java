package com.gamemaharaja.zombie;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
//import com.gamemaharaja.zombie.OnlineAdsActivity;
//import com.google.ads.AdRequest;
//import com.google.ads.AdView;
import com.google.ads.* ;

public class ZombieinmyPocketActivity extends AndroidApplication  {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* The following code shall be enable to show ads as new activity's view /new Full screen 
         view */
        
        /* New Ads Activity: START */
        
        /* Intent intent = new Intent(this, OnlineAdsActivity.class);
        ZombieinmyPocketActivity.this.startActivity(intent);
        setContentView(R.layout.main);
        initialize(new ZombieApp(), false); */
        
        /* New Ads Activity: END */
        
        /* Comment the below code to show ads in new activity's view/ Full Screen view  */
        /* Relative Layout: START */
        /* Create relative layout & pack libgdx view in it. LibGDX will render in full screen,
        so, first pack libgdx view & then adview on top of it. Ads will be shown on bottom of screen */
        RelativeLayout layout = new RelativeLayout(this);
        
        /* get the libgdx view */
        View gameView = initializeForView(new ZombieApp(), false);
        
        /* Create new adRequest & adView */
        AdRequest adReq = new AdRequest();
        /* Request adSize as BANNER for Mobile devices, IAB_BANNER for tablets */
        /* PublisherId is added from Test account */
        AdView adView = new AdView(this, AdSize.BANNER, "a14e957a1b15ca1");
        //adReq.setTesting(true);
        
        //To test on Emulator set TEST_EMULATOR as device else actual device id
        adReq.addTestDevice(AdRequest.TEST_EMULATOR);
        Log.d(getClass().getName() , "Raising get advertisment request");
        adView.loadAd(adReq);
        
        // Add the libgdx view to the layout
        layout.addView(gameView);

        // Add the AdMob view
        RelativeLayout.LayoutParams adParams = 
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        layout.addView(adView, adParams);

        // show the main screen & adview
        setContentView(layout);
        /* Relative Layout: START */
    }
}