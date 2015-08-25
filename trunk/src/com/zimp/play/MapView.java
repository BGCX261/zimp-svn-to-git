package com.zimp.play;

import com.zimp.R;
import com.zimp.play.ZimpAActivity.GameState;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

public class MapView  extends Activity {

	GridView map;
	mapArrayAdapter mapAA;
	LayoutInflater layoutInflater;
	ZoomControls mZoomControls;
	public static int zoomLevels = 2;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("asd", "inside onCreate");
		// Make Window Fullscreen as it is a game
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.play);

		layoutInflater = LayoutInflater.from(this);

		Log.d("asd", "GameState.ties length" + GameActivity.GameStateApp.tiles.size());
		map = (GridView) findViewById(R.id.Map);
		mZoomControls = (ZoomControls) findViewById(R.id.zoomControl);
		map.setColumnWidth(zoomLevels * 100);
		mapAA = new mapArrayAdapter(this, GameActivity.GameStateApp.tiles);
        map.setAdapter(mapAA);
        mapAA.notifyDataSetChanged();
        
        
        mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomControls.setIsZoomOutEnabled(true);
				zoomLevels++;
				if(zoomLevels == 4) {
					mZoomControls.setIsZoomInEnabled(false);
				}
				redrawMap();
			}
		});

        mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomControls.setIsZoomInEnabled(true);
				zoomLevels--;
				if(zoomLevels == 1) {
					mZoomControls.setIsZoomOutEnabled(false);
				}
				redrawMap();
			}
		});
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Log.d("asd", "setting the width" + zoomLevels * 100 * GameActivity.GameStateApp.gridX + " and height to " + zoomLevels * 100 * GameActivity.GameStateApp.gridY);
    	map.setNumColumns(GameActivity.GameStateApp.gridX);
		map.setLayoutParams(new RelativeLayout.LayoutParams(zoomLevels * 100 * GameActivity.GameStateApp.gridX, zoomLevels * 100 * GameActivity.GameStateApp.gridY));
		map.setSelection(GameActivity.GameStateApp.activeMapTileLoc);
    }
    
    public void redrawMap() {
		map.setLayoutParams(new RelativeLayout.LayoutParams(zoomLevels * 100 * GameActivity.GameStateApp.gridX, zoomLevels * 100 * GameActivity.GameStateApp.gridY));
		map.setColumnWidth(zoomLevels * 100);
    }
}
