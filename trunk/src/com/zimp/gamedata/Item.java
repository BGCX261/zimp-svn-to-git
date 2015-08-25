package com.zimp.gamedata;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zimp.GlobalPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Item {
	private static GlobalPreferences globalPreferences = GlobalPreferences
			.getGlobalPreferences();
	final String defaultBasePath = "data/scenes/classicplay/"; // Default base
	
	public class ItemUse{
		public String type;
		public String combineItem;
		public String useType;
		public boolean addInAttack;
		public boolean addInHealth;
		public boolean dependentItem;
		public int attack;
		public int health;
		public int noOfUsesLeft;
		
		ItemUse(JSONObject use) {
			this.type = use.optString("type", "single");
			this.combineItem = use.optString("combineItem");
			this.useType = use.optString("useType", "attack");

			this.addInAttack = use.optBoolean("addInAttack", false);
			this.addInHealth = use.optBoolean("addInHealth", false);
			this.dependentItem = use.optBoolean("dependentItem", false);
			this.noOfUsesLeft = use.optInt("noOfUsesLeft", 0);

			this.attack = use.optInt("attack", 0);
			this.health = use.optInt("health", 0);
		}
		
		ItemUse() {
		}
		
		public ItemUse clone() {
			ItemUse itemUse = new ItemUse();
			itemUse.type = this.type;
			itemUse.combineItem = this.combineItem;
			itemUse.useType = this.useType;
			itemUse.addInAttack = this.addInAttack;
			itemUse.addInHealth = this.addInHealth;
			itemUse.dependentItem = this.dependentItem;
			itemUse.noOfUsesLeft = this.noOfUsesLeft;
			itemUse.attack = this.attack;
			itemUse.health = this.health;
			return itemUse;
		}
		
		public void LogItemUse() {
			Log.d(GlobalPreferences.TAG, "type>" + this.type);
			Log.d(GlobalPreferences.TAG, "combineItem>" + this.combineItem);
			Log.d(GlobalPreferences.TAG, "useType>" + this.useType);
			Log.d(GlobalPreferences.TAG, "addInAttack>" + this.addInAttack);
			Log.d(GlobalPreferences.TAG, "addInHealth>" + this.addInHealth);
			Log.d(GlobalPreferences.TAG, "depedentItem>" + this.dependentItem);
			Log.d(GlobalPreferences.TAG, "noOfUsesLEft>" + this.noOfUsesLeft);
			Log.d(GlobalPreferences.TAG, "attack>" + this.attack);
			Log.d(GlobalPreferences.TAG, "health>" + this.health);
		}
	}
	
	public String name;
	public String image;
	public String text;
	public ArrayList<ItemUse> uses;
	public Bitmap bitmap;
	public boolean limitedUse;
	public int noOfUsesLeft;
	
	public Item() {
	}
	
	public void LogItem() {
		Log.d(GlobalPreferences.TAG, "name" + this.name);
		Log.d(GlobalPreferences.TAG, "image" + this.image);
		Log.d(GlobalPreferences.TAG, "text" + this.text);
		for(int i = 0; i < this.uses.size(); i++) {
			this.uses.get(i).LogItemUse();
		}
	}
	
	public Item(JSONObject item) throws JSONException {
		this.name = item.optString("name");
		this.image = globalPreferences.getDataDir() + defaultBasePath + "images/" + item.optString("image");
		//this.bitmap = BitmapFactory.decodeFile(this.image);
		this.text = item.optString("text");
		this.noOfUsesLeft = item.optInt("noOfUsesLeft", 0);
		this.limitedUse = item.optBoolean("limitedUse", false);
		uses = new ArrayList<ItemUse>();
		JSONArray itemUse = item.getJSONArray("uses");
		for(int i = 0; i< itemUse.length(); i++) {
			uses.add(new ItemUse(itemUse.getJSONObject(i)));
		}
		
		//this.LogItem();
	}
	
	/* We need to define our own clone function as we need deep copy of the clone as we will be reducing the number of Uses left parameter*/
	public Item clone() {
		Item item = new Item();
		item.name = this.name.toString();
		item.image = this.image.toString();
		item.text = this.text.toString();
		item.bitmap = this.bitmap;
		item.noOfUsesLeft = this.noOfUsesLeft;
		item.limitedUse = this.limitedUse;
		item.uses = new ArrayList<ItemUse>();
		for(int i = 0; i < this.uses.size(); i++) {
			item.uses.add(this.uses.get(i).clone());
		}
		return item;
	}
}

/*		"name" : "gasoline",
"image" : "item_gasoline.png",
"text" : "Can be combined with the Candles to destroy a group of zombies without giving them a chance to deal damage. Can be combined with Chain Saw to give two more Chain Saw uses. May only be used once.",
"uses" : [
	{
		"type" : "combine",
		"combineItem" : "candle",
		"limitedUse" : true,
		"noOfUsesLeft" : 1,
		"useType" : "attack",
		"addInAttack" : true,
		"attack" : 9999
	},
	{
		"type" : "combine",
		"combineItem" : "chain_saw",
		"limitedUse" : true,
		"noOfUsesLeft" : 1,
		"dependentItem" : true
	}
]
*/
