package com.zimp.play;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/* Main Game Play class
 * 
 * Class is organised into 5 Sections
 * 
 * Section 1: Variable Declarations
 * Section 2: Initialisation Functions
 * Section 3: Android onXXX Functions
 * Section 4: Game State, Info and DevCard Manipulation functions
 * Section 5: Screen Adjustment Functions
 */

public class GameActivity extends Activity {

	// Global Preferences
	private static GlobalPreferences globalPreferences = GlobalPreferences.getGlobalPreferences();
	
	
	private static final int EXIT_TOP = 0;
	private static final int EXIT_LEFT = 1;
	private static final int EXIT_BOTTOM = 2;
	private static final int EXIT_RIGHT = 3;
	private static final int EXIT_CENTER = 3764;
	

	// Logging related
	private static final String TAG = GlobalPreferences.TAG;
	
	
	public static class GameStateApp extends Application {
		public static final int NEW_TILE = 1;
		public static final int OLD_TILE = 2;
		public static final int PREPARE_FOR_FIGHT = 3;
		public static final int LETS_FIGHT = 4;
		public static final int READY_FOR_NEW_TILE = 5;		// Usual Resting state
		public static final int OBJECTIVE_VERIFY = 6;
		public static final int OBJECTIVE_PROCESS = 7;
		public static final int OBJECTIVE_IGNORE = 8;
		public static final int OBJECTIVE_COMPLETE = 9;
		public static final int VICTORY = 10;
		public static final int PLACE_TILE = 11;
		public static final int RUN_AWAY = 12;
		public static final int DEAD = 99;

		public int difficulty = 0;
		public int health = 6;
		public int attack = 1;
		public int time = 9;
		public int state = DEAD;
		
		public String rulesJsonPath = null;
		
		/* Items currently held by the user*/
		public ArrayList<Item> items;

		/* the grid representing the map, if any element is null it represents as empty*/
		public static ArrayList<MapTiles> tiles = new ArrayList<MapTiles>();
		/* all indoor tiles excluding the foyer as foyer is startupTile*/
		public ArrayList<MapTiles> indoorTiles = new ArrayList<MapTiles>();
		/* all outdoor tiles excluding the patio as its an adOnTiles*/
		public ArrayList<MapTiles> outdoorTiles = new ArrayList<MapTiles>();
		/* a hash map of adOnTiles like patio in classic game we need hash map as they might be referenced through their name*/
		public HashMap<String, MapTiles> addOnTiles = new HashMap<String, MapTiles>();
		/* the startup tile , foyer in classic play*/
		public MapTiles startupTile;
		
		/* Stack of randomly stacked indoor tiles*/
		public Stack<MapTiles> indoorStack = new Stack<MapTiles>();
		/* Stack of randomly stacked outdoor tiles*/
		public Stack<MapTiles> outdoorStack = new Stack<MapTiles>();
		/* Stack of randomly stacked dev cards*/
		public Stack<DevCards> devCardStack = new Stack<DevCards>();

		/* Active Map Tile and curresponding vars*/
		public MapTiles activeMapTile = null;
		public MapTiles prevMapTile = null;
		public int activeAttachDirection;
		public Objectives activeObjective = null;
		public int turn = 27;
		
		/* current Grid size*/
		public static int activeMapTileLoc = -1;
		public static int gridX = 4, gridY = 4;
		
		/* active dev card*/
		public DevCards activeDevCard;
		public int playerEntry;
	}

	public GameStateApp GameState;
	
	boolean done = false;

	// Game Rules Related
	GameRules	gameRules;
	
	Random random = new Random();
	
	GridView map;
	mapArrayAdapter mapAA;
	LayoutInflater layoutInflater;
	RelativeLayout rl;
	ImageView iv;
	ImageView tileImage, devCardImage;
	Button 	  item1Button, item2Button;
	Button    timeButton;
	Button 	  healthButton;
	ProgressBar healthBar;
	
	final static int DIALOG_CHOOSE_ITEM_ID = 0;
	final static int DIALOG_ASK_FOR_ITEM_ID = 1;
	final static int DIALOG_ASK_FOR_FIGHT_ID = 2;
	final static int DIALOG_ASK_FOR_OBJECTIVE_ID = 3;
	final static int DIALOG_OBJECTIVE_COMPLETE = 4;
	final static int DIALOG_VICTORY = 5;
	final static int DIALOG_GOT_ITEM = 6;
	final static int DIALOG_REST = 7;
	final static int DIALOG_GAMEOVER = 8;
	final static int DIALOG_SHOW_ITEM = 9;
	final static int DIALOG_USE_ITEM = 10;
	final static int DIALOG_RUNAWAY = 11;
	final static int DIALOG_NOWAYOUT = 12;
	final static int DIALOG_SUICIDE = 13;
	final static int DIALOG_OBJECTIVES = 14;
	final static int DIALOG_SHOW_FIGHT = 15;
	
    Dialog itemDialog = null;
    Gallery itemsGallery = null;
    
    AlertDialog askItemDialog = null;
    AlertDialog askFightDialog = null;
    AlertDialog askObjectiveDialog = null;
    AlertDialog showItemDialog  = null;
    AlertDialog dialogObjectiveComplete = null;
    AlertDialog dialogVictory = null;
    AlertDialog dialogGotItem = null;
    AlertDialog dialogChooseItem = null;
    AlertDialog dialogUseItem = null;
    AlertDialog dialogGameOver = null;
    
    // Dialog related data
    Random rand = null;
    int     clickedItem = 0;
    int 	combineItem = 0;
    ItemUse use0, use1;


	private ItemUse runawayUse;
    
	public static class Screen  {
		
		// Game Area
		public static ImageButton exitLeft, exitRight, exitTop, exitBottom;
		public static ImageButton noExitLeft, noExitRight, noExitTop, noExitBottom;
		public static ImageView   mapTile;
		public static ImageButton playerCenter, playerLeft, playerRight, playerTop, playerBottom;
		public static ImageButton rotate, rotateok;
		
		// Status Area
		public static Button buttonHealth, buttonTime, buttonItemOne, buttonItemTwo;
		public static TextProgressBar healthBar, timeBar;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * Section 2: Initialisation Functions
	 * initialiseScreen - Initialises Screen Data Structure
	 */
	    
    public void initialiseScreen() {
    	// Initialise Exits
    	Screen.exitLeft = (ImageButton)findViewById(R.id.leftExit);
    	Screen.exitRight = (ImageButton)findViewById(R.id.rightExit);
    	Screen.exitBottom = (ImageButton)findViewById(R.id.bottomExit);
    	Screen.exitTop = (ImageButton)findViewById(R.id.topExit);

    	Screen.noExitLeft = (ImageButton)findViewById(R.id.leftNoExit);
    	Screen.noExitRight = (ImageButton)findViewById(R.id.rightNoExit);
    	Screen.noExitBottom = (ImageButton)findViewById(R.id.bottomNoExit);
    	Screen.noExitTop = (ImageButton)findViewById(R.id.topNoExit);
    	
    	// Initialise Map tile
    	Screen.mapTile   = (ImageView)findViewById(R.id.mapTile);
    	
    	// Initialise Player
    	Screen.playerLeft = (ImageButton)findViewById(R.id.playerLeft);
    	Screen.playerRight = (ImageButton)findViewById(R.id.playerRight);
    	Screen.playerBottom = (ImageButton)findViewById(R.id.playerBottom);
    	Screen.playerTop = (ImageButton)findViewById(R.id.playerTop);
    	Screen.playerCenter = (ImageButton)findViewById(R.id.playerCenter);
    	
    	Screen.rotate = (ImageButton)findViewById(R.id.rotateIcon);
    	Screen.rotateok = (ImageButton)findViewById(R.id.rotateok);
    	
    	// Text based Buttons
    	Screen.buttonHealth = (Button)findViewById(R.id.health);
    	Screen.buttonTime   = (Button)findViewById(R.id.time);
    	Screen.healthBar    = (TextProgressBar)findViewById(R.id.healthBar);
    	Screen.timeBar    	= (TextProgressBar)findViewById(R.id.timeBar);
    		
    }    
	public void initializeGameState() {
        GameState.health = gameRules.getStartHP();
        GameState.attack = 1;
        GameState.time   = gameRules.getStartTime().hour;
        GameState.state  = GameStateApp.READY_FOR_NEW_TILE;
        GameState.activeMapTile = null;
        GameState.activeObjective = null;
        GameState.activeDevCard = null;
        GameState.prevMapTile = null;
                
        GameState.difficulty = globalPreferences.getDifficulty();
        
	}
	/*
	 * Initialised for each game run
	 */
	public void alwaysReInitialiseGame() {
		
		// Game Rules are not a part of the application object
		// Hence need to be re-initialised
		if(gameRules == null) 
			readGameRules();
		
    	initialiseScreen();
		setupListeners();

    	// Decipher the data to do initial stuff
        // Step 1: Health
        Screen.healthBar = (TextProgressBar)findViewById(R.id.healthBar);
        Screen.healthBar.setProgressDrawable(getResources().getDrawable(R.drawable.healthprogress));
        setHealth(GameState.health);
        
        // Step 2: Time
        Screen.timeBar = (TextProgressBar)findViewById(R.id.timeBar);
        Screen.timeBar.setProgressDrawable(getResources().getDrawable(R.drawable.healthprogress));
        Screen.timeBar.setProgress(GameState.turn);
        Screen.timeBar.setText(GameState.turn + " turns");

        // Step 3: Show timebar and health again
		showTimeBar();
		setHealth(GameState.health);

		// Step 4: Dev Card Image
        devCardImage = (ImageView) findViewById(R.id.devcard);
		if(GameState.activeDevCard != null) {
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));
		} else {
			Bitmap b = BitmapFactory.decodeFile(globalPreferences.getDataDir() + "/data/scenes/classicplay/images/dev_card.png");
			devCardImage.setImageBitmap(b);
		}
	
	}
	
	/*
	 * Reread gameRules
	 */
	public void readGameRules() {
        try {
        	gameRules = new GameRules(GameState.rulesJsonPath);
		} catch (NullPointerException n) {
			Log.e(TAG, "Null Pointer Exception: " + n.getMessage());
			finish();
		} catch (JSONException je) {
			Log.e(TAG, "JSONException :" + je.getMessage());
			je.printStackTrace();
			finish();
		}

	}
	/*
	 * Restart of game freshly
	 */
	public void freshInitialiseGame() {
		
		// Read the Game Rules
		readGameRules();
		
		// Initialise Base state
        initializeGameState();

        // Initialise Items
		GameState.items = new ArrayList<Item>();
		
		// Initialise Tiles
        initializeTiles();
        stackTiles();

        // Initialise DevCards
        stackDevCards();

        // Adjust the difficulty 
		adjustDiffculty(GameState.difficulty);
        
		// Initialise number of turns in the game
		GameState.turn = GameState.devCardStack.size() * 
			(gameRules.getEndTime().hour - gameRules.getStartTime().hour) * gameRules.getHoursPerDiscard();
		
		// Activate map tile 
		GameState.activeMapTile.setActive(true);
		
		// Aalap
		Log.e("asd", "REFRESHING THE MAP");
		initializeMap();
		GameStateApp.activeMapTileLoc = 9;
		GameStateApp.tiles.set(GameStateApp.activeMapTileLoc, GameState.activeMapTile);
		
	}

	public void initializeMap() {
		GameStateApp.gridX = 4;
		GameStateApp.gridY = 4;
		if(!GameStateApp.tiles.isEmpty()) {
			GameStateApp.tiles.clear();
		}
		
		for(int i = 0; i < GameStateApp.gridX * GameStateApp.gridY ; i++) {
			GameStateApp.tiles.add(null);
		}
	}
	
	public void initializeTiles() {
		
		// Remove all current tiles
		if((GameState.indoorTiles != null) && (GameState.indoorTiles.size() > 0))
			GameState.indoorTiles.removeAll(GameState.indoorTiles);
		if((GameState.outdoorTiles != null) && (GameState.outdoorTiles.size() > 0))
			GameState.outdoorTiles.removeAll(GameState.outdoorTiles);
		if((GameState.addOnTiles != null) && (GameState.addOnTiles.size() > 0))
			GameState.addOnTiles.clear();
		
		for(int i=0; i < gameRules.MapTilesList.size(); i++) {
			MapTiles mapTile = gameRules.MapTilesList.get(i);
			
			if(mapTile.getName().compareTo(gameRules.getStartTileName()) == 0) {
				GameState.startupTile = mapTile;
			} else if(mapTile.getArea().compareTo("Inside") == 0) {
				GameState.indoorTiles.add(mapTile);
			} else if(mapTile.getArea().compareTo("Outside") == 0) {
				GameState.outdoorTiles.add(mapTile);
			}
			
			// Note: Area transition tiles are in two hashmaps
			if(mapTile.isAreaTransitionCapable() == true) {
				GameState.addOnTiles.put(mapTile.getName(), mapTile);
			}
		}
		
		GameState.activeMapTile = GameState.startupTile;
	}
	
	@SuppressWarnings("unchecked")
	public void stackTiles() {
		int totalTileCnt, currTileCnt;
		int randomTile;
		ArrayList<MapTiles> tiles;
		
		// Clear all stacks
		GameState.indoorStack.clear();
		GameState.outdoorStack.clear();
		
		/** Stacking the indoor tiles*/
		currTileCnt = 0;
		tiles = (ArrayList<MapTiles>) GameState.indoorTiles.clone();
		totalTileCnt = tiles.size();
		while(currTileCnt < totalTileCnt) {
			randomTile = random.nextInt(totalTileCnt);
			if(tiles.get(randomTile) != null) {
				// Only If the starttile is in the same area as a transition tile add it to the random stack
				if((tiles.get(randomTile).isAreaTransitionCapable() == false) || 
				           (GameState.startupTile.getArea().compareTo(tiles.get(randomTile).getArea()) == 0 )) {
					GameState.indoorStack.push(tiles.set(randomTile, null));
				}
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
				// Only If the starttile is in the same area as a transition tile add it to the random stack
				if((tiles.get(randomTile).isAreaTransitionCapable() == false) || 
				           (GameState.startupTile.getArea().compareTo(tiles.get(randomTile).getArea()) == 0 )) {
					GameState.outdoorStack.push(tiles.set(randomTile, null));
				}
				currTileCnt++;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void stackDevCards() {
		int totalDevCardsCnt = gameRules.DevCardsList.size(), currDevCardCnt = 0;
		int randomDevCard;
		ArrayList<DevCards> DevCardsLocalList = (ArrayList<DevCards>) gameRules.DevCardsList.clone();
		
		// Clear DevCards 
		GameState.devCardStack.clear();
		
		while(currDevCardCnt < totalDevCardsCnt) {
			randomDevCard = random.nextInt(totalDevCardsCnt);
			if(DevCardsLocalList.get(randomDevCard) != null) {
				GameState.devCardStack.push(DevCardsLocalList.set(randomDevCard, null));
				currDevCardCnt++;
			}
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Section 3: Android onXXX Functions
	 */	
    protected Dialog onCreateDialog(int id, Bundle bundle) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		String YesString, NoString;
		
		Context mContext = getApplicationContext();
    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		
    	switch(id) {
    	
    		case DIALOG_SHOW_FIGHT:
    		{
    			DevCardDetails dcd = null;
        		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    	    	int undeadZombies = dcd.count - GameState.attack;

    	    	// If attach = 0, then just use fists = 1
    	    	if(GameState.attack == 0)
    	    		GameState.attack = 1;

    	    	// Step 1: Lineup Left side of the view
	        	View layout = inflater.inflate(R.layout.play_dlg_fight,
  	        			(ViewGroup) findViewById(R.id.fight_root), false);

	        	// Step 1.1: Health
	        	ImageView iv = (ImageView)layout.findViewById(R.id.fight_health_double);
	        	iv.setImageResource(globalPreferences.getNumberIcon(GameState.health));
	        	
        		iv = (ImageView)layout.findViewById(R.id.fight_health_single);
	        	if(GameState.health > 9) {
	        		iv.setImageResource(R.drawable.neon1);
	        	} else {
	        		iv.setImageResource(R.drawable.neon0);
	        	}
	        	
	        	// Step 1.2: Health 
	        	iv = (ImageView)layout.findViewById(R.id.fight_attack);
	        	iv.setImageResource(globalPreferences.getNumberIcon(GameState.attack));
	        	
	        	//  Step 2: Right side of the view (Zombies)
	        	
	        	// Step 2.1 : Zombie Health
	        	iv = (ImageView)layout.findViewById(R.id.zombie_health_double);
	        	iv.setImageResource(globalPreferences.getNumberIcon(dcd.count));
	        	
    	    	if(undeadZombies > 0) {
    	    		setHealth(GameState.health - undeadZombies);
    	    	}

    	    	// Step 3: Post Health
	        	if(GameState.health > 0) {
	        		iv = (ImageView)layout.findViewById(R.id.fight_endhealth_double);
	        		iv.setImageResource(globalPreferences.getNumberIcon(GameState.health));

	        		iv = (ImageView)layout.findViewById(R.id.fight_endhealth_single);
	        		if(GameState.health > 9) {
	        			iv.setImageResource(R.drawable.neon1);
	        		} else {
	        			iv.setImageResource(R.drawable.neon0);
	        		}
	        	
	        	
	        		alertDialogBuilder.setTitle("You Survive the Fight!");
	        	} else {
	        		iv = (ImageView)layout.findViewById(R.id.fight_endhealth_double);
	        		iv.setVisibility(View.INVISIBLE);
	        		
	        		iv = (ImageView)layout.findViewById(R.id.fight_endhealth);
	        		iv.setVisibility(View.INVISIBLE);
	        		
	        		iv = (ImageView)layout.findViewById(R.id.fight_endhealth_single);
	        		iv.setImageResource(R.drawable.zombieicon);
	        	
	        		alertDialogBuilder.setTitle("Dead as a zombie?");	        		
	        	}

	        	alertDialogBuilder.setView(layout);

				
				
				
				
				YesString = "OK";
				alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(GlobalPreferences.TAG, "Fight Complete");
					removeDialog(DIALOG_SHOW_FIGHT);

					// If attach = 0, then just use fists = 1
		    	
			    	if(GameState.health <= 0) {
			    		stateMachine(GameStateApp.DEAD);
			    	} else {
			    		if(GameState.activeObjective !=  null) {
				    		stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
			    		} else {
				    		stateMachine(GameStateApp.OBJECTIVE_VERIFY);
			    		}
			    	}
				}	    			
				});
				
				alertDialogBuilder.setCancelable(true);
				dialogObjectiveComplete = alertDialogBuilder.create();
				return dialogObjectiveComplete;

    		}
    	
	        case DIALOG_OBJECTIVES:
	        {
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_objectives,
	        	                               (ViewGroup) findViewById(R.id.objectives_top), false);
	        	
	        	// Step 1: Scenario Information
	        	// 1. Scene Name
	        	// 2. Scene Image
	        	// 3. Scene Description	        	  
	        	TextView tv = (TextView)layout.findViewById(R.id.dl_obj_scenename);
	        	tv.setText(gameRules.getScenarioName());
	        	
 	        	ImageView image = (ImageView)layout.findViewById(R.id.dl_obj_sceneimage);
 	        	if(gameRules.getScenarioImagePath() != null)
 	        		image.setImageBitmap(BitmapFactory.decodeFile(gameRules.getScenarioImagePath()));
 	        	else
 	        		image.setImageResource(R.drawable.zombies2);
	    		
 	        	tv = (TextView)layout.findViewById(R.id.dl_obj_scenetext);
 	        	tv.setText(gameRules.getScenarioDescription()); 	        	
 	        	
 	        	// Step 2: Objectives Setup
 	        	// 1. For each Objective
 	        	// 2. Place Objective Text
 	        	// 3. Place What has to be done
 	        	// 4. Check whether completed on not and check the box
 	        	Objectives obj = null;
 	        	CheckBox cb = null;
 	        	 	        	
	 	       	for(int i = 0; i < gameRules.ObjectivesList.size() ; i++) {
	 	       		switch(i) {
	 	       		case 0:
	 	       			cb = (CheckBox) layout.findViewById(R.id.dl_obj_check0);
	 	       			tv = (TextView) layout.findViewById(R.id.dl_obj_req0);
	 	       			break;
	 	       		case 1:
	 	       			cb = (CheckBox) layout.findViewById(R.id.dl_obj_check1);
	 	       			tv = (TextView) layout.findViewById(R.id.dl_obj_req1);
	 	       			break;
	 	       		case 2:
	 	       			cb = (CheckBox) layout.findViewById(R.id.dl_obj_check2);
	 	       			tv = (TextView) layout.findViewById(R.id.dl_obj_req2);
	 	       			break;
	 	       		case 3:
	 	       			cb = (CheckBox) layout.findViewById(R.id.dl_obj_check3);
	 	       			tv = (TextView) layout.findViewById(R.id.dl_obj_req3);
	 	       			break;
	 	       		case 4:
	 	       			cb = (CheckBox) layout.findViewById(R.id.dl_obj_check4);
	 	       			tv = (TextView) layout.findViewById(R.id.dl_obj_req4);
	 	       			break;	 	       			
	 	       		} // end switch
	 	       		
	 	       		
	 	    		obj = gameRules.ObjectivesList.get(i);
	 	    		
	 	    		// Show the Objective
	 	    		cb.setVisibility(View.VISIBLE);
	 	    		cb.setText(obj.ObjectiveText);
	 	    		
	 	    		tv.setVisibility(View.VISIBLE);
	 	    		String objReqText = "Objective is to";

	 	    		// Construct Objective Requirements Text	 	    		
	 	    		if(obj.NeedTile == true) {
	 	    			objReqText = objReqText.concat(" Reach " + obj.NeededTile);
	 	    		}
	 	    		
	 	    		if(obj.NeedItems == true) {
 	    				objReqText.concat(" with Items ");
	 	    			for(int j = 0; j < obj.NeededItems.size() ; j++) {
	 	    				objReqText = objReqText.concat(obj.NeededItems.get(j));
	 	    			}
	 	    		}

	 	    		if(obj.NeedObjectives == true) {
	 	    			objReqText = objReqText.concat(" after finishing " + obj.NeededObjectives.size() + " other objectives ");
	 	    		}
	 	    		if(obj.NeedHealth > 0) {
	 	    			objReqText = objReqText.concat(" with atleast " + obj.NeedHealth + " health");
	 	    		}
	 	    		
	 	    		if(obj.ACHIEVED == true) {
	 	    			cb.setChecked(true);	 	    			
	 	    		}
	 	    		tv.setText(objReqText);
 	        	
	 	       	} // endfor
	 	       	
	        	alertDialogBuilder.setView(layout);

	        	// Dialog customisation
	        	alertDialogBuilder.setTitle("Survival Objectives");
	        	alertDialogBuilder.setIcon(R.drawable.target);
	        	
	        	YesString = "Understood";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	        		
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
						Log.d(GlobalPreferences.TAG, "Remove Dialog Objectives");
						removeDialog(DIALOG_OBJECTIVES);
	    			}	    			
	    		});

	        	
	        	
	    		alertDialogBuilder.setCancelable(true);
	    		
	    		// Remove dialog on cancel
	    		alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						Log.d(GlobalPreferences.TAG, "Remove Dialog Objectives");
						removeDialog(DIALOG_OBJECTIVES);
						
					}
			
				});
	        	return alertDialogBuilder.create();
	        }
	        case DIALOG_OBJECTIVE_COMPLETE:
	        {
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	
	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	    		image.setImageResource(R.drawable.objectiveposttext);
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Great! Let's Proceed!";
	        	alertDialogBuilder.setTitle(GameState.activeObjective.PostActionText);
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Objective Complete");
	    				removeDialog(DIALOG_OBJECTIVE_COMPLETE);
	    			}	    			
	    		});
	
	    		alertDialogBuilder.setCancelable(true);
	        	dialogObjectiveComplete = alertDialogBuilder.create();
	        	return dialogObjectiveComplete;

    	
	        }
	        case DIALOG_GAMEOVER:
	        {
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	
	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	    		image.setImageResource(R.drawable.gameover);
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Back to Menu";
	        	if(GameState.health > 0) 
	        		alertDialogBuilder.setTitle("Game Over : Out of Time");
	        	else
	        		alertDialogBuilder.setTitle("Game Over : You died a horrible death");
	        	
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Game Over");
	    				GameState.state = GameStateApp.DEAD; 
	    				gameRules = null;
	    				finish();
	    			}
	    		});
	
	    		alertDialogBuilder.setCancelable(true);
	        	dialogGameOver = alertDialogBuilder.create();
	        	return dialogGameOver;

    	
	        }
	        case DIALOG_SUICIDE:
	        {
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	    		image.setImageResource(R.drawable.suicide);
	        	alertDialogBuilder.setView(layout);

        		alertDialogBuilder.setTitle("Do you wish to quit?");
	        	
	        	YesString = "Commit Suicide";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Game Over by suicide");
	    				GameState.state = GameStateApp.DEAD;
	    				gameRules = null;
	    				finish();
	    			}
	    		});

	        	NoString = "No, Not Yet";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "");
	    				removeDialog(DIALOG_SUICIDE);
	    			}
	    		});
	
	    		alertDialogBuilder.setCancelable(true);
	        	return alertDialogBuilder.create();
	        }
	        case DIALOG_NOWAYOUT:
	        {	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	
	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	    		image.setImageResource(R.drawable.nowayout);

				TextView tv = (TextView)(layout.findViewById(R.id.leftText));
				tv.setText("Try to discover way to outside/inside going through older tiles OR complete objective to proceed");
				tv.setVisibility(View.VISIBLE);
	
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Ok, Countinue";
	        	alertDialogBuilder.setTitle("No Way Out");
	        		
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "No way out");
	    				removeDialog(DIALOG_NOWAYOUT);
	    				
	    			}
	    		});
	
	    		alertDialogBuilder.setCancelable(true);
	        	return alertDialogBuilder.create();

    	
	        }
	        case DIALOG_VICTORY:
	        {
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	
	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	    		image.setImageResource(R.drawable.victory);
	        	alertDialogBuilder.setView(layout);
	        	
	        	int score = (GameState.turn * GameState.difficulty) + (GameState.health / 3);
	        	TextView tv = (TextView)layout.findViewById(R.id.leftText);
	        	tv.setText((GameState.turn * GameState.difficulty) + "  points for speed\n" 
	        			+ (GameState.health/3) + " points for health");	        	
	        	
	        	tv = (TextView)layout.findViewById(R.id.rightText);
	        	tv.setText("  Score : " + score + "\n\n\nScore \n = Level x turns \n + Health / 3");
	        	GameState.state = GameStateApp.DEAD;
	        	
	        	YesString = "Great! Let's Proceed!";
	        	alertDialogBuilder.setTitle("You are a Survivor");
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Game Won");
	    				// TODO Log Information and move to next level
	    				finish();
	    			}
	    		});
	    		NoString = "Replay Scenario";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "Replay Scenario");
	    				// TODO : Fix this where scenario is replayed
	    				finish();
	    			}
	    		});
	
	    		alertDialogBuilder.setCancelable(false);
	        	dialogVictory = alertDialogBuilder.create();
	        	return dialogVictory;

    	
	        }
	        case DIALOG_CHOOSE_ITEM_ID:
	        {
	        	View layout = null;
        		layout = inflater.inflate(R.layout.play_dlg_itemchoose, 
	        				(ViewGroup)findViewById(R.id.itemchoose_top), false);
	        	use0 = checkUsage(0, GameState.state);
	        	use1 = checkUsage(1, GameState.state);
	        	
	        	if(use0 != null) {
	        		// Item No 1
	        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem1image));
	        		Item item = GameState.items.get(0);
	        		if(item.bitmap == null) {
	        			item.bitmap = BitmapFactory.decodeFile(item.image);
	        		}
    	    		image.setImageBitmap(item.bitmap);
        			image.setOnClickListener(itemFightClick);
        			image.setVisibility(View.VISIBLE);
        			
        			if(use0.type.compareTo("single") != 0){
        				TextView tv = (TextView)(layout.findViewById(R.id.dltext1));
        				tv.setText(item.name + "attack with" + use0.combineItem);
        				tv.setVisibility(View.VISIBLE);
        			} 
	        	}
	        	
	        	if(use1 != null) {
	        		// Item No 2
	        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem2image));
	        		Item item = GameState.items.get(1);
	        		if(item.bitmap == null) {
	        			item.bitmap = BitmapFactory.decodeFile(item.image);
	        		}
		    		image.setImageBitmap(item.bitmap);
        			image.setOnClickListener(itemFightClick);
        			image.setVisibility(View.VISIBLE);

	        		if(use1.type.compareTo("single") != 0){
        				TextView tv = (TextView)(layout.findViewById(R.id.dltext1));
        				tv.setText(item.name + "attack with" + use1.combineItem);
        				tv.setVisibility(View.VISIBLE);
        			}

	        	}


	    		alertDialogBuilder.setTitle("Select Weapons of Choice");
	    		alertDialogBuilder.setView(layout);
	        	alertDialogBuilder.setCancelable(false);
	    		itemDialog = alertDialogBuilder.create();
	    		return itemDialog;
	        }	
	        case DIALOG_GOT_ITEM:	
	        {
	        	View layout = null;
	        	try {
	        		layout = inflater.inflate(R.layout.play_dlg_itemchoose, 
	        				(ViewGroup)findViewById(R.id.itemchoose_top), false);
	        	} catch (InflateException i) {
	        		Log.e(GlobalPreferences.TAG, "Layout Exception : " + i.getMessage());
	        	} catch (ClassCastException c) {
	        		Log.e(GlobalPreferences.TAG, "ClassCast Exception : " + c.getMessage());
	        	}
	        	int numItems = GameState.items.size();
	        		        	
	        	if(numItems > 0) {
	        		
	        		showHand(0);
	        		// Item No 1
	        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem1image));
	        		Item item = GameState.items.get(0);
	        		if(item.bitmap == null) {
	        			item.bitmap = BitmapFactory.decodeFile(item.image);
	        		}
	        		try {
	    	    		image.setImageBitmap(item.bitmap);
	        		} catch (NullPointerException n) {
	        			Log.e(GlobalPreferences.TAG, "Null Pointer Exception "+ n.getMessage());
	        		}
        			image.setVisibility(View.VISIBLE);
	        		if(numItems > 2) {
	        			image.setOnClickListener(itemDiscardClick);
	        		}
	        	}
	        	
	        	if(numItems > 1) {

	        		showHand(1);
	        		// Item No 2
	        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem2image));
	        		Item item = GameState.items.get(1);
	        		if(item.bitmap == null) {
	        			item.bitmap = BitmapFactory.decodeFile(item.image);
	        		}
		    		image.setImageBitmap(item.bitmap);
	        		if(numItems > 2) {
	        			image.setOnClickListener(itemDiscardClick);
	        		}	        		
        			image.setVisibility(View.VISIBLE);
	        	}

	        	if(numItems > 2) {
	        		
	        		// Item No 3
	        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem3image));
	        		Item item = GameState.items.get(2);
	        		if(item.bitmap == null) {
	        			item.bitmap = BitmapFactory.decodeFile(item.image);
	        		}
		    		image.setImageBitmap(item.bitmap);
		    		
		    		// Enable the text box 
		    		TextView tv = (TextView) layout.findViewById(R.id.dltext1);
		    		tv.setVisibility(View.VISIBLE);
        			image.setOnClickListener(itemDiscardClick);
        			image.setVisibility(View.VISIBLE);
		    		
	        	}
	        	
	        	if(numItems > 2) 
	        		alertDialogBuilder.setTitle("New Item (can carry 2 items)");
	        	else
	        		alertDialogBuilder.setTitle("New Item");
	        	alertDialogBuilder.setView(layout);

	        	// Setup the OK things
	        	YesString = "OK";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {
	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				
	    				// Discard the 3rd item, if OK is pressed by default
	    				if(GameState.items.size() > 2) {
	    					discardItem(2);
	    				}
	    				
	    				if(GameState.state == GameStateApp.OBJECTIVE_PROCESS) {
	    					stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
	    				} else if(GameState.state == GameStateApp.NEW_TILE || GameState.state == GameStateApp.OLD_TILE) {
	    					stateMachine(GameStateApp.OBJECTIVE_VERIFY);
	    				}
	    				removeDialog(DIALOG_GOT_ITEM);
	    				dialogGotItem = null;
	    			}
	    		});
	        	
	        	alertDialogBuilder.setCancelable(false);
	        	dialogGotItem = alertDialogBuilder.create();
	        	
	        	return dialogGotItem;
	        	
	        }
	        case DIALOG_SHOW_ITEM: 
	        {
	        	
	        	alertDialogBuilder.setTitle("Examine Item");
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);

	        	// Step 2: Item in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	        	TextView tv = (TextView)layout.findViewById(R.id.leftText);

	        	Item item = GameState.items.get(clickedItem);
        		if(item.bitmap == null) {
        			item.bitmap = BitmapFactory.decodeFile(item.image);
        		}
        		image.setImageBitmap(item.bitmap);
        		
        		// Step 3: Text regarding use
        		if(item.limitedUse == false) {
        			tv.setText("Use: Unlimited");
        		} else if(item.noOfUsesLeft == 9999){
        			tv.setText("Use: Once");
        		} else {
        			tv.setText("Uses Left: " + item.noOfUsesLeft);
        		}
        		
	        	alertDialogBuilder.setView(layout);
        	
	        	// Step 3: Check, if there are direct uses now
	        	ItemUse use = checkUsage(clickedItem, GameState.state);
	        	if(clickedItem == 0) {
	        		use0 = use;
	        	} else {
	        		use1 = use;
	        	}

	        	
	        	if(use != null) {
					YesString = "Try to Use Item Now";
					alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

		    			@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				Log.d(GlobalPreferences.TAG, "Trying to use item now");
		    				removeDialog(DIALOG_SHOW_ITEM);
		    				
		    				ItemUse use = null;
		    				if(clickedItem == 0) use = use0;
		    				if(clickedItem == 1) use = use1;
		    				
	    					if(use.useType.compareTo("health") == 0) {
	    						int health = useItem(clickedItem, use, GameStateApp.READY_FOR_NEW_TILE);
	    						setHealth(GameState.health + health);
	    						return;
		    				}
		    				showDialog(DIALOG_USE_ITEM);
		    			}
		    		});
				}
				
	    		NoString = "OK, Done";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "No dont do anything");
	    				removeDialog(DIALOG_SHOW_ITEM);

	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	        	showItemDialog = alertDialogBuilder.create();
	        	return showItemDialog;
	        }	
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
	    	    		if(itemDevCard == null)	// Game Over 
	    	    			return;

	    	    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(itemDevCard.image));

	    				Log.d(GlobalPreferences.TAG, "You got " + itemDevCard.item);
	    				//Log.d(GlobalPreferences.TAG , "keys inside the hash map" + gameRules.ItemsMap.keySet().toString());
	    				Item item = gameRules.ItemsMap.get(itemDevCard.item);
	    				//FIXME : check how many items are allowed to be carried and then allow only those many
	    				Toast.makeText(getApplicationContext(), "You got " + item.name, Toast.LENGTH_LONG).show();
	    				GameState.items.add(item.clone());	    						
	    				
	    				showDialog(DIALOG_GOT_ITEM);
	    				
	    				if(GameState.state == GameStateApp.OBJECTIVE_PROCESS) {
	    					stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
	    				} else if(GameState.state == GameStateApp.NEW_TILE || GameState.state == GameStateApp.OLD_TILE) {
	    					stateMachine(GameStateApp.OBJECTIVE_VERIFY);
	    				}
	    			}
	    		});

	    		NoString = "No I want to save time";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "No I want to save the time");
	    				if(GameState.state == GameStateApp.OBJECTIVE_PROCESS) {
	    					stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
	    				} else if(GameState.state == GameStateApp.NEW_TILE || GameState.state == GameStateApp.OLD_TILE) {
	    					stateMachine(GameStateApp.OBJECTIVE_VERIFY);
	    				}
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	        	askItemDialog = alertDialogBuilder.create();
	        	return askItemDialog;
	        }	
	        case DIALOG_REST: 
	        {
	        	alertDialogBuilder.setTitle("Do you wish to rest (costs 1 turn)? ");
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);

	        	// Step 2: Zombie in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
        		image.setImageResource(R.drawable.rest);
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Yes, Gain 3 Health";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Yes I choose to take the item");
	    				
	    				// Discard Top devcard without resolving for gaining 3 Hitpoints
	    				DevCards devcard = nextDevCard();
	    	    		if(devcard == null)	// Game Over 
	    	    			return;

	    				setHealth(GameState.health + 3);
	    				
	    				Log.d(GlobalPreferences.TAG, "Rest 1 Turn");
	    				Toast.makeText(getApplicationContext(), "Resting gives you 3 Health", Toast.LENGTH_LONG).show();
	    				
	    			}
	    		});

	    		NoString = "No I want to save time";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "No Rest");
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	        	askItemDialog = alertDialogBuilder.create();
	        	return askItemDialog;
	        }	
	        case DIALOG_USE_ITEM: 
	        {
	        	alertDialogBuilder.setTitle("Combine Items ? ");
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_itemchoose,
	        	                               (ViewGroup) findViewById(R.id.itemchoose_top), false);

	        	Item item = GameState.items.get(clickedItem);
	        	ItemUse use = (clickedItem == 0)?use0:use1;
	        	Item combinedItem = (clickedItem == 0)?GameState.items.get(1):GameState.items.get(0); 
	        	
        		// Item No 1
        		ImageView image = (ImageView) (layout.findViewById(R.id.dlitem1image));
        		if(item.bitmap == null) {
        			item.bitmap = BitmapFactory.decodeFile(item.image);
        		}
   	    		image.setImageBitmap(item.bitmap);
   	    		
   	    		// Item No 2
        		image = (ImageView) (layout.findViewById(R.id.dlitem2image));
        		if(combinedItem.bitmap == null)
        			combinedItem.bitmap = BitmapFactory.decodeFile(combinedItem.image);
        		image.setImageBitmap(combinedItem.bitmap);

	        	TextView tv = (TextView)layout.findViewById(R.id.dltext1);
	        	if(use.dependentItem == true) {
	        		tv.setText("Combine/Refuel " + combinedItem.name + " with " + item.name);
	        	} else {
	        		tv.setText("Combine/Refuel " + item.name + " with " + combinedItem.name);
	        	}
	        	tv.setVisibility(View.VISIBLE);
        		
	        	alertDialogBuilder.setView(layout);	        	
	        	
	        	YesString = "Yes, Combine";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Yes I combine the items");
	    				int health = 0;
	    				ItemUse use = (clickedItem == 0)?use0:use1;

	    	        	Toast.makeText(getApplicationContext(), "Combined " +
	    						GameState.items.get(clickedItem).name + " with " +
	    						use.combineItem, 
	    						Toast.LENGTH_LONG).show();
	    				
	    				// Combine the items
    	        		health = useItem(clickedItem, use, GameState.state);
	    	        	
	    	        	// Increase Health, if required
	    	        	if(health != 0) {
	    	        		setHealth(health + GameState.health);
	    	        	}
	    				
	    				
	    			}
	    		});

	    		NoString = "No, Dont Combine Now";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				// do nothing
	    				Log.d(GlobalPreferences.TAG, "No Rest");
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
	        // 	Yes:  Lets Fight 
	        // 	No :  Run Away    
	        case DIALOG_ASK_FOR_FIGHT_ID:
	        {	
	        	
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
	        	                               (ViewGroup) findViewById(R.id.layout_root), false);
	        	
	        	// Step 1: Setup Left and Right text pane
	        	TextView leftText   = (TextView) layout.findViewById(R.id.leftText);
	        	TextView rightText   = (TextView) layout.findViewById(R.id.rightText);

	        	DevCardDetails dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
	        	Log.d(GlobalPreferences.TAG, "Setting the zombie count to " + dcd.count);
	        	
	        	// Find how much attacking power available
	        	leftText.setText("Your Health: " + GameState.health + "\n");
	        	rightText.setText("Zombie Health : " + dcd.count );
	        	

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
	        		break;
	        	}

	        	// Step 3: Build the alert dialog
	        	alertDialogBuilder.setTitle("Fight or Run? (Health Lost = Weapons - Zombie Health)");
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Let's fight";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Let's fight");
	    				//DevCardDetails dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
	    				removeDialog(DIALOG_ASK_FOR_FIGHT_ID);
	    				stateMachine(GameStateApp.PREPARE_FOR_FIGHT);
	    			}
	    		});

	    		NoString = "Run away";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				Log.d(GlobalPreferences.TAG, "I am running away .. I am a wuss");
	    				removeDialog(DIALOG_ASK_FOR_FIGHT_ID);
	    				
	    				runawayUse = null;
	    				runawayUse = checkUsage(0, GameStateApp.RUN_AWAY);
	    				if(runawayUse != null) {
	    					clickedItem = 0;
	    				} else { 
	    					runawayUse = checkUsage(1, GameStateApp.RUN_AWAY);
	    					clickedItem = 1;
	    				}
	    				
	    				if(runawayUse == null) {
	    				
		    				// Note: We loose a health point for running away.
		    				setHealth(GameState.health - 1);
		    				moveToPreviousRoom();
		    				showNormalActiveMapTile();
		    				stateMachine(GameStateApp.READY_FOR_NEW_TILE);
	    				} else {
	    					showDialog(DIALOG_RUNAWAY);
	    				}
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	    		return alertDialogBuilder.create();
	        }	
	        case DIALOG_RUNAWAY:
	        {
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
                        (ViewGroup) findViewById(R.id.layout_root), false);

	        	// Step 1: Item shown in the image
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	        	Item item = GameState.items.get(clickedItem);
	        	if(item.bitmap == null) {
	        		item.bitmap = BitmapFactory.decodeFile(item.image);
	        	}
	        	image.setImageBitmap(item.bitmap);
	        	
	        	// Step 2: Build the alert dialog
	        	alertDialogBuilder.setTitle("Use Item while running?");
	        	alertDialogBuilder.setView(layout);
	        	
	        	YesString = "Use Item";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				Log.d(GlobalPreferences.TAG, "Use Item");
	    				
	    				removeDialog(DIALOG_RUNAWAY);

	    				// Add Health if possible
	    				setHealth(GameState.health - 1 + useItem(clickedItem, runawayUse, GameState.state));	    				
	    				
	    				// Move back to older room, without loosing hitpoints
	    				moveToPreviousRoom();
	    				showNormalActiveMapTile();
	    				stateMachine(GameStateApp.READY_FOR_NEW_TILE);
	    				
	    				// Remove the dialog
	    				removeDialog(DIALOG_RUNAWAY);
	    			}
	    		});

	    		NoString = "Run away";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				Log.d(GlobalPreferences.TAG, "No item used for running away");
	    				removeDialog(DIALOG_RUNAWAY);
	    				
	    				// Note: We loose a health point for running away.
	    				setHealth(GameState.health - 1);
	    				moveToPreviousRoom();
	    				showNormalActiveMapTile();
	    				stateMachine(GameStateApp.READY_FOR_NEW_TILE);
	    			}
	    		});
	    		alertDialogBuilder.setCancelable(true);
	    		return alertDialogBuilder.create();
	        	
	        }
	        case DIALOG_ASK_FOR_OBJECTIVE_ID:
	        	View layout = inflater.inflate(R.layout.play_dlg_standard,
                        (ViewGroup) findViewById(R.id.layout_root), false);
	        	ImageView image = (ImageView)layout.findViewById(R.id.image);
	        	TextView leftText   = (TextView) layout.findViewById(R.id.leftText);

	        	leftText.setText(GameState.activeObjective.PreActionText);
	        	image.setImageResource(R.drawable.objectivepretext);
	        	
	        	alertDialogBuilder.setTitle("Objective Completion Possibility (May cost turn or object)");
	        	alertDialogBuilder.setView(layout);

	        	YesString = "Yes I want to";
	        	alertDialogBuilder.setPositiveButton(YesString, new OnClickListener() {

	    			@Override
	    			public void onClick(DialogInterface dialog, int which) {
	    				removeDialog(DIALOG_ASK_FOR_OBJECTIVE_ID);
	    				DevCards itemDevCard = nextDevCard();
	    	    		if(itemDevCard == null)	// Game Over 
	    	    			return;

	    	    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(itemDevCard.image));
	    				stateMachine(GameStateApp.OBJECTIVE_PROCESS);
	    			}
	    		});

	    		NoString = "No I don't want to";
	    		alertDialogBuilder.setNegativeButton(NoString, new OnClickListener() {
	    			public void onClick(DialogInterface dialog, int arg1) {
	    				stateMachine(GameStateApp.OBJECTIVE_IGNORE);
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

    
    /*
     * Depending on state check whether this item is usable
     * Dependent items are not returned here
     */
    public ItemUse checkUsage(int idx, int gameState) {
    	
    	// Check if item is within bounds
    	if(idx >= GameState.items.size())
    		return null;

    	// Step 1: Get corresponding item
    	Item item = GameState.items.get(idx);
    	Item otherItem = null;
    	ItemUse use = null;
    	int otherIdx = ((idx+1)%2);
    	
    	// Check if item is within bounds
    	if(otherIdx < GameState.items.size())
    		otherItem = GameState.items.get(otherIdx);
    	
    	
    	switch(gameState) {
    		case GameStateApp.LETS_FIGHT:
    		case GameStateApp.PREPARE_FOR_FIGHT:
    		{
    	    	if((item.limitedUse == true) && (item.noOfUsesLeft == 0))
    	    		return null;
    	    	for(int i = 0; i < item.uses.size(); i++) {
    	    		use = item.uses.get(i);
    	    		if(use.dependentItem == true)
    	    			continue;
    	    		if((use.useType.compareTo("attack") == 0) && (use.type.compareTo("single") == 0)) 
    	    			return use;
    	    		if(use.type.compareTo("combine") == 0) {
    	    			if(otherItem == null)
    	    				continue;
    	    			if((otherItem != null) 
    	    					&& (use.combineItem.compareTo(otherItem.name) == 0) 
    	    					&& (use.useType.compareTo("attack") == 0)) 
    	    				return use;
    	    		} 
    	    	}
    			break;
    			
    		}
	    	case GameStateApp.RUN_AWAY:
	    	{
    	    	if((item.limitedUse == true) && (item.noOfUsesLeft == 0))
    	    		return null;
	        	for(int i = 0; i < item.uses.size(); i++) {
	        		use = item.uses.get(i);
	        		if(use.useType.compareTo("runaway") == 0) {
	        			runawayUse = use;
	        			return use;
	        		}
	        	}
	    		
	    		break;
	    	}
	    	case GameStateApp.READY_FOR_NEW_TILE:
	    	default:
	    	{
    	    	for(int i = 0; i < item.uses.size(); i++) {
    	    		use = item.uses.get(i);
    	    		if(use.useType.compareTo("attack") == 0) 
    	    			continue;
    	    		    	    		
    	    		if(otherItem != null) {
    	    			if((use.type.compareTo("combine") == 0) 
    	    					&& (use.combineItem.compareTo(otherItem.name) == 0) 
    	    					&& (use.useType.compareTo("enhance") == 0)) 
    	    				return use;
    	    		}
    	    		if((use.useType.compareTo("health") == 0) && (use.type.compareTo("single") == 0)) {
    	    			return use;
    	    		}
    	    	}
	    		break;
	    	}
    	}
		return null;
    	
    }
    
   
    /*
     * Use a particular item, depending on stateMachine
     * Step 1: Find the increase in health
     * Step 2: If combination, possible adjust noUsesLeft
     * Step 3: Decrease noUsesLeft
     */
    public int useItem(int itemNo, ItemUse itemUse, int gameState) {
    	
    	Item item = GameState.items.get(itemNo);
    	int health = 0;
    	Item combinedItem = null;
    	ItemUse combinedUse = null;
    	
    	switch(gameState) {
	    	case GameStateApp.LETS_FIGHT:
    		case GameStateApp.PREPARE_FOR_FIGHT:
	    	{
	    		if((itemUse.type.compareTo("single") == 0) && (itemUse.useType.compareTo("attack") == 0)) {
	    			health = itemUse.attack;
	    		} else if((itemUse.type.compareTo("combine") == 0) && (itemUse.useType.compareTo("attack") == 0)) {
	    			health = itemUse.attack;
	    		}
	    		break;
	    	}
	    	case GameStateApp.RUN_AWAY:
	    	{
	    		if(itemUse.type.compareTo("attack") == 0) {
	    			health = itemUse.attack;
	    		} else if(itemUse.type.compareTo("health") == 0) {
	    			health = itemUse.health;
	    		} else if(itemUse.type.compareTo("combine") == 0) {
	    			health = itemUse.attack + itemUse.health;
	    		}
	    		break;
	    	}
	    	case GameStateApp.READY_FOR_NEW_TILE:
	    	default:
	    	{
	    		if((itemUse.useType.compareTo("health") == 0) && (itemUse.type.compareTo("single") == 0)) {
	    			health = itemUse.health;
	    		} else if(itemUse.type.compareTo("combine") == 0) {
	    			if(itemUse.dependentItem == true) {
	    				// What is the other item?
	    	    		if(itemNo == 0) combinedItem = GameState.items.get(1);
	    	    		else if(itemNo == 1) combinedItem = GameState.items.get(0);
	    	    		
	    	    		for(int i = 0; i < combinedItem.uses.size(); i++) {
	    	    			if(item.name.compareTo(combinedItem.uses.get(i).combineItem) == 0) {
	    	    				combinedUse = combinedItem.uses.get(i);
	    	    				if(combinedUse.useType.compareTo("enhance") == 0) {
	    	    					combinedItem.noOfUsesLeft = combinedItem.noOfUsesLeft + itemUse.noOfUsesLeft;
	    	    				}
	    	    			}
	    	    		}
	    	    		
	    			} else { // if not dependentItem
	    				int combinedNo = 2;
	    	    		if(itemNo == 0) { 
	    	    			combinedItem = GameState.items.get(1);
	    	    			combinedNo   = 1;
	    	    		} else if(itemNo == 1) {
	    	    			combinedItem = GameState.items.get(0);
	    	    			combinedNo = 0;
	    	    		}
	    	    		
	    	    		for(int i = 0; i < combinedItem.uses.size(); i++) {
	    	    			if(item.name.compareTo(combinedItem.uses.get(i).combineItem) == 0) {
	    	    				combinedUse = combinedItem.uses.get(i);
	    	    				if(itemUse.useType.compareTo("enhance") == 0) {
	    	    					
	    	    					// Finished adjusting the use of current item
    	    						item.noOfUsesLeft = combinedUse.noOfUsesLeft + item.noOfUsesLeft;
    	    						
    	    						// Further noOfUses adjustments should happen on dependent item
    	    						item = combinedItem;
    	    						itemNo = combinedNo;
    	    						
	    	    				} // if(itemUse.useType.compareTo("enhance") == 0)
	    	    			} // if(item.name.compareTo(combinedItem.uses.get(i).combineItem) == 0)
	    	    		} // for(int i = 0; i < combinedItem.uses.size(); i++)	    	    		
	    				
	    			}
	    		}
	    		break;
	    	}	    	
	    }    	
    	
    	// Adjust the usage
    	if(item.limitedUse == true) {
    		if(item.noOfUsesLeft == 9999) {
    			discardItem(itemNo);
    		} else {
        		item.noOfUsesLeft--;        		
    		}
    	}
    	
		return health;    	
    }
    
    
    /*
     * Discard item from the item array
     */
    public void discardItem(int index) {
    		Log.d(GlobalPreferences.TAG, "User discards " + GameState.items.get(index).name);
    		GameState.items.remove(index);
    		showHand(0);
    		showHand(1);
    		removeDialog(DIALOG_GOT_ITEM);
   	}
    
    /*
     * OnClickListener for Image Discard 
     */
    private View.OnClickListener itemDiscardClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			
			switch(view.getId()) {
			case R.id.dlitem1image:
				discardItem(0);
				break;
			case R.id.dlitem2image:
				discardItem(1);
				break;
			case R.id.dlitem3image:
				discardItem(2);
				break;
			}
			removeDialog(DIALOG_CHOOSE_ITEM_ID);
			dialogGotItem = null;
		}
    };

    /*
     * OnClickListener for Image Selection for Fight
     */
    private View.OnClickListener itemFightClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			
			switch(view.getId()) {
			case R.id.dlitem1image:
				clickedItem = 0;
				GameState.attack = useItem(clickedItem, use0, GameStateApp.LETS_FIGHT);
				break;
			case R.id.dlitem2image:
				clickedItem = 1;
				GameState.attack = useItem(clickedItem, use1, GameStateApp.LETS_FIGHT);
				break;
			}
			
			stateMachine(GameStateApp.LETS_FIGHT);
			removeDialog(DIALOG_CHOOSE_ITEM_ID);
		}
    };
    /*
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
						boolean usePossible = true;
						
						// Combined items require special check regarding combination
						if(use.combineItem != null) {
							usePossible = false;
							for(int k = 0; k < GameState.items.size(); k++) {
								if(use.combineItem.compareTo(GameState.items.get(k).name) == 0) {
									usePossible = true;
								}
							}							
						}
						if(usePossible == true) {
							possibleItems.add(item);
							break;
						}
					}
				}
			}
			itemsGallery.setAdapter(new ImageAdapter(this, possibleItems));
			itemsGallery.setSelection(0);
        case DIALOG_ASK_FOR_ITEM_ID:
        	break;
        case DIALOG_ASK_FOR_FIGHT_ID:
        	break;
        default:
    		break;
        }
    }
    
    */
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make Window Fullscreen as it is a game
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		GameState = (GameStateApp)getApplication();
		
		// Initialise Random Number Generator
    	rand = new Random();

    	// Get game rules 
    	Bundle extras = getIntent().getExtras();
    	if(extras != null) {
    		GameState.rulesJsonPath = extras.getString(GlobalPreferences.KEY_GAME_RULES);
    	} else {
    		GameState.rulesJsonPath = globalPreferences.getDefaultRules();
    	}

    	// Parse game rules 
		setContentView(R.layout.play_relative);

		layoutInflater = LayoutInflater.from(this);	
    }

	@Override
    public void onResume() {
    	super.onResume();
    	// Reinitialise the handlers
    	alwaysReInitialiseGame();
    }

	
	
    @Override
    public void onStart() {
    	super.onStart();

    	if(GameState.state == GameStateApp.DEAD) {
    		freshInitialiseGame();
        	alwaysReInitialiseGame();
    		stateMachine(GameStateApp.READY_FOR_NEW_TILE);
    		showNormalActiveMapTile();
    		showDialog(DIALOG_OBJECTIVES);
    	} else { 
        	alwaysReInitialiseGame();
    		showNormalActiveMapTile();
    	}

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
    public void onDestroy() {
    	super.onDestroy();
    	
    }

    private View.OnClickListener restClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			showDialog(DIALOG_REST);			
		}
    };
    
    private View.OnClickListener exitClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			processMovement(view);			
		}
    };
    	

    private View.OnClickListener rotateOkClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			showNormalActiveMapTile();
			linkrooms(GameState.prevMapTile, GameState.activeMapTile, (GameState.playerEntry+2)%4, GameState.playerEntry);
			stateMachine(GameStateApp.NEW_TILE);
		}
    };

    private View.OnClickListener rotateClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			
			// Rotate and re-render
			rotateToNextAlignedRoom(GameState.playerEntry);			
			// State Change: PLACE TILE
			stateMachine(GameStateApp.PLACE_TILE);
			showTile();
			showPlayer(GameState.playerEntry);
		}
    };

    private View.OnClickListener itemClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if(view.getId() == R.id.itemtwo) {
				clickedItem = 1;
			} else {
				clickedItem = 0;
			}			
			showDialog(DIALOG_SHOW_ITEM);			
		}
    };
	
    private View.OnClickListener suicideClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			showDialog(DIALOG_SUICIDE);			
		}
    };
    private View.OnClickListener objectivesClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			showDialog(DIALOG_OBJECTIVES);			
		}
    };
    private View.OnClickListener mapClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent mapViewIntent = new Intent(GameActivity.this, MapView.class);
			startActivity(mapViewIntent);
		}
    };
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
    /* Room Manipulation
     * Move back to previous Room
     */
    protected void moveToPreviousRoom() {
    	GameState.activeMapTile.setActive(false);
    	GameState.activeMapTile = GameState.prevMapTile;
    	GameState.prevMapTile.setActive(true);
    	
	}

    /*
     * Rotate the room to align with entry point
     */
    protected void rotateToNextAlignedRoom(int exit) {
    	
    	int rotation = GameState.activeMapTile.getRotation();
    	for(int i=1; i<5; i++) {
    		if(GameState.activeMapTile.exits[GameState.activeMapTile.getExitIndex(i+rotation, exit)] == MapTiles.STANDARD_EXIT) {
    			// Found the next valid exit after rotating clockwise
    			GameState.activeMapTile.setRotation(rotation + i);
    			return;
    		}
    	}
    }
    
    /*
     * Rotate to align properly to special exit
     */
    protected void rotateSpecialToAlign(int exit) {
    	
    	int rotation = GameState.activeMapTile.getRotation();
    	for(int i=1; i<5; i++) {
    		if(GameState.activeMapTile.exits[GameState.activeMapTile.getExitIndex(i+rotation, exit)] == MapTiles.SPECIAL_EXIT) {
    			// Found the next valid exit after rotating clockwise
    			GameState.activeMapTile.setRotation(rotation + i);
    			return;
    		}
    	}
    }    
    
    // Move the game state to show the next tile
	protected void processMovement(View view) {
		
		int id = 0;
		MapTiles tile = GameState.activeMapTile;
		int exitType = 0;
		int newTileLocation = -1;
		
		// If not ready for current movement then don't process
		if(GameState.state != GameStateApp.READY_FOR_NEW_TILE) {
			Log.d(GlobalPreferences.TAG, "Pressing it  on the same tile ignore it or not yet ready to open a new tile ignore it");
			Toast.makeText(getApplicationContext(), "Cannot move out of room now. Complete current action by pressing OK", Toast.LENGTH_LONG).show();
			return;
		}
		
		int playerExit = 0;
		// locate the exit
		switch(id = view.getId()) {
		case R.id.bottomExit:
			exitType = tile.getBottomExit();
			GameState.playerEntry = EXIT_TOP;
			playerExit = EXIT_BOTTOM;
			newTileLocation = GameStateApp.activeMapTileLoc + GameStateApp.gridX;
			Log.d(GlobalPreferences.TAG, "Bottom Exit clicked :" + id );
			break;
		case R.id.topExit:
			exitType = tile.getUpExit();
			GameState.playerEntry = EXIT_BOTTOM;
			playerExit = EXIT_TOP;
			newTileLocation = GameStateApp.activeMapTileLoc - GameStateApp.gridX;
			Log.d(GlobalPreferences.TAG, "Top Exit clicked :" + id );
			break;
		case R.id.leftExit:
			exitType = tile.getLeftExit();
			GameState.playerEntry = EXIT_RIGHT;
			playerExit = EXIT_LEFT;
			newTileLocation = GameStateApp.activeMapTileLoc - 1;
			Log.d(GlobalPreferences.TAG, "Left Exit clicked :" + id );
			break;
		case R.id.rightExit:
			exitType = tile.getRightExit();
			GameState.playerEntry = EXIT_LEFT;
			playerExit = EXIT_RIGHT;
			newTileLocation = GameStateApp.activeMapTileLoc + 1;
			Log.d(GlobalPreferences.TAG, "Right Exit clicked :" + id );
			break;
		default:
			Log.e(GlobalPreferences.TAG, "Unknown Exit clicked :" + id );
		}
		
		// Case 1: Error condition of no exit being present, the icon for click should not have been visible here
		if(exitType == MapTiles.NO_EXIT) {
			showDialog(DIALOG_NOWAYOUT);
			Log.e(GlobalPreferences.TAG, "Trying to go to wrong exit - No exit here to Room : " + tile.getName() + " Exit :" + id );
			Toast.makeText(this, "No way out here, its blocked!", Toast.LENGTH_LONG).show();
			return;
			
		// Case 2: Standard case for moving to a new room
		} else if (exitType == MapTiles.STANDARD_EXIT) { // Standard case of new room
			if(GameStateApp.tiles.get(newTileLocation) != null) {
				//TODO : Should we check if that other room has a door in this exact position If so then we could go in that room
				Log.e(GlobalPreferences.TAG, "Can not go towards this exit there is already some other room there" + GameStateApp.tiles.get(newTileLocation).getName());
				Toast.makeText(this,  "Can not go towards this exit there is already some other room there" + GameStateApp.tiles.get(newTileLocation).getName(), Toast.LENGTH_LONG).show();
				return;
			}
			
			if(GameState.activeMapTile.getArea().compareTo("Inside") == 0) {
				if(!GameState.indoorStack.isEmpty()) {
					
					// Introduce a new Active tile
					GameState.activeMapTile.setActive(false);
					GameState.prevMapTile = GameState.activeMapTile;
					GameState.activeMapTile = (MapTiles) GameState.indoorStack.pop();
					GameState.activeMapTile.setActive(true);
				} else {
					showDialog(DIALOG_NOWAYOUT);
					Toast.makeText(this, "No way out. Try to change the area using area transition space or complete objective!", Toast.LENGTH_LONG).show();
					Log.d(GlobalPreferences.TAG, "All of indoor tiles used up");
			        return;
				}
			} else {
				if(!GameState.outdoorStack.isEmpty()) {
					GameState.activeMapTile.setActive(false);
					GameState.prevMapTile = GameState.activeMapTile;
					GameState.activeMapTile = (MapTiles) GameState.outdoorStack.pop();
					GameState.activeMapTile.setActive(true);
				} else {
					showDialog(DIALOG_NOWAYOUT);
					Toast.makeText(this, "No way out. Try to change the area using area transition space or complete objective!", Toast.LENGTH_LONG).show();
					Log.d(GlobalPreferences.TAG, "All of outdoor tiles used up");
					return;
				}
				
			}
			// Rotate Tile at the right angle and show
			rotateToNextAlignedRoom(GameState.playerEntry);
			stateMachine(GameStateApp.PLACE_TILE);
			showTile();
			showPlayer(GameState.playerEntry);
			
		// Case 3: Connected Exit to older room
		} else if (exitType == MapTiles.CONNECTED_EXIT) {
			GameState.prevMapTile = GameState.activeMapTile;
			MapTiles connectedRoom = tile.getConnection(playerExit);
			GameState.activeMapTile = connectedRoom;
			
			showTile();
			showPlayer(EXIT_CENTER);
			showExits(GameState.activeMapTile);
			stateMachine(GameStateApp.OLD_TILE);
			
			
		// Case 4: Area Transition
		} else if (exitType == MapTiles.SPECIAL_EXIT) {
			/** Moving from inside to outside*/
			GameState.activeMapTile.setActive(false);
			GameState.prevMapTile = GameState.activeMapTile; 
			GameState.activeMapTile = (MapTiles) GameState.addOnTiles.get(GameState.activeMapTile.getAddOnTileName());
			GameState.activeMapTile.setActive(true);
			
			// Rotate the tile around
			rotateSpecialToAlign(GameState.playerEntry);
			
			// It only fits in one position, hence there is no need to turn.
			showNormalActiveMapTile();
			linkrooms(GameState.prevMapTile, GameState.activeMapTile, (GameState.playerEntry+2)%4, GameState.playerEntry);
			stateMachine(GameStateApp.NEW_TILE);

		} else {
			// No way out of here
			showDialog(DIALOG_NOWAYOUT);
			Log.e(GlobalPreferences.TAG, "Trying to go to wrong exit - Wrong ExitType : " + exitType + tile.getName() + " Exit :" + id );
			Toast.makeText(this, "No way out here, its blocked!", Toast.LENGTH_LONG).show();
			return;
		}		
	}

	// Connect the rooms exit to entry of the next room
    void linkrooms(MapTiles prevTile, MapTiles nextTile, int prevExit, int nextEntry) {
    	
    	int newTileLocation = -1;
    	switch(prevExit) {
    	//switch(prevTile.getExitIndex(prevTile.getRotation(), prevExit)) {
    	case 0:
			newTileLocation = GameStateApp.activeMapTileLoc - GameStateApp.gridX;
    		break;
    	case 2:
    		newTileLocation = GameStateApp.activeMapTileLoc + GameStateApp.gridX;
    		break;
    	case 1:
			newTileLocation = GameStateApp.activeMapTileLoc - 1;
    		break;
    	case 3:
			newTileLocation = GameStateApp.activeMapTileLoc + 1;
    		break;
    	}
    	
    	Log.e("asd", "current Tile location is " + GameStateApp.activeMapTileLoc);
    	Log.e("asd", "New tile location is " + newTileLocation);
    	GameStateApp.activeMapTileLoc = newTileLocation;
    	GameStateApp.tiles.set(newTileLocation, nextTile);
    	
    	adjustGridIfNeeded();
    	
/*    	for(int i = 0; i < GameState.gridY; i++) {
    		for(int j = 0; j < GameState.gridX ; j++) {
    			MapTiles tile = GameState.tiles.get(i*GameState.gridX + j);
    			if(tile != null) {
        			Log.e("asd", " Map i[" + i + "]  j[" + j + "] tile name >" + tile.getName());
    			} else {
        			Log.e("asd", " Map i[" + i + "]  j[" + j + "] empty");
    			}
    		}
    	}*/
    	
    	// Link previous tile to next
    	prevTile.exits[prevTile.getExitIndex(prevTile.getRotation(), prevExit)] = MapTiles.CONNECTED_EXIT;
    	prevTile.exitConnectedTo[prevTile.getExitIndex(prevTile.getRotation(), prevExit)] = nextTile;
    	
    	nextTile.exits[nextTile.getExitIndex(nextTile.getRotation(), nextEntry)] = MapTiles.CONNECTED_EXIT;
    	nextTile.exitConnectedTo[nextTile.getExitIndex(nextTile.getRotation(), nextEntry)] = prevTile;
    	
    }
	
	/** This function is called when u try to open a door next to current active door. This function decides if that door is openable 
	 * and if it is openable then it find the new attach direction of the new tile 
	 * @param currMapTileLoc - Current Map Tile location
	 * @param currMapTile - Current Active Map Tile
	 * @param currClickLoc - Where exactly was it clicked
	 * @return returns attach direction for new tile 0-south, 1-west, 2-north, 3 east
	 */
	public int findIfTileOpenableReturnAttachDirection(int currClickLoc) {
		int activeMapTileY = (int)GameStateApp.activeMapTileLoc/GameStateApp.gridX;
		int activeMapTileX = (int)GameStateApp.activeMapTileLoc%GameStateApp.gridX;
		int currClickY = (int)currClickLoc/GameStateApp.gridX;
		int currClickX = (int)currClickLoc%GameStateApp.gridX;
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
		int tileY = GameStateApp.activeMapTileLoc/GameStateApp.gridX;
		int tileX = GameStateApp.activeMapTileLoc%GameStateApp.gridX;
		int i = 0;
		//Log.d(TAG, "before update" + GameState.tiles.toString());

		/** There can not be a time when both new row as well as new column needs to be added*/
		if(tileY == 0) {
			//Log.e("asd", "We need to add 1 extra top row");
			/** We need to add 1 extra top row*/
			GameStateApp.gridY++;
			for(i = 0; i < GameStateApp.gridX; i++) {
				GameStateApp.tiles.add(0, null);
			}
			GameStateApp.activeMapTileLoc = GameStateApp.activeMapTileLoc + GameStateApp.gridX;
		} else if(tileY == GameStateApp.gridY - 1) {
			//Log.e("asd", "We need to add 1 extra bottom row");
			/** We need to add 1 extra bottom row*/
			for(i = 0; i < GameStateApp.gridX; i++) {
				GameStateApp.tiles.add(null);
			}
			GameStateApp.gridY++;
		} else if(tileX == 0) {
			//Log.e("asd", "We need to add 1 extra left column");
			/** We need to add 1 extra left column*/
			GameStateApp.gridX++;
			for(i = 0; i < GameStateApp.gridY; i++) {
				GameStateApp.tiles.add(i*GameStateApp.gridX, null);
			}
			GameStateApp.activeMapTileLoc = GameStateApp.activeMapTileLoc + tileY + 1;
		} else if(tileX == GameStateApp.gridX - 1) {
			//Log.e("asd", "We need to add 1 extra right column");
			/** We need to add 1 extra right coloumn*/
			for(i = 0; i < GameStateApp.gridY; i++) {
				GameStateApp.tiles.add((i + 1)*GameStateApp.gridX + i, null);
			}
			GameStateApp.gridX++;
			GameStateApp.activeMapTileLoc = GameStateApp.activeMapTileLoc + tileY;
		} else {
			return;
		}
		
		/*for(int k = 0; k < GameState.gridY; k++) {
    		for(int j = 0; j < GameState.gridX ; j++) {
    			MapTiles tile = GameState.tiles.get(k*GameState.gridX + j);
    			if(tile != null) {
        			Log.e("asd", " adjusted Map i[" + k + "]  j[" + j + "] tile name >" + tile.getName());
    			} else {
        			Log.e("asd", " adjusted Map i[" + k + "]  j[" + j + "] empty");
    			}
    		}
    	}*/

		//Log.d(TAG, "after update " + GameState.tiles.toString());
		//map.setLayoutParams(new RelativeLayout.LayoutParams(198*GameState.gridX, 198*GameState.gridY));
		//map.setNumColumns(GameState.gridX);
	}
	
	/** Get the next Dev Card*/
    public DevCards nextDevCard() {
    	boolean gameOnGoing = true; 

    	if(GameState.devCardStack.isEmpty()) {
    		gameOnGoing = TimePasses();
    		if(gameOnGoing == false) {
    			return null;
    		}
    			
    	}
    	// This accounts to loss of one turn
    	GameState.turn--;
    	showTimeBar();
    	return (DevCards) GameState.devCardStack.pop();
    }

    /*
     * Move from one hour to the next, and replenish devcards
     */
    public boolean TimePasses() {
    	GameState.time++;
    	if(GameState.time < gameRules.getEndTime().hour) {
    		Toast.makeText(getApplicationContext(), "Its " + GameState.time + " already", Toast.LENGTH_LONG).show();
    		stackDevCards();
    		adjustDiffculty(GameState.difficulty);
    		return true;
    	} else {
    		GameOver();
    		return false;
    	}
    }

    /*
     * Drop Dev cards depending on difficulty
     * 0 - Easy   (27 turns)
     * 2 - Normal (21 turns)
     * 3 - Hard   (18 turns)
     */
    public void adjustDiffculty(int difficulty) {
    	if(difficulty == 1) {
    		GameState.devCardStack.pop();
    		GameState.devCardStack.pop();
    	} else if(difficulty == 2) {
    		GameState.devCardStack.pop();
    		GameState.devCardStack.pop();
    		GameState.devCardStack.pop();
    	}
    }
    
    /*
     * Game Over due to
     * 1. Suicide
     * 2. Time Over 
     * 3. Death - Health < 0
     */
    public void GameOver() {
    	showDialog(DIALOG_GAMEOVER);
		Toast.makeText(getApplicationContext(), "Game Over!!", Toast.LENGTH_LONG).show();
		GameState.state = GameStateApp.DEAD;
		gameRules = null;
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
    	
		showDialog(DIALOG_OBJECTIVE_COMPLETE);
    	
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
    
    // FIXME: Crashes sometimes here on beginning of for loop with nullpointer exception
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
    			boolean objectivesOk = true;
    			for(int j = 0; j < obj.NeededObjectives.size() ; j++) {
    				if(gameRules.ObjectivesList.get(obj.NeededObjectives.get(j)).ACHIEVED == false) {
    					/** one of these objective are not yet compete*/
    					Log.d(GlobalPreferences.TAG, "one of these objective are not yet compete" + obj.NeededObjectives.get(j));
    					objectivesOk = false;        				
    					break;
    				}
    			} // end for NeedObjectives.size()
    			if(objectivesOk == false)
    				continue;
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
    	case GameStateApp.READY_FOR_NEW_TILE:
    	{
    		MapTiles maptile = GameState.activeMapTile;
    		if(maptile.isFreeItem() == true) {
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    		} else if(maptile.isHealthItem() == true) {
    			setHealth(GameState.health + maptile.getHealthChange());
    			Toast.makeText(getApplicationContext(), maptile.getHealthText(), Toast.LENGTH_LONG).show();
    		}
    		break;
    	}
    	case GameStateApp.VICTORY:
			Toast.makeText(getApplicationContext(), "You have won the game" , Toast.LENGTH_LONG).show();
    		break;
    	case GameStateApp.OBJECTIVE_COMPLETE:
    		if(objectivesComplete() == true) {
    			stateMachine(GameStateApp.VICTORY);
    			showDialog(DIALOG_VICTORY);
    		} else {
    			stateMachine(GameStateApp.READY_FOR_NEW_TILE);
    		}
    		break;
    	case GameStateApp.OBJECTIVE_VERIFY:
    		if(verifyObjectives() == true) {
    			showDialog(DIALOG_ASK_FOR_OBJECTIVE_ID);
    		} else {
    			stateMachine(GameStateApp.READY_FOR_NEW_TILE);
    		}
    		break;
    	case GameStateApp.OBJECTIVE_IGNORE:
    		GameState.activeObjective = null;
    		stateMachine(GameStateApp.READY_FOR_NEW_TILE);
    		break;
    	case GameStateApp.OBJECTIVE_PROCESS: 
    		GameState.activeDevCard = nextDevCard();
    		if(GameState.activeDevCard == null)	// Game Over 
    			return;
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));

    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			setHealth(GameState.health + dcd.count);
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_COMPLETE);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameStateApp.PLACE_TILE:
    		break;
    	case GameStateApp.NEW_TILE: 
    		GameState.activeDevCard = nextDevCard();
    		if(GameState.activeDevCard == null)	// Game Over 
    			return;
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));
    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			setHealth(GameState.health + dcd.count);
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameStateApp.OLD_TILE: 
    		GameState.activeDevCard = nextDevCard();
    		if(GameState.activeDevCard == null)	// Game Over 
    			return;
    		dcd = GameState.activeDevCard.devCardDetails.get(GameState.time + "PM");
    		devCardImage.setImageBitmap(BitmapFactory.decodeFile(GameState.activeDevCard.image));
    		switch(dcd.type) {
    		case DevCardDetails.PLAIN_DEVCARD:
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.HEALTH_DEVCARD:
    			setHealth(GameState.health + dcd.count);
    			Toast.makeText(getApplicationContext(), dcd.message, Toast.LENGTH_LONG).show();
    			stateMachine(GameStateApp.OBJECTIVE_VERIFY);
    			break;
    		case DevCardDetails.ITEM_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_ITEM_ID);
    			break;
    		case DevCardDetails.ZOMBIE_DEVCARD:
    			showDialog(DIALOG_ASK_FOR_FIGHT_ID);
    			break;
    		}
    		break;
    	case GameStateApp.PREPARE_FOR_FIGHT:
			boolean usePossible = false;

        	if(!GameState.items.isEmpty()) {
        		use0 = checkUsage(0, GameState.state);
        		use1 = checkUsage(1, GameState.state);
        		if((use0 != null) || (use1 != null)) {
        			usePossible = true;
        		}
        	}

    		if(usePossible == true) {
        		showDialog(DIALOG_CHOOSE_ITEM_ID);
        	} else {
        		GameState.attack = 1;
        		stateMachine(GameStateApp.LETS_FIGHT);
        	}
    		break;
    	case GameStateApp.LETS_FIGHT:
	    	showDialog(DIALOG_SHOW_FIGHT);
    		break;
    	case GameStateApp.DEAD :
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

	
    
    private void setupListeners() {
    	Screen.exitLeft.setOnClickListener(exitClick);
    	Screen.exitRight.setOnClickListener(exitClick);
    	Screen.exitTop.setOnClickListener(exitClick);
    	Screen.exitBottom.setOnClickListener(exitClick);
    	
    	Screen.rotateok.setOnClickListener(rotateOkClick);
    	Screen.rotate.setOnClickListener(rotateClick);
    	
    	Button restButton = (Button)findViewById(R.id.rest);
    	restButton.setOnClickListener(restClick);
    	
    	Button itemOne = (Button)findViewById(R.id.itemone);
    	Button itemTwo = (Button)findViewById(R.id.itemtwo);
    	
    	itemOne.setOnClickListener(itemClick);
    	itemTwo.setOnClickListener(itemClick);
    	
    	Button suicideButton = (Button)findViewById(R.id.suicide);
    	suicideButton.setOnClickListener(suicideClick);
    	
    	ImageButton objButton = (ImageButton)findViewById(R.id.objectives);
    	objButton.setOnClickListener(objectivesClick);
    	
    	ImageButton mapButton = (ImageButton)findViewById(R.id.map);
    	mapButton.setOnClickListener(mapClick);
	}

    /*
     * Show Item in Left or Right Hand
     */
	public void showHand(int itemIdx) {

		Button bt = null;
		
		if(itemIdx == 0) {
			bt = (Button)findViewById(R.id.itemone);			
		} else {
			bt = (Button)findViewById(R.id.itemtwo);			
		}
		if(itemIdx < GameState.items.size())
			bt.setText(GameState.items.get(itemIdx).name);
		else
			bt.setText("No Item");
	}
    
	/*
	 * Set Health 
	 * 1. Change Game Health
	 * 2. Update the UI
	 */
    public void setHealth(int health) {
    	GameState.health = health;
    	if(health > 10) GameState.health = 10;
    	if(health < 0)  GameState.health = 0;
    	
    	Screen.healthBar.setProgress(GameState.health);
        Screen.healthBar.setText(GameState.health + "/" + gameRules.getMaxHP());
        
        if(health <= 0) 
        	GameOver();
    }


	/* 
     * Sets up the UI for the map tile in normal mode
     * 1. Find the imageview
     * 2. Load & Rotate the bitmap, if not already loaded 
     * 3. Find out exits and mark them by hiding and unhiding widgets
     */
    private void showNormalActiveMapTile() {
    	
    	// Step 1: Find the imageview
    	MapTiles tile = GameState.activeMapTile;

    	// Step 2: Show Bitmap
    	showTile();
    	
    	// Step 3: Mark the exits by hiding and showing widgets
    	showExits(tile);
    	showPlayer(EXIT_CENTER);
    	
    }    

    /*
     * Hide all exits, good or bad
     */
    private void hideExits() {
    	Screen.exitLeft.setVisibility(View.INVISIBLE);
    	Screen.exitRight.setVisibility(View.INVISIBLE);
    	Screen.exitTop.setVisibility(View.INVISIBLE);
    	Screen.exitBottom.setVisibility(View.INVISIBLE);

    	Screen.noExitLeft.setVisibility(View.INVISIBLE);
    	Screen.noExitRight.setVisibility(View.INVISIBLE);
    	Screen.noExitTop.setVisibility(View.INVISIBLE);
    	Screen.noExitBottom.setVisibility(View.INVISIBLE);
    	
    }

    /*
     * Update the time bar with current time and turns
     */
	public void showTimeBar() {
        Screen.timeBar.setProgress(GameState.turn);
        Screen.timeBar.setText("Time:" + GameState.time + ":00 / " + GameState.turn + " turns to go");
	}
    /*
     * Shows the current tile in the map position
     * 1. Load the tile if not present
     * 2. Rotate if rotated
     * 3. Place onto imageView of maptile
     */
	private void showTile() {
    	MapTiles tile = GameState.activeMapTile;
		ImageView imageView = Screen.mapTile;
    	
    	// Step 2: Load the bitmap, if not already loaded
    	if(tile.bitmap == null) { 
    	
    		// Decode the bitmap
    		tile.bitmap = BitmapFactory.decodeFile(tile.getTexturePath());
	    	
			Log.d(GlobalPreferences.TAG, "Tile detected at " + GameStateApp.activeMapTileLoc 
					+ " displaying the image bitmap" + tile.getTexturePath());
			
			// If tile is rotated, rotate it
			if (tile.getRotation() > 0) {
	
				Matrix matrix = new Matrix();
				matrix.preRotate(90 * tile.getRotation());
				
				Bitmap rotatedBitmap = Bitmap.createBitmap(tile.bitmap, 0, 0, tile.bitmap.getWidth(), tile.bitmap.getHeight(), matrix, true);
				imageView.setImageBitmap(rotatedBitmap);
				// Free older bitmap
				tile.bitmap.recycle();
				tile.bitmap = rotatedBitmap;
				
			} else {
				// Step 3: Set the imageView
				imageView.setImageBitmap(tile.bitmap);					
			}		
    	} else { // (tile.bitmap == null)  Bitmap was p
			// Step 3: Set the imageView
    		Log.d(GlobalPreferences.TAG, tile.getName() + " : Setting old bitmap image - assuming rotation is proper ");
    		imageView.setImageBitmap(tile.bitmap);
    	}
		
	}

    // Shows the various exits from the current spot
    // 1. Shows the active go-able exits.
    // 2. Blocks the other exits. 
    public void showExits(MapTiles tile) {
    	
   		if(tile.getUpExit() == MapTiles.NO_EXIT) {
   			enableExit(EXIT_TOP, false);
   		} else {
   			enableExit(EXIT_TOP, true);
   		}
    	if(tile.getLeftExit() == MapTiles.NO_EXIT) {
    		enableExit(EXIT_LEFT, false);
    	} else {
    		enableExit(EXIT_LEFT, true);
    	}
    	if(tile.getBottomExit() == MapTiles.NO_EXIT) {
    		enableExit(EXIT_BOTTOM, false);
    	} else {
    		enableExit(EXIT_BOTTOM, true);
    	}
    	if(tile.getRightExit() == MapTiles.NO_EXIT) {
    		enableExit(EXIT_RIGHT, false);
    	} else {
    		enableExit(EXIT_RIGHT, true);
    	}

    }
    
    // 1. Shows where the player is in the room
    // 2. Makes invisible the other areas
    public void showPlayer(int exit) {
    	ImageButton playerCenter, playerRight, playerLeft, playerTop, playerBottom = null;
    	
    	playerCenter = (ImageButton)findViewById(R.id.playerCenter);
    	playerLeft   = (ImageButton)findViewById(R.id.playerLeft);
    	playerRight  = (ImageButton)findViewById(R.id.playerRight);
    	playerTop    = (ImageButton)findViewById(R.id.playerTop);
    	playerBottom = (ImageButton)findViewById(R.id.playerBottom);
    	
    	// Set all icons as invisible
    	playerCenter.setVisibility(View.INVISIBLE);
    	playerLeft.setVisibility(View.INVISIBLE);
    	playerRight.setVisibility(View.INVISIBLE);
    	playerTop.setVisibility(View.INVISIBLE);
    	playerBottom.setVisibility(View.INVISIBLE);
    	
    	// Show the visible icon as requested
    	switch(exit) {
    	case EXIT_CENTER:
    		showRotate(false);
    		break;
    	case EXIT_LEFT:
    		playerLeft.setVisibility(View.VISIBLE);
    		Screen.exitLeft.setVisibility(View.INVISIBLE);
    		Screen.noExitLeft.setVisibility(View.INVISIBLE);
    		showRotate(true);
    		break;
    	case EXIT_RIGHT:
    		playerRight.setVisibility(View.VISIBLE);
    		Screen.exitRight.setVisibility(View.INVISIBLE);
    		Screen.noExitRight.setVisibility(View.INVISIBLE);
    		showRotate(true);
    		break;
    	case EXIT_TOP:
    		playerTop.setVisibility(View.VISIBLE);
    		Screen.exitTop.setVisibility(View.INVISIBLE);
    		Screen.noExitTop.setVisibility(View.INVISIBLE);
    		showRotate(true);
    		break;
    	case EXIT_BOTTOM:
    		playerBottom.setVisibility(View.VISIBLE);
    		Screen.exitBottom.setVisibility(View.INVISIBLE);
    		Screen.noExitBottom.setVisibility(View.INVISIBLE);
    		showRotate(true);
    		break;    		
    	}
    	
    }

    // Show the rotate button & hide exits
    public void showRotate(boolean show) {
    	ImageButton playerCenter = (ImageButton)findViewById(R.id.playerCenter);
    	ImageButton rotateButton = (ImageButton)findViewById(R.id.rotateIcon);

    	if(show == true) { 
    		playerCenter.setVisibility(View.INVISIBLE);
    		rotateButton.setVisibility(View.VISIBLE);
    		Screen.rotateok.setVisibility(View.VISIBLE);
        	hideExits();
    	} else {
    		playerCenter.setVisibility(View.VISIBLE);
    		rotateButton.setVisibility(View.INVISIBLE);
    		Screen.rotateok.setVisibility(View.INVISIBLE);
    	}
    }
    
    
    // Enables a particular exit looking at current state
    public void enableExit(int exit, boolean enable) {
    	ImageButton ibExit = null;
    	ImageButton ibNoExit = null;
    	
    	switch(exit) {
    	case EXIT_TOP:
    		ibExit = (ImageButton)findViewById(R.id.topExit);
    		ibNoExit = (ImageButton)findViewById(R.id.topNoExit);
    		break;
    	case EXIT_BOTTOM:
    		ibExit = (ImageButton)findViewById(R.id.bottomExit);
    		ibNoExit = (ImageButton)findViewById(R.id.bottomNoExit);
    		break;
    	case EXIT_LEFT:
    		ibExit = (ImageButton)findViewById(R.id.leftExit);
    		ibNoExit = (ImageButton)findViewById(R.id.leftNoExit);
    		break;
    	case EXIT_RIGHT:
    		ibExit = (ImageButton)findViewById(R.id.rightExit);
    		ibNoExit = (ImageButton)findViewById(R.id.rightNoExit);
    		break;
    	}
    	
    	if(enable == true) {
    		ibExit.setVisibility(View.VISIBLE);
    		ibNoExit.setVisibility(View.INVISIBLE);
    	} else {
    		ibExit.setVisibility(View.INVISIBLE);
    		ibNoExit.setVisibility(View.VISIBLE);
    		
    	}
    	return;    	
    }
    
    
}
