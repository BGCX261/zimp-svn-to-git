package com.zimp.gamedata;

import org.json.JSONException;
import org.json.JSONObject;

import com.zimp.GlobalPreferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MapTiles {

	private static final String TAG = "GameRules";
	
	String texturePath; // texture path
	String name; // name or the room or outside area

	// Description of the Tile
	boolean startupTile; // tile needs to be the first tile placed. (Foyer for
							// standard game)

	// Addon tile that it can attach only to a specific tile (For eq: Patio in
	// original ZimP)
	boolean isAddOnTile; // Does this tile only link to an existing tile?
	String baseTileName; // Name of the tile that this tile attaches to?

	// Area transitions (from inside to outside)
	// Next tile will be from current area unless area transition capable
	String area; // Area of the map (inside/outside for the original ZimP)
	boolean isAreaTransitionCapable; // Can tile cause area transition?

	// Base Tile - is this a base tile? (Like Dinning room in original ZimP)
	boolean isBaseTile; // Does
	String addOnTileName;

	// Free Item Location
	boolean isFreeItem; // Does tile provides a free item?
	String freeItemText; // Text related to getting the free item

	// Health Related Benefits/Deficits Rooms
	boolean isHealthItem; // Does this tile provide a health change?
	String healthText; // Display message for text change
	int healthChange; // Change + or - in terms of health points

	public static final int NO_EXIT = 1;
	public static final int STANDARD_EXIT = 2;
	public static final int SPECIAL_EXIT = 3;
	public static final int CONNECTED_EXIT = 4;
	
	
	public int[] exits; // Exit Doors/Outdoor paths - always an array of 4
						// Array positions will change when tile is rotated
	public MapTiles[] exitConnectedTo; // Name of the room connected to this tile
								// Specified in the rules

	// Orientation
	int rotation; // 0, 90, 180, 270 Degrees of rotation while placement
					// When rotated texture and array is rotated

	
	//////////////////////////////////////////////////////////////////
	// Dynamic Game Play related data
	// These fields are not parsed from the
	
	public Bitmap 	bitmap = null;		// Image of the room
	Context	context;
	boolean isActive;	// is this tile currently where the character is
	
	private static GlobalPreferences globalPreferences = GlobalPreferences
			.getGlobalPreferences();
	final String defaultBasePath = "/data/scenes/classicplay/"; // Default base

	// Constructor
	public MapTiles() {
		exits = new int[4]; // each room has only 4 exists
		if(exitConnectedTo == null)
			exitConnectedTo = new MapTiles[4]; // each room maybe connected to max 4
											// exits
		//numPlotItems = 0;
	}

	public MapTiles(JSONObject mT) {

		if(exitConnectedTo == null)
			exitConnectedTo = new MapTiles[4]; // each room maybe connected to max 4
		
		this.name = mT.optString("Name");
		this.texturePath = globalPreferences.getDataDir() + defaultBasePath + "images/" + mT.optString("TexturePath", "yard.png");
		//this.bitmap = BitmapFactory.decodeFile(this.texturePath);
		//this.imageWidth  = this.bitmap.getWidth();
		//this.imageHeight = this.bitmap.getHeight();

		this.startupTile = mT.optBoolean("StartupTile", false);
		this.isAddOnTile = mT.optBoolean("IsAddOnTile", false);
		if(this.isAddOnTile) {
			this.baseTileName = mT.optString("BaseTileName");
		}
		this.area = mT.optString("Area", "Inside");
		this.isAreaTransitionCapable = mT.optBoolean("IsAreaTransitionCapable", false);
		this.isBaseTile = mT.optBoolean("IsBaseTile", false);
		if(this.isBaseTile) {
			this.addOnTileName = mT.optString("AddOnTileName");
		}
		this.isFreeItem = mT.optBoolean("IsFreeItem", false);
		if(this.isFreeItem) {
			this.freeItemText = mT.optString("FreeItemText");
		}
		this.isHealthItem = mT.optBoolean("IsHealthItem", false);
		if(this.isHealthItem) {
			this.healthText = mT.optString("HealthText");
			this.healthChange = mT.optInt("HealthChange", 0);
		}
		this.exits = new int[4]; // each room has only 4 exists
		exits[0] = mT.optInt("ExitUp", NO_EXIT);
		exits[1] = mT.optInt("ExitLeft", NO_EXIT);
		exits[2] = mT.optInt("ExitDown", NO_EXIT);
		exits[3] = mT.optInt("ExitRight", NO_EXIT);
	}
	
	// Getter Setter functions
	public String getTexturePath() {
		return texturePath;
	}

	public void setTexturePath(String texturePath) {
		Log.d(TAG, "Read tile from memory:" + texturePath);
		this.texturePath = texturePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStartupTile() {
		return startupTile;
	}

	public void setStartupTile(boolean startupTile) {
		this.startupTile = startupTile;
	}

	public boolean isFreeItem() {
		return isFreeItem;
	}

	public void setFreeItem(boolean freeItem) {
		this.isFreeItem = freeItem;
	}

	public String getFreeItemText() {
		return freeItemText;
	}

	public void setFreeItemText(String freeItemText) {
		this.freeItemText = freeItemText;
	}

	public boolean isHealthItem() {
		return isHealthItem;
	}

	public void setHealthItem(boolean healthItem) {
		this.isHealthItem = healthItem;
	}

	public String getHealthText() {
		return healthText;
	}

	public void setHealthText(String healthText) {
		this.healthText = healthText;
	}

	public int getHealthChange() {
		return healthChange;
	}

	public void setHealthChange(int healthChange) {
		this.healthChange = healthChange;
	}


	public MapTiles getConnection(int direction) {
		return this.exitConnectedTo[(getRotation() + direction) % 4];
	}
	
	
	public int getExitIndex(int rotation, int exit) {		
		return (rotation + exit)%4;		
	}	
	
	// Gets up exit considering rotation
	public int getUpExit() {
		return this.exits[(getRotation() + 0) % 4];
	}
	
	public int getLeftExit() {
		return this.exits[((getRotation() + 1)%4)];
	}

	public int getBottomExit() {
		return this.exits[((getRotation() + 2)%4)];
	}
	public int getRightExit() {
		return this.exits[((getRotation() + 3)%4)];
	}
	
	public int[] getExits() {
		return exits;
	}

	public int getExit(int exitno) {
		return exits[exitno];
	}

	public void setExits(int[] exits) {
		this.exits = exits;
	}

	public void setExit(int exit, int exitno) {
		this.exits[exitno] = exit;
	}

	public boolean isAddOnTile() {
		return isAddOnTile;
	}

	public void setAddOnTile(boolean isAddOnTile) {
		this.isAddOnTile = isAddOnTile;
	}

	public String getBaseTileName() {
		return baseTileName;
	}

	public void setBaseTileName(String baseTileName) {
		this.baseTileName = baseTileName;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isAreaTransitionCapable() {
		return isAreaTransitionCapable;
	}

	public void setAreaTransitionCapable(boolean isAreaTransitionCapable) {
		this.isAreaTransitionCapable = isAreaTransitionCapable;
	}

	public boolean isBaseTile() {
		return isBaseTile;
	}

	public void setBaseTile(boolean isBaseTile) {
		this.isBaseTile = isBaseTile;
	}

	public String getAddOnTileName() {
		return this.addOnTileName;
	}

	public void setAddOnTileName(String addOnTileName) {
		this.addOnTileName = addOnTileName;
	}
	
	/*public boolean isPlotFinale() {
		return isPlotFinale;
	}

	public void setPlotFinale(boolean isPlotFinale) {
		this.isPlotFinale = isPlotFinale;
	}*/

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		int newrotation = ((rotation)%4);
		if(newrotation != this.rotation) {
			this.rotation = newrotation;
			
			// Invalidate the older bitmap to force reload
			if(this.bitmap != null) {
				this.bitmap.recycle();
				this.bitmap = null;
			}
			
		}
	}

	public void incrementRotation() {

		// Invalidate the older bitmap to force reload
		if(this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
		this.rotation = ((this.rotation + 1) % 4);
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/*public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}*/

}
