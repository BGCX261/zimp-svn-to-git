package com.zimp.play;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import org.json.JSONException;

import com.zimp.GlobalPreferences;
import com.zimp.R;
import com.zimp.gamedata.DevCards;
import com.zimp.gamedata.DevCards.DevCardDetails;
import com.zimp.gamedata.GameRules;
import com.zimp.gamedata.MapTiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class TileView extends Activity {

	public static class GameState {
		public static int health = 6;
		public static int attack = 1;
		public static int time = 9;
		public static ArrayList<String> items;
	}
	
	boolean done = false;

	// Logging related
	private static final String TAG = "GameRules";
	public static DevCards activeDevCard;
	Stack<Object> devCardStack = new Stack<Object>();
	
	private static GlobalPreferences globalPreferences = GlobalPreferences
	.getGlobalPreferences();
	GameRules gameRules;

	ImageView tileImage, devCardImage, healthImage, timeImage, item1Image, item2Image;

	Random random = new Random();

	public void stackDevCards() {
		int totalDevCardsCnt = gameRules.DevCardsList.size(), currDevCardCnt = 0;
		int randomRoom;
		ArrayList<Object> DevCardsLocalList = (ArrayList<Object>) gameRules.DevCardsList.clone();
		
		while(currDevCardCnt < totalDevCardsCnt) {
			randomRoom = random.nextInt(totalDevCardsCnt);
			if(DevCardsLocalList.get(randomRoom) != null) {
				devCardStack.push(DevCardsLocalList.set(randomRoom, null));
				currDevCardCnt++;
			}
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
        
        setContentView(R.layout.tileview);

        try {
        	gameRules = new GameRules(globalPreferences.getDefaultRules());
		} catch (NullPointerException n) {
			Log.e(TAG, "Null Pointer Exception: " + n.getMessage());
		} catch (JSONException je) {
			Log.e(TAG, "JSONException :" + je.getMessage());
			je.printStackTrace();
		}

        tileImage = (ImageView) findViewById(R.id.tile);
        healthImage = (ImageView) findViewById(R.id.health);
        timeImage = (ImageView) findViewById(R.id.time);
        devCardImage = (ImageView) findViewById(R.id.devcard);
        item1Image = (ImageView) findViewById(R.id.itemone);
        item2Image = (ImageView) findViewById(R.id.itemtwo);
        
        stackDevCards();
       
    }
    
    /*
    public void AskForItem() {
    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
    	ad.setTitle("Item");
    	ad.setMessage("Do you wish to draw the next Dev Card? If you do the item shown in the next Dev Card will be offered to you!");

    	String YesString = "Yes I want the item";
    	ad.setPositiveButton(YesString, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d("asd", "Yes I choose to take the item");
				nextDevCard();
				done = true;
			}
		});

		String NoString = "No I want to save time";
		ad.setNegativeButton(NoString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				// do nothing
				Log.d("asd", "No I want to save the time");
				done = true;
			}
		});
    	ad.setCancelable(true);
    	ad.show();
    }
    
    public void GameOver() {
		Toast.makeText(getApplicationContext(), "Game Over!!", Toast.LENGTH_LONG).show();
    }
    
    public void TimePasses() {
    	GameState.time++;
    	if(GameState.time < 12) {
    		Toast.makeText(getApplicationContext(), "Its " + GameState.time + "PM already", Toast.LENGTH_LONG).show();
    		stackDevCards();
    	} else {
    		GameOver();
    	}
    }

    public void nextDevCardForItem() {
    	if(devCardStack.isEmpty()) {
    		TimePasses();
    	}
		activeDevCard = (DevCards) devCardStack.pop();
		devCardImage.setImageBitmap(activeDevCard.bitmap);
		Toast.makeText(getApplicationContext(), "You got " + activeDevCard.item, Toast.LENGTH_LONG).show();
    }
    
    public void FightZombies(DevCardDetails dcd) {
    	int undeadZombies = dcd.count - GameState.attack;
    	if(undeadZombies > 0) {
    		GameState.health = GameState.health - undeadZombies;
    	}
    	
    	if(GameState.health <= 0) {
    		GameOver();
    	}
    }
    
    public void AskForZombieFight(DevCardDetails dcd) {
    	Log.d("asd", "AskForZombieFight {");
    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
    	ad.setTitle("Zombie Fight");
    	ad.setMessage("Do you wish to fight " + dcd.count + " zombies or do you wish to run away?");

    	String YesString = "Lets fight";
    	ad.setPositiveButton(YesString, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("asd", "Lets fight");
				DevCardDetails dcd = activeDevCard.devCardDetails.get(GameState.time + "PM");
				FightZombies(dcd);
				done = true;
			}
		});

		String NoString = "Run away";
		ad.setNegativeButton(NoString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				Log.d("asd", "I am running away .. I am a wuss");
				done = true;
				GameState.health--;
			}
		});
    	ad.setCancelable(true);
    	ad.show();
    	Log.d("asd", "AskForZombieFight }");
    }
    
    public void nextDevCard() {
    	if(devCardStack.isEmpty()) {
    		TimePasses();
    	}
		activeDevCard = (DevCards) devCardStack.pop();
		devCardImage.setImageBitmap(activeDevCard.bitmap);
		DevCardDetails dcd = activeDevCard.devCardDetails.get(GameState.time + "PM");
		if(dcd.type == DevCardDetails.PLAIN_DEVCARD) {
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
    }
    
    public void applyActiveMapTile(MapTiles activeMapTile) {
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
    
    @Override
    public void onStart() {
    	super.onStart();
    	//Intent startingIntent = this.getIntent();
    	
    	/*if(startingIntent.hasExtra("tileName")) {
    		String tileName = startingIntent.getStringExtra("tileName");
    		for(int i=0; i < gameRules.arrMapTiles.length; i++) {
    			if(gameRules.arrMapTiles[i].getName().compareTo("tileName") == 0) {
					activeMapTile = (MapTiles)gameRules.arrMapTiles[i];
					break;
    			}
    		}
    	} else {
    		for(int i=0; i < gameRules.arrMapTiles.length; i++) {
    			if(gameRules.arrMapTiles[i].getArea().compareTo("Inside") == 0) {
    				if(gameRules.arrMapTiles[i].isStartupTile() == true) {
    					activeMapTile = (MapTiles)gameRules.arrMapTiles[i];
    					break;
    				}
    			}
    		}
    	}*/
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
/*    	if(ZimpAActivity.activeMapTile != null) {
    		applyActiveMapTile(ZimpAActivity.activeMapTile);
    		nextDevCard();
    	}*/
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    }
}
