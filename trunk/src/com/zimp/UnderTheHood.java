package com.zimp;


import com.zimp.play.ZimpAActivity;
import com.zimp.sceneeditor.SceneEditor;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class UnderTheHood extends ListActivity {

	private ZimpMenuAdapter mZimpMenuAdapter;
	private GlobalPreferences globalPreferences = GlobalPreferences.getGlobalPreferences();;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Make Window Fullscreen as it is a game
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.mainmenu);
		 
        String listOptions[] = {"Google Login","High Scores","Scene Editor","Master Reset", "Report Debug Logs"};
        mZimpMenuAdapter = new ZimpMenuAdapter(listOptions);
        setListAdapter(mZimpMenuAdapter);
		/*tv =  new TextView(this);

		// Create Loading Screen Background
		getWindow().setBackgroundDrawableResource(R.drawable.zimploading);

		setContentView(tv);*/

	}

    protected void onListItemClick (ListView l, View v, int position, long id) {
    	Log.i(GlobalPreferences.TAG, "position = " + position );
    	Log.i(GlobalPreferences.TAG, "id = " + id );
    	
    	switch (position) {
    		case 0: // Google Login & Scores
    			// TODO
     			/*
	    		Intent playIntent	= new Intent(this, ZimpAActivity.class);
	    		playIntent.putExtra(GlobalPreferences.KEY_GAME_RULES, globalPreferences.getDefaultRules());
	    		startActivity(playIntent);
	    		*/	
    		break;    		
    		case 1: // Scores & Rate App
    			// TODO
    			/*
	    		Intent mIntent	= new Intent(this, ZimpStoriesActivity.class);
	    		startActivity(mIntent);
	    		*/	
    		break;
    		case 2: // Scene Editor
    			Intent underIntent = new Intent(this, SceneEditor.class);
    			startActivityForResult(underIntent, 23837);
    		break;
    		case 3: // Master Reset
    			Intent resetIntent = new Intent(this, ZimpLauncher.class);
    			resetIntent.putExtra(GlobalPreferences.KEY_LAUNCHER_RESET, true);
    			startActivity(resetIntent);
    			break;
    		case 4: // Export Debug Package
    			Intent debugIntent = new Intent(this, ZimpLauncher.class);
    			debugIntent.putExtra(GlobalPreferences.KEY_LAUNCHER_DEBUG, true);
    			startActivity(debugIntent);
    			finish();
			break;
    	}

    }

	
	@Override 
	public void onStart() {
		super.onStart();
	}
	

	public class ZimpMenuAdapter extends BaseAdapter {
		
		private String[] 	mData;
		 
	    public ZimpMenuAdapter(String[] data) {
	        mData = data;
	    }

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
	        return mData.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
	        return mData[position];			
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView result;

	        if (convertView == null) {
//	        	LayoutInflater layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        	LayoutInflater layoutInflater = getLayoutInflater();
	            result = (TextView) layoutInflater.inflate(R.layout.launcher_listtext, parent, false);
	        } else {
	            result = (TextView) convertView;
	        }

	        final String text = (String) getItem(position);
	        result.setText(text);

	        return result;
	    }
	}	
}
