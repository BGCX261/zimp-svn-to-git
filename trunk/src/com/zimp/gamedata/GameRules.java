package com.zimp.gamedata;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zimp.GlobalPreferences;

import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.format.Time;
import android.util.Log;

public class GameRules {
	
	// Logging related
	private static final String TAG = "GameRules";

	// Global Preferences
	private static GlobalPreferences globalPreferences = GlobalPreferences
			.getGlobalPreferences();

	// Defaults in case some scenario wishes to use base game
	final String defaultBasePath = "/data/scenes/classicplay/"; // Default base
																// path
	final String defaultItemPath = "zimpitems.json"; // Default item path
														// relative to the Base
	final String defaultMapPath = "zimpmap.json"; // Default map tiles
	final String defaultDevCardsPath = "zimpdevcards.json"; // Default devcards

	// private String json; // JSON String as read from file

	private String scenarioName; // Name of Rules Variant
	private String scenarioImagePath; // Image of the scenario
	private String scenarioBasePath; // Base Path of scenario
	private String scenarioDescription; // Description of Scenario

	// Hit Points
	private int maxHP; // Max Hit Points
	private int startHP; // Start Hit Points

	// Items
	private String itemsJsonPath; // Path of items JSON file
	private int maxCarryItems; // Max items that person can keep in possession

	// Dev Cards & Time Progression
	private String devJsonPath; // Path of dev cards file
	private int devCards; // Total DevCards
	private int maxDiscardRounds; // Max discard rounds (a round is when the
									// whole set of dev cards is over)
	private Time startTime; // Start time
	private Time endTime; // Finish time (when time is up)
	private int hoursPerDiscard; // How many hours to move ahead when all dev
									// cards are discarded
	private int minutesPerActivity; // How many minutes progress per activity

	// Map Tiles
	private String mapTilesJsonPath; // JsonPath for map Tiles
	private String startTileName; // First map tile

	// Plot items
	private String finalPlotDestTileName; // Game gets over when player reaches
											// this tile
	private String[] plotItemName; // Plotitem names reqd, to finish the plot
	
	public ArrayList<Objectives> ObjectivesList;
	public ArrayList<DevCards> DevCardsList;
	public ArrayList<MapTiles> MapTilesList;
	public HashMap<String, Item> ItemsMap;

	// Number of Areas TODO 
	private int	numAreas;
	private int[] roomsPerArea;
	private int numIndoorRooms;
	
	// Parsed Map Tiles

	// Main Constructor that parses the JSON
	public GameRules(String jsonPath) throws JSONException {
		super();

		// Parse Map Tiles to setup data structure
		String json = GetJsonFromFile(jsonPath);
		if ((json == null) || (json.length() == 0)) {
			return;
		}

		JSONObject jObject;
		jObject = new JSONObject(json);

		Log.d(TAG, "Got JSON Object");

		// Parse scenario meta information
		JSONObject jMetaInfo = jObject.getJSONObject("MetaInfo");
		scenarioName = jMetaInfo.getString("ScenarioName");
		scenarioImagePath = jMetaInfo.optString("ScenarioImage", null);
		scenarioBasePath = jMetaInfo.optString("ScenarioBasePath");
		scenarioDescription = jMetaInfo.optString("ScenarioDescription", null);

		Log.d(TAG, "Got Meta Info");

		// Parse Game Scenario Rules
		JSONObject jRules = jObject.getJSONObject("Rules");
		maxHP = jRules.optInt("MaxHP");
		if(maxHP == 0) maxHP = 10;
		startHP = jRules.optInt("StartHP");
		if(startHP == 0)
		maxCarryItems = jRules.getInt("MaxCarryItems");

		startTime = new Time();
		endTime = new Time();
		startTime.set(0, 0, jRules.getInt("StartTime"), 0, 0, 0);
		endTime.set(0, 0, jRules.getInt("EndTime"), 0, 0, 0);

		hoursPerDiscard = jRules.optInt("HoursPerDiscard");
		minutesPerActivity = jRules.optInt("MinutesPerActivity");
		startTileName = jRules.getString("StartTile");

		// TODO: Fix Dest tile
//		jRules.getString("DestTile");

		Log.d(TAG, "Rules Parsed");

		if(jRules.has("Objectives")) {
			ObjectivesList = new ArrayList<Objectives>();
			JSONArray jObjectives = jRules.getJSONArray("Objectives");
			for(int i = 0; i < jObjectives.length(); i++) {
				ObjectivesList.add(new Objectives((JSONObject) jObjectives.get(i)));
			}
		}
		
		Log.d(TAG, "Objectives Parsed");
		
		// Get item paths
		JSONObject jPaths = jObject.getJSONObject("Paths");
		itemsJsonPath = jPaths.getString("ItemJsonPath");
		devJsonPath = jPaths.getString("DevCardsJsonPath");
		mapTilesJsonPath = jPaths.getString("MapTilesJsonPath");

		// If scenario uses defaults
		if (itemsJsonPath.matches("default")) {
			itemsJsonPath = defaultBasePath + "/" + defaultItemPath;
		} else {
			itemsJsonPath = itemsJsonPath;
		}
		if (devJsonPath.matches("default")) {
			devJsonPath = defaultBasePath + "/" + defaultDevCardsPath;
		} else {
			devJsonPath = devJsonPath;
		}
		if (mapTilesJsonPath.matches("default")) {
			mapTilesJsonPath = defaultBasePath + "/" + defaultMapPath;
		} else {
			mapTilesJsonPath = mapTilesJsonPath;
		}
		Log.d(TAG, "Items Parsed");
		LogJsonParsing();

		// Parse Map Tiles to setup data structure
		String jsonMap = GetJsonFromFile(mapTilesJsonPath);
		if (jsonMap == null) {
			return;
		}

		ParseMapTiles(jsonMap);
		Log.d(TAG, "Map Tiles Parsed");

		// Parse Map Tiles to setup data structure
		String jsonDevCards = GetJsonFromFile(devJsonPath);
		if (jsonDevCards == null) {
			return;
		}

		ParseDevCards(jsonDevCards);
		Log.d(TAG, "Dev Cards Parsed");
		
		// parse items 
		String jsonItems = GetJsonFromFile(itemsJsonPath);
		if (jsonItems == null) {
			return;
		}

		ParseItems(jsonItems);
		Log.d(TAG, "Items Parsed");
	}

	// Only to be used by scene editor
	public GameRules(String itemPath, String mapPath, String devCardsPath) throws JSONException {
		super();
		itemsJsonPath = itemPath;
		devJsonPath = devCardsPath;
		mapTilesJsonPath = mapPath;

		// Parse Map Tiles to setup data structure
		String jsonMap = GetJsonFromFile(mapTilesJsonPath);
		if (jsonMap == null) {
			return;
		}

		ParseMapTiles(jsonMap);
		Log.d(TAG, "Map Tiles Parsed");

		// parse items 
		String jsonItems = GetJsonFromFile(itemsJsonPath);
		if (jsonItems == null) {
			return;
		}

		ParseItems(jsonItems);
		Log.d(TAG, "Items Parsed");

		// Parse Map Tiles to setup data structure
		String jsonDevCards = GetJsonFromFile(devJsonPath);
		if (jsonDevCards == null) {
			return;
		}

		ParseDevCards(jsonDevCards);
		Log.d(TAG, "Dev Cards Parsed");
	
	}

	
	private String GetJsonFromFile(String jsonfilename) {

		InputStream is = null;
		String strJson = null;

		try {
			is = new BufferedInputStream(new FileInputStream(
					globalPreferences.getDataDir() + jsonfilename));
			strJson = readRawTextFile(is);
		} catch (NullPointerException n) {
			Log.e(TAG,
					"Null Pointer Exception opening json file "
							+ n.getMessage());
		} catch (FileNotFoundException f) {
			Log.e(TAG,
					"FileNotFoundException Opening Rules file "
							+ f.getMessage());
			f.printStackTrace();
		} catch (IOException i) {
			Log.e(TAG, "IOException Opening Rules file " + i.getMessage());
			i.printStackTrace();
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG,
							"IOException Closing Rules file " + e.getMessage());
					e.printStackTrace();
				}
			}
			return strJson;
		}
	}

	private void ParseDevCards(String json) throws JSONException{
		
		DevCardsList = new ArrayList<DevCards>();
		JSONArray devCards = new JSONArray(json);
		for(int i = 0; i < devCards.length(); i++) {
			DevCardsList.add(new DevCards((JSONObject) devCards.get(i)));
		}
	}
	
	private void ParseMapTiles(String json) throws JSONException {
		JSONObject jObject = new JSONObject(json);
		JSONArray jsonTiles = jObject.getJSONArray("Tiles");
		MapTilesList = new ArrayList<MapTiles>();

		for(int i = 0; i < jsonTiles.length(); i++) {
			MapTilesList.add(new MapTiles((JSONObject) jsonTiles.get(i)));
		}
	}
	
	private void ParseItems(String json) throws JSONException {
		JSONArray jsonItems = new JSONArray(json);
		ItemsMap = new HashMap<String, Item>();
		Item item;
		for(int i = 0; i < jsonItems.length(); i++) {
			item = new Item(jsonItems.getJSONObject(i)); 
			ItemsMap.put(item.name, item);
		}
	}
	
	// Parse map tiles to make up the map
/*	private void ParseMapTiles(String json) throws JSONException {
		JSONObject jObject;
		JSONArray arrJsonTiles;

		jObject = new JSONObject(json);
		arrJsonTiles = jObject.getJSONArray("Tiles");
		int numTiles = arrJsonTiles.length();
		MapTiles = new ArrayList<MapTiles>();
		MapTiles mapTiles;
		//[arrJsonTiles.length()];

		// Accumulate all the map tiles
		for (int i = 0; i < arrJsonTiles.length(); i++) {
			mapTiles = new MapTiles();
			MapTiles.add(mapTiles);
			mapTiles.setName(arrJsonTiles.getJSONObject(i).getString("Name"));
			if(mapTiles.getName().matches(this.getStartTileName())) {
				mapTiles.setStartupTile(true);				
			}
			mapTiles.setTexturePath(globalPreferences.getDataDir()+
					defaultBasePath + 
					"images/" +
					(arrJsonTiles.getJSONObject(i).getString("TexturePath")));
			mapTiles.setAddOnTile((arrJsonTiles.getJSONObject(i)
					.getBoolean("IsAddOnTile")));
			mapTiles.setBaseTileName((arrJsonTiles.getJSONObject(i)
					.getString("BaseTileName")));
			mapTiles.setArea((arrJsonTiles.getJSONObject(i)
					.getString("Area")));
			mapTiles.setAreaTransitionCapable((arrJsonTiles
					.getJSONObject(i).getBoolean("IsAreaTransitionCapable")));
			mapTiles.setBaseTile((arrJsonTiles.getJSONObject(i)
					.getBoolean("IsBaseTile")));
			mapTiles.setFreeItem((arrJsonTiles.getJSONObject(i)
					.getBoolean("IsFreeItem")));
			mapTiles.setHealthItem((arrJsonTiles.getJSONObject(i)
					.getBoolean("IsHealthItem")));
			mapTiles.setHealthText((arrJsonTiles.getJSONObject(i)
					.getString("HealthText")));
			mapTiles.setHealthChange((arrJsonTiles.getJSONObject(i)
					.getInt("HealthChange")));
			mapTiles.setExit(
					(arrJsonTiles.getJSONObject(i).getBoolean("ExitUp")), 0);
			mapTiles.setExit(
					(arrJsonTiles.getJSONObject(i).getBoolean("ExitLeft")), 3);
			mapTiles.setExit(
					(arrJsonTiles.getJSONObject(i).getBoolean("ExitDown")), 2);
			mapTiles.setExit(
					(arrJsonTiles.getJSONObject(i).getBoolean("ExitRight")), 1);
			Log.d(TAG, "Parsed MapTile *" + mapTiles.getName());
			// TODO : If the name = plotitem, do something and fill up the rest
			// of the stuff

		}
		this.numMapTiles = MapTiles.size(); 

		Log.d(TAG, "Got JSON Object for Map Tiles");

	}*/

	void LogJsonParsing() {

		Log.i(TAG, "Rules Description:");
		Log.i(TAG, "Scenario Name: " + scenarioName);
		Log.i(TAG, "Scenario Image: " + scenarioImagePath);
		Log.i(TAG, "Scenario Base Path: " + scenarioBasePath);
		Log.i(TAG, "Max HP: " + maxHP);
		Log.i(TAG, "StartHP: " + startHP);
		Log.i(TAG, "MaxCarryItems: " + maxCarryItems);
		Log.i(TAG, "MaxDiscardRounds: " + maxDiscardRounds);
		Log.i(TAG, "StartTime: " + startTime.hour);
		Log.i(TAG, "EndTime: " + endTime.hour);
		Log.i(TAG, "Hours per Discard: " + hoursPerDiscard);
		Log.i(TAG, "Minutes per Activity: " + minutesPerActivity);
		Log.i(TAG, "StartTile: " + startTileName);
		Log.i(TAG, "DestTile: " + finalPlotDestTileName);
	}

	public static String readRawTextFile(InputStream inputStream) {
		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while ((line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public String getScenarioImagePath() {
		return scenarioImagePath;
	}

	public void setScenarioImagePath(String scenarioImagePath) {
		this.scenarioImagePath = scenarioImagePath;
	}

	public String getScenarioBasePath() {
		return scenarioBasePath;
	}

	public void setScenarioBasePath(String scenarioBasePath) {
		this.scenarioBasePath = scenarioBasePath;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}

	public int getStartHP() {
		return startHP;
	}

	public void setStartHP(int startHP) {
		this.startHP = startHP;
	}

	public String getItemsJsonPath() {
		return itemsJsonPath;
	}

	public void setItemsJsonPath(String itemsJsonPath) {
		this.itemsJsonPath = itemsJsonPath;
	}

	public int getMaxCarryItems() {
		return maxCarryItems;
	}

	public void setMaxCarryItems(int maxCarryItems) {
		this.maxCarryItems = maxCarryItems;
	}

	public String getDevJsonPath() {
		return devJsonPath;
	}

	public void setDevJsonPath(String devJsonPath) {
		this.devJsonPath = devJsonPath;
	}

	public int getDevCards() {
		return devCards;
	}

	public void setDevCards(int devCards) {
		this.devCards = devCards;
	}

	public int getMaxDiscardRounds() {
		return maxDiscardRounds;
	}

	public void setMaxDiscardRounds(int maxDiscardRounds) {
		this.maxDiscardRounds = maxDiscardRounds;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public int getHoursPerDiscard() {
		return hoursPerDiscard;
	}

	public void setHoursPerDiscard(int hoursPerDiscard) {
		this.hoursPerDiscard = hoursPerDiscard;
	}

	public int getMinutesPerActivity() {
		return minutesPerActivity;
	}

	public void setMinutesPerActivity(int minutesPerActivity) {
		this.minutesPerActivity = minutesPerActivity;
	}

	public String getMapTilesJsonPath() {
		return mapTilesJsonPath;
	}

	public void setMapTilesJsonPath(String mapTilesJsonPath) {
		this.mapTilesJsonPath = mapTilesJsonPath;
	}

	public String getStartTileName() {
		return startTileName;
	}

	public void setStartTileName(String startTileName) {
		this.startTileName = startTileName;
	}

	public String getFinalPlotDestTileName() {
		return finalPlotDestTileName;
	}

	public void setFinalPlotDestTileName(String finalPlotDestTileName) {
		this.finalPlotDestTileName = finalPlotDestTileName;
	}

	public String[] getPlotItemName() {
		return plotItemName;
	}

	public void setPlotItemName(String[] plotItemName) {
		this.plotItemName = plotItemName;
	}

	public int getNumAreas() {
		return numAreas;
	}

	public void setNumAreas(int numAreas) {
		this.numAreas = numAreas;
	}

	public String getDefaultBasePath() {
		return defaultBasePath;
	}

	public String getDefaultItemPath() {
		return defaultItemPath;
	}

	public String getDefaultMapPath() {
		return defaultMapPath;
	}

	public String getDefaultDevCardsPath() {
		return defaultDevCardsPath;
	}

	public void setScenarioDescription(String scenarioDescription) {
		this.scenarioDescription = scenarioDescription;
	}

	public String getScenarioDescription() {
		return scenarioDescription;
	}

}
