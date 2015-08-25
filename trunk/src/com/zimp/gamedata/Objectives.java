package com.zimp.gamedata;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.zimp.R;

import android.content.res.Resources;
import android.util.Log;

public class Objectives {
	public static final String TAG = "asd";

	public static final int NO_ACTION = 0;
	public static final int DRAW_CARD = 1;
	public static final int FIGHT_ZOMBIES = 2;
	public static final int REDUCE_HEALTH = 3;
	public static final int DROP_ITEM = 4;
					
	public String ObjectiveText, PreActionText, PostActionText, AchieveText;
	
	public boolean NeedItems = false, NeedTile = false;
	public ArrayList<String> NeededItems;
	public String NeededTile;
	
	public int NeedHealth = 1, NeedTime = 12;
	public boolean NeedObjectives = false;
	public ArrayList<Integer> NeededObjectives = null;
	
	public int Action = NO_ACTION, ActionCount = 0;
	
	public boolean AchieveItems = false, AchieveTile = false;
	public ArrayList<String> AchievedItems;
	public String AchievedTile;
	
	public int AchieveHealth = 0, AchieveTime = 0;
	
	public boolean ACHIEVED = false;

	void LogObjectives(Objectives obj) {
		Log.i(TAG, "ObjectiveText " + obj.ObjectiveText);
		Log.i(TAG, "PreActionText " + obj.PreActionText);
		Log.i(TAG, "PostActionText " + obj.PostActionText);
		Log.i(TAG, "AchieveText " + obj.AchieveText);
		Log.i(TAG, "NeedItems " + obj.NeedItems);
		Log.i(TAG, "NeedTile " + obj.NeedTile);
		Log.i(TAG, "NeededTile " + obj.NeededTile);
		Log.i(TAG, "NeedHealth " + obj.NeedHealth);
		Log.i(TAG, "NeedTime " + obj.NeedTime);
		Log.i(TAG, "Action " + obj.Action);
		Log.i(TAG, "ActionCount " + obj.ActionCount);
		Log.i(TAG, "AchieveItems " + obj.AchieveItems);
		Log.i(TAG, "AchieveTile " + obj.AchieveTile);
		Log.i(TAG, "AchievedTile " + obj.AchievedTile);
		Log.i(TAG, "AchieveHealth " + obj.AchieveHealth);
		Log.i(TAG, "AchieveTime " + obj.AchieveTime);
		
		for(int i = 0; obj.AchievedItems != null && i < obj.AchievedItems.size(); i++) {
			Log.i(TAG, "AchievedItems " + obj.AchievedItems.get(i));
		}
		for(int i = 0; obj.NeededItems != null && i < obj.NeededItems.size(); i++) {
			Log.i(TAG, "NeededItems " + obj.NeededItems.get(i));
		}
	}
	
	// To be only used by scene editor to make a new objective
	public Objectives() {
		
	}
	
	public String convertToJSON(Resources resources) throws JSONException {
		JSONObject jObject = new JSONObject();
		
		// Objectives Editor takes input of following fields
		// TODO d. Need Objectives - Dialog

		// a. ObjectiveText - EditText      
		jObject.put("ObjectiveText", this.ObjectiveText);
		// b. TriggerTile   - Spinner
		jObject.put("NeedTile", this.NeedTile);
		if(this.NeedTile == true) 
			jObject.put("NeededTile", this.NeededTile);
		// c. Need Items    - Dialog
		jObject.put("NeedItems", this.NeedItems);
		if(this.NeedItems == true) {
			JSONArray jArray = new JSONArray();

			for(int i = 0; i < this.NeededItems.size(); i++) {
				JSONObject jNeedItems = new JSONObject();
				jNeedItems.put("ItemName", this.NeededItems.get(i));
				jArray.put(jNeedItems);
			}
			jObject.put("NeededItems", jArray);
		}
		// e. Action        - Spinner (Constant)
		String[] mArray = resources.getStringArray(R.array.obj_ActionArray);
		jObject.put("Action", mArray[this.Action]);
		jObject.put("ActionCount", 1);

		// f. Preaction Text - EditText
		jObject.put("PreActionText", this.PreActionText);
		// g. Postaction Text - EditText
		jObject.put("PostActionText", this.PostActionText);
		// h. Gain Health    - RatingsBar
		jObject.put("AchieveHealth", this.AchieveHealth);
		jObject.put("NeedObjectives", this.NeedObjectives);

		jObject.put("NeedHealth", new Integer(1));
		jObject.put("NeedTime", new Integer(12));
		
		// i. Gain Item      - Spinner
		jObject.put("AchieveItems", this.AchieveItems);
		if(this.AchieveItems == true) {
			JSONObject jAchievedItems = new JSONObject();
			jAchievedItems.put("ItemName", this.AchievedItems.get(0));
			JSONArray jArray = new JSONArray();
			jArray.put(jAchievedItems);
			jObject.put("AchievedItems", jArray);
		}
		// j. Enable Tile    - Spinner
		if((this.AchieveTile == true) && (this.AchievedTile != null)) {
			jObject.put("AchieveTile", this.AchievedTile);
		}
		return jObject.toString(3);
	}
	
	public Objectives(JSONObject Objective) {
		
		try {
			int i = 0;
			
			this.ObjectiveText = Objective.optString("ObjectiveText");
			this.PreActionText = Objective.optString("PreActionText");
			this.PostActionText = Objective.optString("PostActionText");
			this.AchieveText = Objective.optString("AchieveText");

			if(Objective.getBoolean("NeedItems") == true) {
				this.NeedItems = true;
				this.NeededItems = new ArrayList<String>();
				JSONArray neededItems = Objective.getJSONArray("NeededItems");
				for(i = 0; i < neededItems.length(); i++) {
					JSONObject item = (JSONObject) neededItems.get(i);
					this.NeededItems.add(item.getString("ItemName"));
				}
			}
			if(Objective.getBoolean("NeedTile") == true) {
				this.NeedTile = true;
				this.NeededTile = Objective.getString("NeededTile");
			}
		
			this.NeedHealth = Objective.optInt("NeedHealth"); 
			this.NeedTime = Objective.optInt("NeedTime"); 
			if(Objective.getBoolean("NeedObjectives") == true) {
				this.NeedObjectives = true;
				this.NeededObjectives = new ArrayList<Integer>();
				JSONArray neededObjectives = Objective.getJSONArray("NeededObjectives");
				for(i = 0; i < neededObjectives.length(); i++) {
					this.NeededObjectives.add(neededObjectives.getInt(i));
				}
			}

			if(Objective.getString("Action").compareTo("DrawCard") == 0) {
				this.Action = Objectives.DRAW_CARD;
				this.ActionCount = Objective.getInt("ActionCount");
			} else if(Objective.getString("Action").compareTo("FightZombies") == 0) {
				this.Action = Objectives.FIGHT_ZOMBIES;
				this.ActionCount = Objective.getInt("ActionCount");
			} else if(Objective.getString("Action").compareTo("ReduceHealth") == 0) {
				this.Action = Objectives.REDUCE_HEALTH;
				this.ActionCount = Objective.getInt("ActionCount");
			} else if(Objective.getString("Action").compareTo("DropItem") == 0) {
				this.Action = Objectives.DROP_ITEM;
				this.ActionCount = Objective.getInt("ActionCount");
			} else {
				this.Action = Objectives.NO_ACTION;
				this.ActionCount = 0;
			}
			
			if(Objective.getBoolean("AchieveItems") == true) {
				this.AchieveItems = true;
				this.AchievedItems = new ArrayList<String>();
				JSONArray achievedItems = Objective.getJSONArray("AchievedItems");
				for(i = 0; i < achievedItems.length(); i++) {
					JSONObject item = (JSONObject) achievedItems.get(i);
					this.AchievedItems.add(item.getString("ItemName"));
				}
			}

			this.AchievedTile = Objective.optString("AchievedTile");
			if(this.AchievedTile.length() != 0) {
				this.AchieveTile = true;
			} else {
				this.AchieveTile = false;
			}

			this.AchieveHealth = Objective.optInt("AchieveHealth"); 
			this.AchieveTime = Objective.optInt("AchieveTime"); 

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//LogObjectives(this);
	}
	
};
