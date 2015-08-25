package com.zimp.play;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import org.json.JSONException;

import com.zimp.GlobalPreferences;
import com.zimp.R;
import com.zimp.gamedata.DevCards;
import com.zimp.gamedata.GameRules;
import com.zimp.gamedata.Item;
import com.zimp.gamedata.Item.ItemUse;
import com.zimp.gamedata.MapTiles;
import com.zimp.gamedata.DevCards.DevCardDetails;
import com.zimp.gamedata.Objectives;
import com.zimp.play.TileView.GameState;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ZimpAActivity extends Activity {

	// Global Preferences
	private static GlobalPreferences globalPreferences = GlobalPreferences.getGlobalPreferences();

	// Logging related
	private static final String TAG = GlobalPreferences.TAG;
	
	
	public static class GameState {
		public static final int NEW_TILE = 1;
		public static final int OLD_TILE = 2;
		public static final int PREPARE_FOR_FIGHT = 3;
		public static final int LETS_FIGHT = 4;
		public static final int READY_FOR_NEW_TILE = 5;
		public static final int OBJECTIVE_VERIFY = 6;
		public static final int OBJECTIVE_PROCESS = 7;
		public static final int OBJECTIVE_IGNORE = 8;
		public static final int OBJECTIVE_COMPLETE = 9;
		public static final int VICTORY = 10;
		
		public static final int DEAD = 99;
		
		
		public static int health = 6;
		public static int attack = 1;
		public static int time = 9;
		public static int state = 0;
		/* Items currently held by the user*/
		public static ArrayList<Item> items;

		/*the grid representing the map, if any element is null it represents as empty*/
		public static ArrayList<MapTiles> tiles = new ArrayList<MapTiles>();
		/*all indoor tiles excluding the foyer as foyer is startupTile*/
		public static ArrayList<MapTiles> indoorTiles = new ArrayList<MapTiles>();
		/*all outdoor tiles excluding the patio as its an adOnTiles*/
		public static ArrayList<MapTiles> outdoorTiles = new ArrayList<MapTiles>();
		/*a hash map of adOnTiles like patio in classic game we need hash map as they might be referenced through their name*/
		public static HashMap<String, MapTiles> addOnTiles = new HashMap<String, MapTiles>();
		/* the startup tile , foyer in classic play*/
		public static MapTiles startupTile;
		
		/* Stack of randomly stacked indoor tiles*/
		public static Stack<MapTiles> indoorStack = new Stack<MapTiles>();
		/* Stack of randomly stacked outdoor tiles*/
		public static Stack<MapTiles> outdoorStack = new Stack<MapTiles>();
		/* Stack of randomly stacked dev cards*/
		public static Stack<DevCards> devCardStack = new Stack<DevCards>();

		/* Active Map Tile and curresponding vars*/
		public static MapTiles activeMapTile = null;
		public static int activeMapTileLoc = -1;
		public static int activeAttachDirection;
		public static Objectives activeObjective = null;
		
		/* current Grid size*/
		public static int gridX = 3, gridY = 4;
		
		/* active dev card*/
		public static DevCards activeDevCard;
	}

	boolean done = false;

	
	GameRules	gameRules;
	
	Random random = new Random();
	
	GridView map;
	mapArrayAdapter mapAA;
	LayoutInflater layoutInflater;
	RelativeLayout rl;
	ImageView iv;
//	ImageView tileImage;
	ImageView tileImage, devCardImage;
	Button 	  item1Button, item2Button;
	Button    timeButton;
	Button 	  healthButton;
	ProgressBar healthBar;
	
	final static int FOYER = 0;
	final static int BATHROOM = 1;
	final static int BEDROOM = 2;
	final static int DINING_ROOM = 3;
	final static int EVIL_TEMPLE = 4;
	final static int FAMILY_ROOM = 5;
	final static int STORAGE = 6;
	final static int KITCHEN = 7;
	final static int GARAGE = 8;
	final static int GARDEN = 9;
	final static int GRAVEYARD = 10;
	final static int PATIO = 11;
	final static int SITTING_AREA = 12;
	final static int YARD = 13;
	final static int YARD2 = 14;
	final static int YARD3 = 15;
	
	final static int DIALOG_CHOOSE_ITEM_ID = 0;
	final static int DIALOG_ASK_FOR_ITEM_ID = 1;
	final static int DIALOG_ASK_FOR_FIGHT_ID = 2;
	final static int DIALOG_ASK_FOR_OBJECTIVE_ID = 3;
	
    Dialog itemDialog = null;
    Gallery itemsGallery = null;
    
    AlertDialog askItemDialog = null;
    AlertDialog askFightDialog = null;
    AlertDialog askObjectiveDialog = null;
    
    Random rand = null;
	
	public void initializeGameState() {
		GameState.items = new ArrayList<Item>();
	}
	
	public void initializeTiles() {
		
		for(int i=0; i < gameRules.MapTilesList.size(); i++) {
			MapTiles mapTile = gameRules.MapTilesList.get(i);
			
			if(mapTile.getName().compareTo(gameRules.getStartTileName()) == 0) {
//			if(mapTile.isStartupTile() == true) {
				if(GameState.startupTile == null) {
					GameState.startupTile = mapTile;
				} else {
					Log.e(GlobalPreferences.TAG, "More than 1 tile can not be startup tile error");
				}
			} else if(mapTile.isAddOnTile()) {
				GameState.addOnTiles.put(mapTile.getName(), mapTile);
			} else if(mapTile.getArea().compareTo("Inside") == 0) {
				GameState.indoorTiles.add(mapTile);
			} else if(mapTile.getArea().compareTo("Outside") == 0) {
				GameState.outdoorTiles.add(mapTile);
			}
		}
		GameState.activeMapTile = GameState.startupTile;
	}
	
	@SuppressWarnings("unchecked")
	public void stackTiles() {
		int totalTileCnt, currTileCnt;
		int randomTile;
		ArrayList<MapTiles> tiles;
		
		/** Stacking the indoor tiles*/
		currTileCnt = 0;
		tiles = (ArrayList<MapTiles>) GameState.indoorTiles.clone();
		totalTileCnt = tiles.size();
		while(currTileCnt < totalTileCnt) {
			randomTile = random.nextInt(totalTileCnt);
			//Log.d(TAG, "currnet randomNumer > " + randomTile + "  current Roomcnt > " + currTileCnt);
			if(tiles.get(randomTile) != null) {
				GameState.indoorStack.push(tiles.set(randomTile, null));
				currTileCnt++;
			}
		}
		
		/** Stacking the outdoor tiles*/
		currTileCnt = 0;
		tiles = (ArrayList<MapTiles>) GameState.outdoorTiles.clone();
		totalTileCnt = tiles.size();
		while(currTileCnt < totalTileCnt) {
			randomTile = random.nextInt(totalTileCnt);
			if(tiles.get(randomTile) != null) {
				GameState.outdoorStack.push(tiles.set(randomTile, null));
				currTileCnt++;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void stackDevCards() {
		int totalDevCardsCnt = gameRules.DevCardsList.size(), currDevCardCnt = 0;
		int randomDevCard;
		ArrayList<DevCards> DevCardsLocalList = (ArrayList<DevCards>) gameRules.DevCardsList.clone();
		
		while(currDevCardCnt < totalDevCardsCnt) {
			randomDevCard = random.nextInt(totalDevCardsCnt);
			if(DevCardsLocalList.get(randomDevCard) != null) {
				GameState.devCardStack.push(DevCardsLocalList.set(randomDevCard, null));
				currDevCardCnt++;
			}
		}
	}
	
	/** This function is called when u try to open a door next to current active door. This function decides if that door is openable 
	 * and if it is openable then it find the new attach direction of the new tile 
	 * @param currMapTileLoc - Current Map Tile location
	 * @param currMapTile - Current Active Map Tile
	 * @param currClickLoc - Where exactly was it clicked
	 * @return returns attach direction for new tile 0-south, 1-west, 2-north, 3 east
	 */
	public int findIfTileOpenableReturnAttachDirection(int currClickLoc) {
		int activeMapTileY = (int)GameState.activeMapTileLoc/GameState.gridX;
		int activeMapTileX = (int)GameState.activeMapTileLoc%GameState.gridX;
		int currClickY = (int)currClickLoc/GameState.gridX;
		int currClickX = (int)currClickLoc%GameState.gridX;
		int tileAngle = 0;
		Log.d(TAG, "maptileX > " + activeMapTileX + "  mapTileY  > " + activeMapTileY + "  currClickX > " + currClickX + "  currClickY > " + currClickY);
		
		if(activeMapTileX == currClickX) {
			/** Clicked either north or south entrance*/
			if(activeMapTileY == currClickY + 1) {
				/** Clicked north entrance*/
				Log.d(TAG, "north");
				tileAngle = 2;
			} else if(activeMapTileY + 1 == currClickY) {
				/** Clicked south entrance*/
				Log.d(TAG, "south");
				tileAngle = 0;
			} else {
				return -1;
			}
		} else if(activeMapTileY == currClickY) {
			/** Clicked either east or west entrance */
			if(activeMapTileX == currClickX + 1) {
				/** Clicked west entrance*/
				Log.d(TAG, "west");
				tileAngle = 1;
			} else if(activeMapTileX + 1 == currClickX) {
				/** Clicked east entrance*/
				Log.d(TAG, "east");
				tileAngle = 3;
			} else {
				return -1;
			}
		}
		else {
			return -1;
		}
		/** I survived till here hence processed further now*/
		/** Here I check the exits for the current active tile and add the currently already rotated angle for it. Then i check if tileAngle matches for it .
		 * I add +2 cause of U-0, R-1, B-2, L-3 calculation
		 */
		Log.d(TAG, "Choosing the tile > tileAngle" + tileAngle + "  currMapTile.angle > " + GameState.activeMapTile.getRotation() + " exits" + GameState.activeMapTile.exits[0]);
		if(((GameState.activeMapTile.exits[(tileAngle + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.STANDARD_EXIT) || ((GameState.activeMapTile.exits[(tileAngle + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.SPECIAL_EXIT)) {
			Log.d(TAG, "returning the tileAngle " + tileAngle);
			return tileAngle;
		}
		return -1;
	}
	
	/**
	 * This API is basically checks if end of an row or coloumn is reached and if reached then it adds extra row/col 
	 * to basically balance the map so that it always fits in the center 
	 */
	public void adjustGridIfNeeded() {
		int tileY = GameState.activeMapTileLoc/GameState.gridX;
		int tileX = GameState.activeMapTileLoc%GameState.gridX;
		int i = 0;
		//Log.d(TAG, "before update" + GameState.tiles.toString());

		/** There can not be a time when both new row as well as new column needs to be added*/
		if(tileY == 0) {
			/** We need to add 1 extra top row*/
			GameState.gridY++;
			for(i = 0; i < GameState.gridX; i++) {
				GameState.tiles.add(0, null);
			}
			GameState.activeMapTileLoc = GameState.activeMapTileLoc + GameState.gridX;
		} else if(tileY == GameState.gridY - 1) {
			/** We need to add 1 extra bottom row*/
			for(i = 0; i < GameState.gridX; i++) {
				GameState.tiles.add(null);
			}
			GameState.gridY++;
		} else if(tileX == 0) {
			/** We need to add 1 extra left column*/
			GameState.gridX++;
			for(i = 0; i < GameState.gridY; i++) {
				GameState.tiles.add(i*GameState.gridX, null);
			}
			GameState.activeMapTileLoc = GameState.activeMapTileLoc + tileY + 1;
		} else if(tileX == GameState.gridX - 1) {
			/** We need to add 1 extra right coloumn*/
			for(i = 0; i < GameState.gridY; i++) {
				GameState.tiles.add((i + 1)*GameState.gridX + i, null);
			}
			GameState.gridX++;
			GameState.activeMapTileLoc = GameState.activeMapTileLoc + tileY;
		} else {
			return;
		}
		//Log.d(TAG, "after update " + GameState.tiles.toString());
		map.setLayoutParams(new RelativeLayout.LayoutParams(198*GameState.gridX, 198*GameState.gridY));
		map.setNumColumns(GameState.gridX);
	}
	
	/** Get the next Dev Card*/
    public DevCards nextDevCard() {
    	if(GameState.devCardStack.isEmpty()) {
    		TimePasses();
    	}
		return (DevCards) GameState.devCardStack.pop();
    }
    
    public void TimePasses() {
    	GameState.time++;
    	if(GameState.time < gameRules.getEndTime().hour) {
    		Toast.makeText(getApplicationContext(), "Its " + GameState.time + " already", Toast.LENGTH_LONG).show();
    		stackDevCards();
    	} else {
    		GameOver();
    	}
    }
    
    public void GameOver() {
		Toast.makeText(getApplicationContext(), "Game Over!!", Toast.LENGTH_LONG).show();
    }
    
    public boolean objectivesComplete() {
    	Objectives obj = GameState.activeObjective; 
    	obj.ACHIEVED = true;
		Toast.makeText(getApplicationContext(), obj.PostActionText , Toast.LENGTH_LONG).show();
		
		if(obj.AchieveItems) {
			//TODO
		}
		
		if(obj.AchieveTile) {
			//TODO
		}
		
		if(obj.AchieveHealth > 0) {
			//TODO
		}

    	if(obj.AchieveTime > 0) {
			//TODO
    	}
    	
		Toast.makeText(getApplicationContext(), obj.AchieveText , Toast.LENGTH_LONG).show();
    	GameState.activeObjective = null;
    	
    	for(int i = 0; i < gameRules.ObjectivesList.size() ; i++) {
    		obj = gameRules.ObjectivesList.get(i);
    		if(obj.ACHIEVED == false) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean verifyObjectives() {
    	Log.d(GlobalPreferences.TAG, "Inside verifyObjectives");
    	Objectives obj = null;
    	boolean itemFound = false;
    	
    	for(int i = 0; i < gameRules.ObjectivesList.size() ; i++) {
    		obj = gameRules.ObjectivesList.get(i);
    		if(obj.ACHIEVED == true) {
    			continue;
    		}
        	
    		if(obj.NeedHealth > GameState.health) {
    			Log.d(GlobalPreferences.TAG, "returned due to need health" + GameState.health);
    			continue;
    		}
    		
    		if(obj.NeedTime < GameState.time) {
    			Log.d(GlobalPreferences.TAG, "returned due to time");
    			continue;
    		}
    		
    		Log.d(GlobalPreferences.TAG, "needed tile >" + GameState.activeMapTile.getName() + "  obj neededtile" + obj.NeededTile);
    		if(obj.NeedTile == true) {
    			if(obj.NeededTile.compareTo(GameState.activeMapTile.getName()) != 0) {
        			Log.d(GlobalPreferences.TAG, "returned due need tile");
    				continue;
    			}
    		}
    		
    		if(obj.NeedItems == true) {
    			Log.d(GlobalPreferences.TAG, "checking for item");
    			for(int j = 0; j < obj.NeededItems.size() ; j++) {
    				itemFound = false;
    				for(int k = 0; k < GameState.items.size() ; k++) {
    					if(obj.NeededItems.get(j).compareTo(GameState.items.get(k).name) == 0) {
    						itemFound = true;
    					}
    				}
    				if(itemFound == false) {
    					break;
    				}
    			}
    			if(itemFound == false) {
        			Log.d(GlobalPreferences.TAG, "returned due to item not found");
    				continue;
    			}
    		}

    		if(obj.NeedObjectives == true) {
    			Log.d(GlobalPreferences.TAG, "checking for already completed objectives");
    			for(int j = 0; j < obj.NeededObjectives.size() ; j++) {
    				if(gameRules.ObjectivesList.get(obj.NeededObjectives.get(j)).ACHIEVED == false) {
    					/** one of these objective are not yet compete*/
    					Log.d(GlobalPreferences.TAG, "one of these objective are not yet compete" + obj.NeededObjectives.get(j));
    					continue;
    				}
    			}
    		}
    		
    		GameState.activeObjective = obj;
    		
    		
    		Log.d(GlobalPreferences.TAG, "things look good till here");
    		/** If it reached here it means tile is correct as well as needed items are also present and health and time are also correct*/
			Toast.makeText(getApplicationContext(), obj.ObjectiveText , Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(), obj.PreActionText , Toast.LENGTH_LONG).show();
    		return true;
    	}
    	return false;
    }
    
    public void stateMachine(int state) {
    	Log.d(GlobalPreferences.TAG, "stateMachine state>" + state);
    	GameState.state = state;
    	DevCardDetails dcd = null;
    	switch(state) {
    	case GameState.READY_FOR_NEW_TILE:
    		break;
    	case GameState.VICTORY:
			Toast.makeText(getApplicationContext(), "You have won the game" , Toast.LENGTH_LONG).show();
    		break;
    	case GameState.OBJECTIVE_COMPLETE:
    		if(objectivesComplete() == true) {
    			stateMachine(GameState.VICTORY);
    		} else {
    			stateMachine(GameState.READY_FOR_NEW_TILE);
    		}
    		break;
    	case GameState.OBJECTIVE_VERIFY:
    		if(verifyObjectives() == true) {
    			showDialog(DIALOG_ASK_FOR_OBJECTIVE_ID);
    		} else {
    			stateMachine(GameState.READY_FOR_NEW_TILE);
    		}
    		break;
    	case GameState.OBJECTIVE_IGNORE:
    		GameState.activeObjective = null;
    		stateMachine(GameState.READY_FOR_NEW_TILE);
    		break;
    	case GameState.OBJECTIVE_PROCESS: 
    		GameState.activeDevCard = nextDevCard();
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));

    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_COMPLETE);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			GameState.health = GameState.health + dcd.count;
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_COMPLETE);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameState.NEW_TILE: 
    		GameState.activeDevCard = nextDevCard();
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));
    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			GameState.health = GameState.health + dcd.count;
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameState.OLD_TILE: 
    		GameState.activeDevCard = nextDevCard();
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));
    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			GameState.health = GameState.health + dcd.count;
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameState.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameState.PREPARE_FOR_FIGHT:
        	if(!GameState.items.isEmpty()) {
        		showDialog(DIALOG_CHOOSE_ITEM_ID);
        	} else {
        		stateMachine(GameState.LETS_FIGHT);
        	}
    		break;
    	case GameState.LETS_FIGHT:
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
	    	int undeadZombies = dcd.count - GameState.attack;
	    	if(undeadZombies > 0) {
	    		GameState.health = GameState.health - undeadZombies;
	    	}
	    	
	    	if(GameState.health <= 0) {
	    		stateMachine(GameState.DEAD);
	    	} else {
	    		if(GameState.activeObjective !=  null) {
		    		stateMachine(GameState.OBJECTIVE_COMPLETE);
	    		} else {
		    		stateMachine(GameState.OBJECTIVE_VERIFY);
	    		}
	    	}
    		break;
    	case GameState.DEAD :
			Toast.makeText(getApplicationContext(), "You died a horrible horrible death", Toast.LENGTH_LONG).show();
    		break;
    	}
    }	
/*		if(dcd.type == DevCardDetails.PLAIN_DEVCARD) {
			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
			done = true;
		} else if(dcd.type == DevCardDetails.HEALTH_DEVCARD) {
			GameState.health = GameState.health + dcd.count;
			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
			done = true;
		} else if(dcd.type == DevCardDetails.ZOMBIE_DEVCARD) {
			AskForZombieFight(dcd);
			//FIXME do the fight
		} else if(dcd.type == DevCardDetails.ITEM_DEVCARD) {
			//Toast.makeText(getApplicationContext(), "Do you wish to draw the next card to get the item?", Toast.LENGTH_LONG).show();
			AskForItem();
		}
    }*/
	
	/** This function basically asks the user if he wishes to choose the item*/
    /*public void AskForItem() {
    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
    	ad.setTitle("Item");
    	ad.setMessage("Do you wish to draw the next Dev Card? If you do the item shown in the next Dev Card will be offered to you!");

    	String YesString = "Yes I want the item";
    	ad.setPositiveButton(YesString, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(GlobalPreferences.TAG, "Yes I choose to take the item");
				DevCards itemDevCard = nextDevCard();
				Log.d(GlobalPreferences.TAG, "You got " + itemDevCard.item);
				Item item = gameRules.ItemsMap.get(itemDevCard.item);
				//FIXME : check how many items are allowed to be carried and then allow only those many
				Toast.makeText(getApplicationContext(), "You got " + item.name, Toast.LENGTH_LONG).show();

				GameState.items.add(item.clone());
				chooseNextRoom();
			}
		});

		String NoString = "No I want to save time";
		ad.setNegativeButton(NoString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				// do nothing
				Log.d(GlobalPreferences.TAG, "No I want to save the time");
				chooseNextRoom();
			}
		});
    	ad.setCancelable(true);
    	ad.show();
    }*/
    
/*    public void nextDevCardForItem() {
    	if(GameState.devCardStack.isEmpty()) {
    		TimePasses();
    	}
		activeDevCard = (DevCards) devCardStack.pop();
		devCardImage.setImageBitmap(activeDevCard.bitmap);
		Toast.makeText(getApplicationContext(), "You got " + activeDevCard.item, Toast.LENGTH_LONG).show();
    }*/
    
    public class ImageAdapter extends BaseAdapter {
//        int mGalleryItemBackground;
        private Context mContext;
        private ArrayList<Item> mItems;
        
        public ImageAdapter(Context c, ArrayList<Item> items) {
            mContext = c;
            mItems = items;
//            TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
//            mGalleryItemBackground = attr.getResourceId(
//                    R.styleable.HelloGallery_android_galleryItemBackground, 0);
//            attr.recycle();
        }

        public int getCount() {
            return mItems.size();
        }

        public Object getItem(int position) {
            return mItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	ImageView imageView;
        	if(convertView != null) {
        		imageView = (ImageView) convertView;
        	} else {
        		imageView = new ImageView(mContext);
        	}
        	        	
        	imageView.setImageBitmap(BitmapFactory.decodeFile(mItems.get(position).image));
            imageView.setLayoutParams(new Gallery.LayoutParams(150, 150));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            //imageView.setBackgroundResource(mGalleryItemBackground);
            return imageView;
        }
    }
    
    /*public void CheckForAvailableItems(DevCardDetails dcd) {
    	if(!GameState.items.isEmpty()) {
    		showDialog(DIALOG_CHOOSE_ITEM_ID);
    		
    		itemAlertDialog = new AlertDialog.Builder(this);
    		itemAlertDialog.setTitle("Zombie Fight");
    		itemAlertDialog.setView(itemsGallery);
    		itemAlertDialog.setCancelable(true);
    		itemAlertDialog = 
    		itemAlertDialog.show();
    	} else {
    	}
    }*/
    
    /*public void AskForZombieFight(DevCardDetails dcd) {
    	Log.d(GlobalPreferences.TAG, "AskForZombieFight {");
    	showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    	Log.d(GlobalPreferences.TAG, "AskForZombieFight }");
    }*/
    

    
/*    public void applyActiveMapTile(MapTiles activeMapTile) {
    	tileImage.setImageBitmap(activeMapTile.getBitmap());
    	tileImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(done == true) {
					Intent mapViewIntent = new Intent(TileView.this, ZimpAActivity.class);
					//tileViewIntent.putExtra("GameRules", globalPreferences.getDefaultRules());
					startActivity(mapViewIntent);
				}
			}
		});
    }*/

	
    protected Dialog onCreateDialog(int id, Bundle bundle) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		String YesString, NoString;
		
		Context mContext = getApplicationContext();
    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		
    	switch(id) {
	        case DIALOG_CHOOSE_ITEM_ID:
	        	itemsGallery = (Gallery) layoutInflater.inflate(R.layout.itemsview, null);
	    		itemsGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    			
	    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	            Toast.makeText(ZimpAActivity.this, "" + position, Toast.LENGTH_SHORT).show();
	    	            itemDialog.dismiss();
	    	            stateMachine(GameState.LETS_FIGHT);
	    	        }
	    	    });
	    		
	    		alertDialogBuilder.setTitle("Zombie Fight");
	    		alertDialogBuilder.setView(itemsGallery);
	    		alertDialogBuilder.setCancelable(true);
	    		itemDialog = alertDialogBuilder.create();
	    		return itemDialog;
	    		
	    		
	        case DIALOG_ASK_FOR_ITEM_ID: 
	        {
	        	alertDialogBuilder.setTitle("Do you wish to search for an item (costs 1 turn)? ");
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);

	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
        		image.setImageResource(R.drawable.searchforitem);
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Yes I want the item";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Yes I choose to take the item");
	    				DevCards itemDevCard = nextDevCard();
	    				
	    	    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(itemDevCard.image));

	    				Log.d(GlobalPreferences.TAG, "You got " + itemDevCard.item);
	    				//Log.d(GlobalPreferences.TAG , "keys insdie the hash map" + gameRules.ItemsMap.keySet().toString());
	    				Item item = gameRules.ItemsMap.get(itemDevCard.item);
	    				//FIXME : check how many items are allowed to be carried and then allow only those many
	    				Toast.makeText(getApplicationContext(), "You got " + item.name, Toast.LENGTH_LONG).show();

	    				GameState.items.add(item.clone());
	    				
	    				if(GameState.state == GameState.OBJECTIVE_PROCESS) {
	    					stateMachine(GameState.OBJECTIVE_COMPLETE);
	    				} else if(GameState.state == GameState.NEW_TILE || GameState.state == GameState.OLD_TILE) {
	    					stateMachine(GameState.OBJECTIVE_VERIFY);
	    				}
	    			}
	    		});

	    		NoString = "No I want to save time";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "No I want to save the time");
	    				if(GameState.state == GameState.OBJECTIVE_PROCESS) {
	    					stateMachine(GameState.OBJECTIVE_COMPLETE);
	    				} else if(GameState.state == GameState.NEW_TILE || GameState.state == GameState.OLD_TILE) {
	    					stateMachine(GameState.OBJECTIVE_VERIFY);
	    				}
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	        	askItemDialog = alertDialogBuilder.create();
	        	return askItemDialog;
	        }	
	        // Dialog Ask For Fight
	        // Information : 
	        //				Left  Pane: Health and Items
	        //  			Right Pane: Number of Zombies
	        // Yes:  Lets Fight 
	        // No :  Run Away    
	        case DIALOG_ASK_FOR_FIGHT_ID:
	        {	
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	        	
	        	
	        	// Step 1: Setup Left and Right text pane
	        	TextView leftText   = (TextView) layout.findViewById(R.id.leftText);
	        	TextView rightText   = (TextView) layout.findViewById(R.id.rightText);
	        	
	        	// TODO: Fix the exact counts
	        	// Find how much attacking power available
	        	leftText.setText("Health : [" + GameState.health + "]\n");
	        	rightText.setText("n Zombies attack you");
	        	

	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	        	int num = rand.nextInt(5); 
	        	switch(num) {
	        	case 0:
	        		image.setImageResource(R.drawable.zombie1);
	        		break;
	        	default:
	        	case 1: 
	        		image.setImageResource(R.drawable.zombies2);
	        		break;
	        	case 2:
	        		image.setImageResource(R.drawable.zombie3);
	        		break;
	        	case 3:
	        		image.setImageResource(R.drawable.zombie4);
	        		break;
	        	case 4: 
	        		image.setImageResource(R.drawable.zombie5);
	        		
	        		
	        	}

	        	// Step 3: Build the alert dialog
	        	alertDialogBuilder.setTitle("Would you like to fight or run?");
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Lets fight";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Lets fight");
	    				//DevCardDetails dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
	    				stateMachine(GameState.PREPARE_FOR_FIGHT);
	    				//CheckForAvailableItems(dcd);
	    				//FightZombies(dcd);
	    			}
	    		});

	    		NoString = "Run away";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				Log.d(GlobalPreferences.TAG, "I am running away .. I am a wuss");

	    				// Note: We loose a health point for running away.
	    				GameState.health--;
	    				if(GameState.state == GameState.OBJECTIVE_PROCESS) {
	    					stateMachine(GameState.OBJECTIVE_IGNORE);
	    				} else if(GameState.state == GameState.NEW_TILE || GameState.state == GameState.OLD_TILE) {
	    					stateMachine(GameState.OBJECTIVE_VERIFY);
	    				}
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	    		askFightDialog = alertDialogBuilder.create();
	    		return askFightDialog;
	    		
	        }	
	        case DIALOG_ASK_FOR_OBJECTIVE_ID:
	        	//bundle.getString("title");
	        	//bundle.getString("title");
	        	alertDialogBuilder.setTitle("DrawCard");
	        	alertDialogBuilder.setMessage("Do you wish to draw the next Dev Card? ");
	        	YesString = "Yes I want to";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				DevCards itemDevCard = nextDevCard();
	    	    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(itemDevCard.image));
	    				stateMachine(GameState.OBJECTIVE_PROCESS);
	    			}
	    		});

	    		NoString = "No I don't want to";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				stateMachine(GameState.OBJECTIVE_IGNORE);
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	        	askObjectiveDialog = alertDialogBuilder.create();
	        	return askObjectiveDialog;
	        default:
	    		break;
        }
        return null;
    }
    
    protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {
    	Log.d(GlobalPreferences.TAG, "inside onPrepareDialog");
        switch(id) {
        case DIALOG_CHOOSE_ITEM_ID:
			ArrayList<Item> possibleItems = new ArrayList<Item>();
			for(int i = 0; i < GameState.items.size(); i++) {
				Item item = GameState.items.get(i);
				for(int j = 0; j < item.uses.size(); j++) {
					ItemUse use = item.uses.get(j);
					if(use.useType.compareTo("attack") == 0) {
						possibleItems.add(item);
						break;
					}
				}
			}
			itemsGallery.setAdapter(new ImageAdapter(this, possibleItems));
			itemsGallery.setSelection(0);
        case DIALOG_ASK_FOR_ITEM_ID:
        	break;
        case DIALOG_ASK_FOR_FIGHT_ID:
        	DevCardDetails dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
        	Log.d(GlobalPreferences.TAG, "Setting the zombie count to " + dcd.count);
        	askFightDialog.setMessage("Do you wish to fight " + dcd.count + " zombies or do you wish to run away?");
        	break;
        default:
    		break;
        }
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// Make Window Fullscreen as it is a game
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		// Initialise Random Number Generator
    	rand = new Random();

		

		String rulesJsonPath = null;
    	Bundle extras = getIntent().getExtras();
    	if(extras != null) {
    		rulesJsonPath = extras.getString(GlobalPreferences.KEY_GAME_RULES);
    	} else {
    		rulesJsonPath = globalPreferences.getDefaultRules();
    	}

		
        //setContentView(R.layout.itemsview);
        setContentView(R.layout.play);

        try {

        	gameRules = new GameRules(rulesJsonPath);
		} catch (NullPointerException n) {
			Log.e(TAG, "Null Pointer Exception: " + n.getMessage());
		} catch (JSONException je) {
			Log.e(TAG, "JSONException :" + je.getMessage());
			je.printStackTrace();
		}

		// Setup all the Bars
		
        tileImage = (ImageView) findViewById(R.id.tile);
        healthButton = (Button) findViewById(R.id.health);
        healthBar = (ProgressBar) findViewById(R.id.healthBar);
        timeButton = (Button) findViewById(R.id.time);

        // Decipher the data to do initial stuff
        // Step 1: Health
        GameState.health = gameRules.getStartHP();
        healthButton.setText("Health ["+GameState.health+"/"+gameRules.getMaxHP()+"]");
        healthBar.setProgress(GameState.health*100/gameRules.getMaxHP());

        // Step 2: Time
        GameState.time = gameRules.getStartTime().hour;
        timeButton.setText("Time ["+gameRules.getStartTime().hour+":"+gameRules.getStartTime().minute+"]");
        
        // Setup 3: Dev Card Area
        devCardImage = (ImageView) findViewById(R.id.devcard);
        Bitmap b = BitmapFactory.decodeFile(globalPreferences.getDataDir() + "/data/scenes/classicplay/images/dev_card.png");
        devCardImage.setImageBitmap(b);

        
		layoutInflater = LayoutInflater.from(this);

		map = (GridView) findViewById(R.id.Map);
		mapAA = new mapArrayAdapter(this, GameState.tiles);
        map.setAdapter(mapAA);
        mapAA.notifyDataSetChanged();
        
        /* //Testing code to show the item's gallery in a alert box
        Item item = gameRules.ItemsMap.get("chain_saw");
		GameState.items.add(item.clone());
        item = gameRules.ItemsMap.get("gasoline");
		GameState.items.add(item.clone());
        item = gameRules.ItemsMap.get("oil");
		GameState.items.add(item.clone());
		CheckForAvailableItems(null);*/
		
/*		iv = new ImageView(this);
		rl = (RelativeLayout) findViewById(R.id.rl);
		
		iv.setImageResource((Integer) R.drawable.rotate_anticlock);
		iv.setVisibility(View.GONE);*/
		
		map.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				
				//if(position == GameState.activeMapTileLoc && !GameState.activeMapTile.isStartupTile()) {
					////Intent tileViewIntent	= new Intent(ZimpAActivity.this, TileView.class);
					////tileViewIntent.putExtra("GameRules", globalPreferences.getDefaultRules());
					////startActivity(tileViewIntent);
			    	//if(GameState.activeMapTile != null) {
			    		//applyActiveMapTile(GameState.activeMapTile);
			    		//nextDevCard();
			    	//}
					//return;
				//}
				
				map.bringChildToFront(v);
				
				if(position == GameState.activeMapTileLoc || GameState.state != GameState.READY_FOR_NEW_TILE) {
					Log.d(GlobalPreferences.TAG, "Pressing it  on the same tile ignore it or not yet ready to open a new tile ignore it");
					return;
				}
				
				MapTiles alreadyOpenedTile = GameState.tiles.get(position);
				if(alreadyOpenedTile != null) {
			        GameState.activeMapTile = alreadyOpenedTile;
					GameState.activeMapTile.setActive(true);
					GameState.activeMapTileLoc = position;
			        stateMachine(GameState.OLD_TILE);
				}
				
				int localActiveAttachDirection = findIfTileOpenableReturnAttachDirection(position);
				// When attachDirection is -1 it means it is not attachable
				if(localActiveAttachDirection >= 0) {

					Log.d(TAG, "Open a new Tile here");
					
					if((GameState.activeMapTile.exits[(localActiveAttachDirection + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.STANDARD_EXIT) {
						if(GameState.activeMapTile.getArea().compareTo("Inside") == 0) {
							if(!GameState.indoorStack.isEmpty()) {
								GameState.activeMapTile.setActive(false);
								GameState.activeMapTile = (MapTiles) GameState.indoorStack.pop();
							} else {
						        Toast.makeText(ZimpAActivity.this, "No indoor tiles left", Toast.LENGTH_LONG).show();
						        //FIXME
								return;
							}
						} else {
							if(!GameState.outdoorStack.isEmpty()) {
								GameState.activeMapTile.setActive(false);
								GameState.activeMapTile = (MapTiles) GameState.outdoorStack.pop();
							} else {
						        Toast.makeText(ZimpAActivity.this, "No outdoor tiles left", Toast.LENGTH_LONG).show();
						        //FIXME
								return;
							}
						}

						GameState.activeAttachDirection = localActiveAttachDirection;
						GameState.activeMapTile.setActive(false);
						for (int i = 0; i < 4; i++) {
							if(GameState.activeMapTile.exits[(i + GameState.activeAttachDirection) % 4] == MapTiles.STANDARD_EXIT) {
								break;
							}
							Log.d(TAG, "Rotating..");
							GameState.activeMapTile.setRotation(GameState.activeMapTile.getRotation()+1);
						}
					} else if((GameState.activeMapTile.exits[(localActiveAttachDirection + GameState.activeMapTile.getRotation() + 2) % 4]) == MapTiles.SPECIAL_EXIT) {
						/** Moving from inside to outside*/
						if(GameState.activeMapTile.getArea().compareTo("Inside") == 0) {
							if(GameState.activeMapTile.isBaseTile()) {
								GameState.activeMapTile.setActive(false);
								GameState.activeMapTile = (MapTiles) GameState.addOnTiles.get(GameState.activeMapTile.getAddOnTileName());
							}
						} else { /** moving from outside to inside*/
							//FIXME
						}
						GameState.activeAttachDirection = localActiveAttachDirection;
						for (int i = 0; i < 4; i++) {
							if(GameState.activeMapTile.exits[(i + GameState.activeAttachDirection) % 4] == MapTiles.SPECIAL_EXIT) {
								break;
							}
							Log.d(TAG, "Rotating..");
							GameState.activeMapTile.setRotation(GameState.activeMapTile.getRotation()+1);
						}
					}

					GameState.activeMapTile.setActive(true);
					GameState.activeMapTileLoc = position;
					GameState.tiles.set(position, GameState.activeMapTile);
					
			        mapAA.notifyDataSetChanged();
			        Toast.makeText(ZimpAActivity.this, "Press and hold the tile to rotate it", Toast.LENGTH_LONG).show();
			        
			        adjustGridIfNeeded();
			        stateMachine(GameState.NEW_TILE);
        			Log.d(TAG, "Setting the next tile to > " + GameState.activeMapTile.getName());
				}
			        
					//ImageView tile = (ImageView) v;
					//tile.setImageResource((Integer) activeMapTile.resourceId);
					//Matrix m = tile.getImageMatrix();
					//Log.d(TAG, "" + m.toString());
					//if(activeMapTile.angle > 0) {
						//Log.d(TAG, "Applying the actual rotation > " + activeMapTile.angle);
						////m.postRotate(activeMapTile.angle * 90, (tile.getWidth()/2), (tile.getHeight()/2));
						////m.postRotate(activeMapTile.angle * 90, (tile.getWidth()/2), (tile.getHeight()/2));
						////m.setRotate(activeMapTile.angle * 90, (tile.getWidth()/2), (tile.getHeight()/2));
						////m.preRotate(activeMapTile.angle * 90, (tile.getWidth()/2), (tile.getHeight()/2));
						//Log.d(TAG, "" + m.toString());
						////m.postScale(2, 2);
						//tile.setImageMatrix(m);
						////tile.invalidate();
						////tile.requestLayout();
						////map.requestLayout();
					//}

					//arg0.invalidate();
					//arg0.requestLayout();

			}
		});

		map.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
				if(position == GameState.activeMapTileLoc) {
					Log.d(TAG, "activeMap Tile Items long click detected and valid current tile anggle is >" + GameState.activeMapTile.getRotation() + "active attach direction > " + GameState.activeAttachDirection);
					for (int i = 1; i < 4; i++) {
						Log.d(TAG, "Trying i " + i);
						if(GameState.activeMapTile.exits[(i + GameState.activeMapTile.getRotation() + GameState.activeAttachDirection) % 4] == MapTiles.STANDARD_EXIT) {
							GameState.activeMapTile.setRotation((GameState.activeMapTile.getRotation() + i) % 4);
							Log.d(TAG, "Rotating..by > " + i);
							break;
						}
					}
					GameState.tiles.set(position, GameState.activeMapTile);
			        mapAA.notifyDataSetChanged();
				}
				return true;
			}
		});
        

		
		/*
		map.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int arg2, long arg3) {
				Log.d(TAG, "Items long click detected ");
				iv.setVisibility(View.VISIBLE);
				tileImage = (ImageView) v;
				int x = tileImage.getLeft();
				int y = tileImage.getTop();
				int dx = tileImage.getWidth();
				int dy = tileImage.getHeight();
				
				
				if(iv instanceof ImageView) {
					Log.d(TAG, "iv properly detected to be imageview" + iv.getId());
					
					
				}
				int idx = iv.getWidth();
				int idy = iv.getHeight();

				Log.d(TAG, "x>" + x + "  y>" + y + "  dx >" + dx + "  dy>" + dy + " idx >" + idx + " idy>" + idy );
				//m.postTranslate((x + dx - idx), (y + dy - idy));
				Matrix m = iv.getImageMatrix();
				m.setTranslate((x + dx - idx), (y + dy - idy));
				m.setScale(2, 2);
				iv.setImageMatrix(m);
				rl.requestLayout();
				arg0.requestLayout();

				Log.d(TAG, "Image is shown now try rotating");
				iv.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Log.d(TAG, "Rotating .. ");
						int x = tileImage.getLeft();
						int dx = tileImage.getWidth();
						int y = tileImage.getTop();
						int dy = tileImage.getHeight();
						Log.d(TAG, "x " + x + "  y " + y + " dx " + dx + " dy " + dy + " calc " + (x + dx/2));
						Matrix m = tileImage.getImageMatrix();
						
						m.postRotate(90, (dx/2), (dy/2));
						tileImage.setImageMatrix(m);
						tileImage.requestLayout();
						Log.d(TAG, "done :) ?");
						map.requestLayout();
					}
				});

				return false;
			}
		});
        
        */
        
        
        /*map.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(arg1 instanceof ImageView) {
					Log.d(TAG, "arg1 is imageview");
				}
				Log.d(TAG, "in on click" + arg2 + "< arg2  arg3>" + arg3);
				// TODO Auto-generated method stub
				ImageView v = (ImageView)arg1;
				int x = v.getLeft();
				int dx = v.getWidth();
				int y = v.getTop();
				int dy = v.getHeight();
				Log.d(TAG, "x " + x + "  y " + y + " dx " + dx + " dy " + dy + " calc " + (x + dx/2));
				Matrix m = v.getImageMatrix();
				
				//m.setRotate(90, (x + dx/2), (y + dy/2));
				m.preTranslate(-(dx/2), -(dy/2));
				m.postRotate(90);
				m.postTranslate((dx/2), (dy/2));
				v.setImageMatrix(m);
				//v.invalidate();
				
				//arg0.requestLayout();
				
				
				//arg0.scrollBy(1, 1);
				//ImageView iv = (ImageView) ZimpAActivity.this.findViewById(R.id.rotate);
				//iv.setImageResource((Integer) R.drawable.rotate_anticlock);
				
				arg0.requestLayout();

		    	//ImageView iv = new ImageView(ZimpAActivity.this);
				//iv.setLayoutParams(new GridView.LayoutParams(32, 32));
				//iv.setLayoutParams(new LayoutParams(32, 32));
				//iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
				//iv.setPadding(0, 0, 0, 0);
				//iv.setImageResource((Integer) R.drawable.rotate_anticlock);
				
			}
        	
        });*/

        //RelativeLayout container = (RelativeLayout) findViewById(R.id.Container);
        

        
        /*LinearLayout mLinearLayout = new LinearLayout(this);

        ImageView i = new ImageView(this);
        i.setImageResource(R.drawable.bedroom);
        i.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
        i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Add the ImageView to the layout and set the layout as the content view
        mLinearLayout.addView(i);
        setContentView(mLinearLayout);*/
        
		/*Resources res = this.getResources();
		Drawable bathroom = res.getDrawable(R.drawable.bathroom);
		Drawable bedroom = res.getDrawable(R.drawable.bedroom);
		Drawable dining_room = res.getDrawable(R.drawable.dining_room);
		Drawable evil_temple = res.getDrawable(R.drawable.evil_temple);
		Drawable family_room = res.getDrawable(R.drawable.family_room);
		Drawable foyer = res.getDrawable(R.drawable.foyer);
		Drawable garage = res.getDrawable(R.drawable.garage);
		Drawable garden = res.getDrawable(R.drawable.garden);
		Drawable graveyard = res.getDrawable(R.drawable.graveyard);
		Drawable kitchen = res.getDrawable(R.drawable.kitchen);
		Drawable patio = res.getDrawable(R.drawable.patio);
		Drawable sitting_area = res.getDrawable(R.drawable.sitting_area);
		Drawable storage = res.getDrawable(R.drawable.storage);
		Drawable yard = res.getDrawable(R.drawable.yard);
		Drawable yard2 = res.getDrawable(R.drawable.yard2);
		Drawable yard3 = res.getDrawable(R.drawable.yard3);*/
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
        /** Initialize indoor rooms and outdoor rooms and stack them randomly*/
        initializeGameState();
        initializeTiles();
        stackTiles();
        stackDevCards();

		GameState.activeMapTile.setActive(true);
		GameState.activeMapTileLoc = 7;
		Log.d(TAG, "intial length of tiles " + GameState.tiles.size());
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.add(null);
		GameState.tiles.set(7, GameState.activeMapTile);
		Log.d(TAG, "intial length of tiles " + GameState.tiles.size());
		
        map.setSelection(7);
        stateMachine(GameState.READY_FOR_NEW_TILE);

/*		tiles.add(R.drawable.bathroom);
        tiles.add(R.drawable.bedroom);
        tiles.add(R.drawable.dining_room);
        tiles.add(R.drawable.evil_temple);
        tiles.add(R.drawable.family_room);
        tiles.add(R.drawable.foyer);
        tiles.add(R.drawable.garage);
        tiles.add(R.drawable.garden);
        tiles.add(R.drawable.graveyard);
        tiles.add(R.drawable.kitchen);
        tiles.add(R.drawable.patio);
        tiles.add(R.drawable.sitting_area);
        tiles.add(R.drawable.storage);
        tiles.add(R.drawable.yard);
        tiles.add(R.drawable.yard2);
        tiles.add(R.drawable.yard3);*/

/*		Intent tileviewIntent	= new Intent(this, TileView.class);
		tileviewIntent.putExtra("GameRules", globalPreferences.getDefaultRules());
		startActivity(tileviewIntent);*/
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    }
}
