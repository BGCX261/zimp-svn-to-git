package com.zimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


import com.zimp.play.GameActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ZimpLauncher extends Activity {

	// Logging related
	private static final String TAG = GlobalPreferences.TAG;

	TextView tv;
	
	private static GlobalPreferences globalPreferences = GlobalPreferences.getGlobalPreferences();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		// Make Window Fullscreen as it is a game
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
    	// Setup DataDir
    	SharedPreferences prefs = getSharedPreferences("ZimpPrefs", 0);
    	globalPreferences.setZimpPrefs(prefs);		
    	globalPreferences.setDataDir("/data/data/" + this.getPackageName() + "/");
		
		setContentView(R.layout.main);
		
		
    	// setup listeners
    	setupItems();
		showDifficulty(globalPreferences.getDifficulty()); 

	}
	// Setup listeners for all clicks
	private void setupItems() {
		
    	Button bt = (Button)findViewById(R.id.mainPlay);
    	bt.setOnClickListener(onClickMenu);

    	bt = (Button)findViewById(R.id.mainStory);
    	bt.setOnClickListener(onClickMenu);
		
    	bt = (Button)findViewById(R.id.mainDifficulty);
    	bt.setOnClickListener(onClickMenu);
    	
    	bt = (Button)findViewById(R.id.mainHowto);
    	bt.setOnClickListener(onClickMenu);
		
    	bt = (Button)findViewById(R.id.mainUnderTheHood);
    	bt.setOnClickListener(onClickMenu);

    	bt = (Button)findViewById(R.id.mainExit);
    	bt.setOnClickListener(onClickMenu);

    	ImageButton ib = (ImageButton)findViewById(R.id.mainDiffImage);
    	ib.setOnClickListener(onClickMenu);
    	
	}
	
    protected void onListItemClick (View v) {

    	switch(v.getId()) {
		case R.id.mainPlay:
	    	Log.d(GlobalPreferences.TAG, "Play clicked in main menu");

	    	Intent playIntent	= new Intent(this, GameActivity.class);
    		playIntent.putExtra(GlobalPreferences.KEY_GAME_RULES, globalPreferences.getDefaultRules());
    		startActivity(playIntent);	
			break;
		case R.id.mainStory:
	    	Log.d(GlobalPreferences.TAG, "Story clicked in main menu");

    		Intent mIntent	= new Intent(this, ZimpStoriesActivity.class);
    		startActivity(mIntent);	
	    	break;
		case R.id.mainDifficulty:
		case R.id.mainDiffImage:
	    	Log.d(GlobalPreferences.TAG, "Change Difficulty clicked in main menu");
	    	
	    	int difficulty = globalPreferences.getDifficulty();
	    	difficulty = (difficulty + 1)%3;
	    	showDifficulty(difficulty);
	    	globalPreferences.setDifficulty(difficulty);
	    	
	    	Log.d(GlobalPreferences.TAG, "Change Difficulty to " + difficulty);
			break;
		case R.id.mainHowto:
	    	Log.d(GlobalPreferences.TAG, "How To clicked in main menu");
			break;
		case R.id.mainUnderTheHood:
	    	Log.d(GlobalPreferences.TAG, "Under the Hood clicked in main menu");

			Intent underIntent = new Intent(this, UnderTheHood.class);
			startActivityForResult(underIntent, 23897);
	    	break;
		case R.id.mainExit:
	    	Log.d(GlobalPreferences.TAG, "Exit Clicked clicked in main menu");

			this.onBackPressed();
	    	break;
		}
    	

    }
    private View.OnClickListener onClickMenu = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			onListItemClick(view);
		}
    };

    private void showDifficulty (int difficulty) {
    	ImageButton ib = (ImageButton)findViewById(R.id.mainDiffImage);
    	Button bt = (Button)findViewById(R.id.mainDifficulty);

    	switch(difficulty) {
    	case 0:
    	default:
    		ib.setImageResource(R.drawable.smile_easy);
    		bt.setText("Level : Easy");
    		break;
    	case 1: 
    		ib.setImageResource(R.drawable.smile_normal);
    		bt.setText("Level : Normal");
    		break;
    	case 2: 
    		ib.setImageResource(R.drawable.smile_tough);
    		bt.setText("Level : Tough");
    		break;
    		
    	}

    }
    
	
	
	void DeleteRecursive(File dir)
    {
        Log.d(GlobalPreferences.TAG, "DELETEPREVIOUS TOP" + dir.getPath());
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) 
            {
               File temp =  new File(dir, children[i]);
               if(temp.isDirectory())
               {
                   Log.d(GlobalPreferences.TAG, "Recursive Call" + temp.getPath());
                   DeleteRecursive(temp);
               }
               else
               {
                   Log.d(GlobalPreferences.TAG, "Delete File" + temp.getPath());
                   boolean b = temp.delete();
                   if(b == false)
                   {
                       Log.d(GlobalPreferences.TAG, "DELETE FAIL");
                   }
               }
            }

            dir.delete();
        }
    }
	private void deleteGameDirectory() {

		File rootDataDir = new File(globalPreferences.getDataDir());
		if(rootDataDir.exists() == true) {
			DeleteRecursive(rootDataDir);
		}
	}

	@Override 
	public void onStart() {
		super.onStart();
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		try {

			Bundle extras = getIntent().getExtras();
	    	if(extras != null) {
	    		boolean masterreset = extras.getBoolean(GlobalPreferences.KEY_LAUNCHER_RESET);
	    		if(masterreset == true) {
	    			Log.i(GlobalPreferences.TAG, "Master Reset: Clearing all data");
	    			deleteGameDirectory();
	    			globalPreferences.clearAllPreferences();
	    			Log.i(GlobalPreferences.TAG, "Cleared all data and recopying data afresh");

	    			Log.i(TAG, "First run copying assets to data folder....");
					copyAssetsToData();
					Log.i(TAG, "Copied Assets to data folder");
					globalPreferences.setSoundon(true);
					globalPreferences.incrementGamesPlayed();
					finish();
	    		}
	    	} 

			// check presence of External Storage
			// Or flag error to user - game needs external storage (around
			// 15-20MB)
			// TODO : Check external storage status

			// Check if first run
			// Copy assets to the data directory
			// TODO: Copy is to be done only on first run
			if(globalPreferences.getGamesPlayed() == 0) {
				Log.i(TAG, "First run copying assets to data folder....");
				copyAssetsToData();
				Log.i(TAG, "Copied Assets to data folder");
				globalPreferences.setSoundon(true);
				globalPreferences.incrementGamesPlayed();
			}

		} catch (NullPointerException n) {
			Log.e(TAG, "Null Pointer Exception: " + n.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException io) {
			Log.e(TAG, "IOException : " + io.getMessage());
			io.printStackTrace();
		} finally {
		}
		
	}
	
	public void onDestroy() {
		super.onDestroy();		
	}
	
	
		
	
	// Copy all game related assets to Data partition
	private void copyAssetsToData() throws IOException {
		String dataDir = "data";

		// Clear all preferences
		globalPreferences.clearAllPreferences();
		copyFileOrDir(dataDir);

		return;

	}

	// TODO: Deprecate
	// Move this function to the corresponding parser file
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

	// Copies all file from any area to data partition
	private void copyFileOrDir(String path) {

		AssetManager assetManager = this.getAssets();
		String assets[] = null;
		try {
			assets = assetManager.list(path);
			if (assets.length == 0) {
				copyFile(path);
			} else {
				// String fullPath = "/data/data/" + this.getPackageName() + "/"
				// + path;
				String fullPath = globalPreferences.getDataDir() + path;
				File dir = new File(fullPath);
				if (!dir.exists())
					dir.mkdir();
				for (int i = 0; i < assets.length; ++i) {
					copyFileOrDir(path + "/" + assets[i]);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "I/O Exception" + ex.getMessage());
		}
	}
	
	// Copies a single file into the data partition from assets
	private void copyFile(String filename) {
		AssetManager assetManager = this.getAssets();

		globalPreferences.fileAddToPreferences(filename);
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(filename);
			// String newFileName = "/data/data/" + this.getPackageName() + "/"
			// + filename;
			String newFileName = globalPreferences.getDataDir() + filename;
			out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			Log.d(TAG, "Copied File : " + newFileName);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}	
}
