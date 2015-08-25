package com.zimp.gamedata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.zimp.GlobalPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DevCards {

	// Logging related
	private static final String TAG = "DevCards";
	private static GlobalPreferences globalPreferences = GlobalPreferences
			.getGlobalPreferences();
	final String defaultBasePath = "/data/scenes/classicplay/"; // Default base
	
	public class DevCardDetails {
		public static final int PLAIN_DEVCARD = 1;
		public static final int HEALTH_DEVCARD = 2;
		public static final int ZOMBIE_DEVCARD = 3;
		public static final int ITEM_DEVCARD = 4;
		
		public int type;
		public String message;
		public int count;
		DevCardDetails(int type, String message, int count) {
			this.type = type;
			this.message = message;
			this.count = count;
		}
		
		public void LogDevCardDetails() {
			Log.i(TAG, "type > " + this.type);
			Log.i(TAG, "message > " + this.message);
			Log.i(TAG, "count > " + this.count);
		}
		
		DevCardDetails(JSONObject devcarddetails) throws JSONException {
			if(devcarddetails.getString("type").compareTo("plain") == 0) {
				this.type = DevCardDetails.PLAIN_DEVCARD;
			} else if(devcarddetails.getString("type").compareTo("health") == 0) {
				this.type = DevCardDetails.HEALTH_DEVCARD;
			} else if(devcarddetails.getString("type").compareTo("zombie") == 0) {
				this.type = DevCardDetails.ZOMBIE_DEVCARD;
			} else if(devcarddetails.getString("type").compareTo("item") == 0) {
				this.type = DevCardDetails.ITEM_DEVCARD;
			}
			
			this.message = devcarddetails.optString("message");
			this.count = devcarddetails.optInt("count");
		}
	}
	
	public HashMap<String, DevCardDetails> devCardDetails;
	public String item;
	public String image;
	//public Bitmap bitmap;
	
	public void LogDevCards(DevCards devcard) {
		Log.i(TAG, "item >" + devcard.item);
		Log.i(TAG, "image > " + devcard.image);
		Set<String> keys = devcard.devCardDetails.keySet();
		
		for(Iterator<String> i = keys.iterator(); i.hasNext(); ) {
			String key = i.next();
			Log.d(TAG, "key >>" + key);
			devcard.devCardDetails.get(key).LogDevCardDetails();
		}
	}
	
	DevCards(JSONObject devCards) {
		try {
			this.devCardDetails = new HashMap<String, DevCardDetails>();
			for(Iterator<String> i = devCards.keys(); i.hasNext(); ) {
				String key = i.next();
				if(key.compareTo("item") == 0) {
					this.item = devCards.getString("item");
				} else if(key.compareTo("image") == 0) {
					this.image = globalPreferences.getDataDir() + defaultBasePath + "images/" + devCards.getString("image");
					Log.d(TAG, "bitmap path >" + this.image);
					//this.bitmap = BitmapFactory.decodeFile(globalPreferences.getDataDir() + defaultBasePath + "images/" + this.image);
				} else {
					this.devCardDetails.put(key, new DevCardDetails(devCards.getJSONObject(key)));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//LogDevCards(this);
	}
}
