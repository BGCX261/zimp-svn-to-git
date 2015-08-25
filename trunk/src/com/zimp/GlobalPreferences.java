package com.zimp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import android.R.string;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

// Singleton class to keep all constants
// This class will be initiated only once (singleton class)
public class GlobalPreferences {

	public static final String TAG = "ZimP";
	
	// List of intent bundle extras
	public static final String KEY_OBJ_NO = "SeObjNo";
	public static final String KEY_OBJ_TEXT = "SeObjText";
	public static final String KEY_OBJ_JSON = "SeObjJson";
	public static final String KEY_OBJ_ITEMS = "SeObjItems";
	public static final String KEY_OBJ_MAP = "SeObjMap";
	public static final String KEY_OBJ_DEVCARDS = "SeObjDevCards";
	public static final String KEY_GAME_RULES  = "GameRules";
	public static final String KEY_LAUNCHER_RESET = "GameReset";
	public static final String KEY_LAUNCHER_DEBUG = "GameDebugExport";
	
	
	// Preference Constants
	public static final String PREF_LONG_GAMESPLAYED = "GamesPlayed";
	public static final String PREF_BOOL_SOUNDON = "SoundOn";
	public static final String PREF_STR_ARR_STORIES = "Stories";
	public static final String PREF_STR_ARR_MAPS = "Maps";
	public static final String PREF_STR_ARR_ITEMS = "Items";
	public static final String PREF_STR_ARR_RULES = "Rules";
	public static final String PREF_STR_ARR_DEVCARDS = "DevCards";
	public static final String PREFS_INT_DIFFICULTY = "Difficulty";
	
	// Values for 
	public final int UNDEFINED = 78634;
	public static final String JSON_NULL = "None";
	public static final String FOLDER_SDCARD = "/zimp";
	
	private long 	gamesPlayed = 0;	
	
	private String dataDirPath;
	private final String defaultRules = "/data/rules/classicplay.json";
	
	// Shared Preferences Handles
	SharedPreferences 	zimpPrefs;
	private Editor 		zimpPrefsEditor;
	
	// Shared Preferences in Android
	private Boolean		soundon;		// Whether sound is on or off		
	
	// Gaming Metadata
	private String[]			stories;		//
	private String[]			maps;
	private String[]			items;
	private String[]			rules;
	private String[]			devCards;

	// Save and Retrieve Packages from the system
	private String 				packagePathBase;

	private static GlobalPreferences ref; // reference to pass around

	// Noone can call the constructor excepting us
	private GlobalPreferences() {
	}

	public static GlobalPreferences getGlobalPreferences() {
		if (ref == null) {
			// its ok to call the constructor now
			ref = new GlobalPreferences();
		}
		return ref;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
		// that'll teach 'em
	}

	// Getter & Setter methods for variables
	public void setDataDir(String dataDirPath) {
		this.dataDirPath = dataDirPath;
	}

	public String getDataDir() {
		return dataDirPath;
	}

	public String getDefaultRules() {
		return defaultRules;
	}

	public long getGamesPlayed() {
		gamesPlayed = zimpPrefs.getLong(PREF_LONG_GAMESPLAYED, 0);
		return gamesPlayed;
	}

	public void incrementGamesPlayed() {
		gamesPlayed = zimpPrefs.getLong(PREF_LONG_GAMESPLAYED, 0);
		gamesPlayed = gamesPlayed + 1;
		zimpPrefsEditor.putLong(PREF_LONG_GAMESPLAYED, gamesPlayed);
		zimpPrefsEditor.commit();
	}

	public SharedPreferences getZimpPrefs() {
		return zimpPrefs;
	}

	public void setZimpPrefs(SharedPreferences zimpPrefs) {
		zimpPrefsEditor = zimpPrefs.edit();
		this.zimpPrefs = zimpPrefs;
	}

	public Boolean getSoundon() {
		soundon = zimpPrefs.getBoolean(PREF_BOOL_SOUNDON, true);
		return soundon;
	}

	public void setSoundon(Boolean soundon) {
		zimpPrefsEditor.putBoolean(PREF_BOOL_SOUNDON, soundon);
		zimpPrefsEditor.commit();
		this.soundon = soundon;
	}

	private String[] assimilateArray(String basename) {
		int num = zimpPrefs.getInt("num"+basename, 0);
		String[] collector = new String[num];
		for(int i=0; i<num; i++) {
			collector[i] = zimpPrefs.getString(basename+i, null);
		}
		
		return collector;
	}
	
	private void appendElement(String basename, String element) {
		String[] currentList = assimilateArray(basename);
		for(int i=0; i< currentList.length; i++) {
			// Duplicate Check, remove Duplicate Elements from the list
			if(currentList[i].matches(element))
				return;
		}
		
		int num = currentList.length;
		zimpPrefsEditor.putString(basename+num, element);
		num=num+1;
		zimpPrefsEditor.putInt("num"+basename, num);
		zimpPrefsEditor.commit();
	}
	
	private void clearPreference(String basename) {
		int num = zimpPrefs.getInt("num"+basename, 0);
		for(int i=0; i<num; i++) {
			zimpPrefsEditor.remove(basename+num);
		}
		zimpPrefsEditor.putInt("num"+basename, 0);
	}

	public void clearAllPreferences() {
		clearPreference(PREF_STR_ARR_STORIES);
		clearPreference(PREF_STR_ARR_RULES);
		clearPreference(PREF_STR_ARR_MAPS);
		clearPreference(PREF_STR_ARR_ITEMS);
		clearPreference(PREF_STR_ARR_DEVCARDS);
		zimpPrefsEditor.putLong(PREF_LONG_GAMESPLAYED, 0);
	}
	
	public String[] getStories() {
		stories = assimilateArray(PREF_STR_ARR_STORIES);
		return stories;
	}

	public void setStories(String story) {
		appendElement(PREF_STR_ARR_STORIES, story);
	}

	public String[] getMaps() {
		maps = assimilateArray(PREF_STR_ARR_MAPS);
		return maps;
	}

	public void setMaps(String map) {
		appendElement(PREF_STR_ARR_MAPS, map);
	}

	public String[] getItems() {
		items = assimilateArray(PREF_STR_ARR_ITEMS);
		return items;
	}

	public void setItems(String item) {
		appendElement(PREF_STR_ARR_ITEMS, item);
	}

	public int getDifficulty() {
		return zimpPrefs.getInt(PREFS_INT_DIFFICULTY, 0);
	}

	public void setDifficulty(int difficulty) {
		zimpPrefsEditor.putInt(PREFS_INT_DIFFICULTY, difficulty);
		zimpPrefsEditor.commit();
	}

	public String[] getRules() {
		rules = assimilateArray(PREF_STR_ARR_RULES);
		return rules;
	}

	public void setRules(String rules) {
		appendElement(PREF_STR_ARR_RULES, rules);
	}

	public void setDevCards(String devCards) {
		appendElement(PREF_STR_ARR_DEVCARDS, devCards);
	}

	public String[] getDevCards() {
		devCards = assimilateArray(PREF_STR_ARR_DEVCARDS);
		return devCards;
	}
	
	public void fileAddToPreferences(String filename) {
		if(filename.endsWith("map.json")) {
			setMaps(filename);
		} else if(filename.endsWith("story.json")) {
			setStories(filename);
		} else if(filename.endsWith("items.json")) {
			setItems(filename);
		} else if(filename.endsWith("play.json")) {
			setRules(filename);
		} else if(filename.endsWith("devcards.json")) {
			setDevCards(filename);
		}
	}

	public int getNumberIcon(int number) {
		
		switch((number % 10)) {
		case 0:
			return R.drawable.neon0;
		case 1: 
			return R.drawable.neon1;
		case 2: 
			return R.drawable.neon2;
		case 3:
			return R.drawable.neon3;
		case 4:
			return R.drawable.neon4;
		case 5:
			return R.drawable.neon5;
		case 6:
			return R.drawable.neon6;
		case 7:
			return R.drawable.neon7;
		case 8:
			return R.drawable.neon8;
		case 9: 
			return R.drawable.neon9;
		}
		
		return R.drawable.neon0;
	}

	public void setPackagePathBase(String packagePathBase) {
		this.packagePathBase = packagePathBase;
	}

	public String getPackagePathBase() {
		packagePathBase = Environment.getExternalStorageDirectory() + FOLDER_SDCARD;
		if(Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0)
			return packagePathBase;
		return null;
	}	
	
	
	
}
