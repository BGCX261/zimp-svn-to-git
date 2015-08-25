package com.zimp.sceneeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.zimp.GlobalPreferences;
import com.zimp.R;
import com.zimp.gamedata.GameRules;
import com.zimp.gamedata.ZimpPackage;
import com.zimp.play.GameActivity;
import com.zimp.play.ZimpAActivity;

public class SceneEditor extends TabActivity {
	private static final String TAG = "GameRules";
	GlobalPreferences globalPreferences;

    private Resources res;
	
    private TabHost tabHost;
	private Spinner mapSpinner;
	private Spinner devCardsSpinner;
	private Spinner itemsSpinner;
	private Spinner startMapTileSpinner;
	private ArrayAdapter<CharSequence> startTileSpinAdapter;
	private Spinner destMapTileSpinner;
	private ArrayAdapter<CharSequence> destTileSpinAdapter;
	private Spinner mapLayoutSpinner;
	
	private int lastSelectedTab = 0;
	
	// TAB 4 Data: Objectives 
	Button[] butObj = null;
	String[] jsonObj = null;
	
	String[] items, spinItems;
	String[] maps,  spinMaps;
	String[] devCards, spinDevCards;
	String[] mapTiles[];
	int		 selectedItem, selectedMap, selectedDevCards;
	int		 parsedItem = 878937, parsedMap = 878937, parsedDevCards = 878937;
	
	final private int NUM_OBJECTIVES = 5;
	
	final private int TAB_INFO  = 0;
	final private int TAB_TILES = 1;
	final private int TAB_MAP   = 2;
	final private int TAB_OBJ   = 3;
	final private int TAB_RULES = 4;
	final private int TAB_GENERATE = 5;
	
	final private int REQ_CODE_PICK_SCENE_IMAGE = 76764;
	final private int REQ_CODE_OBJECTIVE 		= 28346;
	final private int REQ_CODE_PLAY				= 89347;
	
	private GameRules gameRules = null;
	
	private int[] tabMarker;
	
	private Uri selectedImage = null;
	private String sceneImage = null;
	private String rulesJsonPath = null;

	// Generation Variables
	String scenarioJSONName = null;
	String scenarioBasePath = null; 
	String storyName        = null;	
	
	private String errorText        = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.sceneeditor);
    	
    	globalPreferences = GlobalPreferences.getGlobalPreferences();
    	
    	res = getResources(); // Resource object to get Drawables
    	tabHost  = getTabHost();
    	tabMarker = new int[6];
    		//(TabHost)findViewById(R.id.tabhost);  	// The activity TabHost
    	TabHost.TabSpec spec;

        spec = tabHost.newTabSpec("specMetaInfo");
        spec.setContent(R.id.metaInfoTable);
        spec.setIndicator(getString(R.string.seInfo), res.getDrawable(R.layout.sc_tab_metainfo));
        tabMarker[0] = R.layout.sc_tab_metainfo;
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("tiles");
        spec.setContent(R.id.pathsTopLevel);
        spec.setIndicator(getString(R.string.seTiles), res.getDrawable(R.layout.sc_tab_tick));
        tabMarker[1] = R.layout.sc_tab_tick;
        tabHost.addTab(spec);
        setupTilesTab();

        spec = tabHost.newTabSpec("map");
        spec.setContent(R.id.mapTopLevel);
        spec.setIndicator(getString(R.string.seMap), res.getDrawable(R.layout.sc_tab_metainfo));
        tabMarker[2] = R.layout.sc_tab_metainfo;
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("objectives");
        spec.setContent(R.id.objectivesTopLevel);
        spec.setIndicator(getString(R.string.seObjectives), res.getDrawable(R.layout.sc_tab_metainfo));
        tabMarker[3] = R.layout.sc_tab_metainfo;
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("rules");
        spec.setContent(R.id.rulesTopLevel);
        spec.setIndicator(getString(R.string.seRules), res.getDrawable(R.layout.sc_tab_tick));
        tabMarker[4] = R.layout.sc_tab_tick;
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("generate");
        spec.setContent(R.id.generateTopLevel);
        spec.setIndicator(getString(R.string.sePlotItems), res.getDrawable(R.layout.sc_tab_metainfo));
        tabMarker[5] = R.layout.sc_tab_metainfo;
        tabHost.addTab(spec);
                
        tabHost.setCurrentTab(0);
        Log.i(TAG, "Created Tab Spec and Host");

        // Tab 0: Meta Information Description
        ImageButton sceneImageButton = (ImageButton) findViewById(R.id.sceneImage);
        sceneImageButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		launchImageChooser(REQ_CODE_PICK_SCENE_IMAGE);
			}
		});
        
        // Tab 2: Map Description 
    	startTileSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
    	startTileSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    	destTileSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
    	destTileSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	
    	mapLayoutSpinner = (Spinner) findViewById(R.id.mapLayoutSpinner);
        ArrayAdapter<CharSequence> mapLayoutAdapter = ArrayAdapter.createFromResource(
                this, R.array.seMapLayoutSpinner_array, android.R.layout.simple_spinner_item);
        mapLayoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapLayoutSpinner.setAdapter(mapLayoutAdapter);
       
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
        	@Override
        	public void onTabChanged(String arg0) {         
            	
        		updateSelectedTiles();
                Log.i(TAG, "Selected Tab with index:" + tabHost.getCurrentTab());
                
                
                if(lastSelectedTab == TAB_TILES) {
                	if((parsedMap != selectedMap)) {
                		parseGameRules();
                	}                	
                } else if(lastSelectedTab == TAB_INFO) {
                	String error = checkInfoTab();
                	if(error == null) { 
                    	changeTabImage(TAB_INFO, R.layout.sc_tab_tick);
                        Log.i(TAG, "Info Tab Successfully entered");
                	} else if(error != null) {
                    	changeTabImage(TAB_INFO, R.layout.sc_tab_metainfo);
                        Log.i(TAG, "Info Tab Not properly filled yet - "+error);
                	}
                }
                
                if(tabHost.getCurrentTab() == TAB_MAP) {
                	if((parsedMap != selectedMap)) {
                		parseGameRules();
                	}
                	
                	// Tick the tab
                	changeTabImage(TAB_MAP, R.layout.sc_tab_tick);
                  
                } else if(tabHost.getCurrentTab() == TAB_OBJ) {
                	if((parsedMap != selectedMap)) 
                		parseGameRules();
                } else if(tabHost.getCurrentTab() == TAB_GENERATE) {
                	
                	Button bt = (Button) findViewById(R.id.buttonGenerate);
                	if(areTabsOk() == true) {
                		changeTabImage(TAB_GENERATE, R.layout.sc_tab_tick);
                		bt.setEnabled(true);                		
                	} else {
                		changeTabImage(TAB_GENERATE, R.layout.sc_tab_metainfo);
                		bt.setEnabled(false);
                	}                	
                }
                
                	
                // New last selected tab
                lastSelectedTab = tabHost.getCurrentTab();
            }

        });  
        
        // Tab 4: Objectives
        setupObjectiveHandlers();
        
        // Tab 6: Generate
        setupGenerateHandlers();
                
    }

	// Setup Generate Handlers
	private void setupGenerateHandlers() {
		
		Button buttonGenerate = (Button) findViewById(R.id.buttonGenerate);
		buttonGenerate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				generateFinalJSON();
			}
		});

		// Play button allows the user to try to play once
		Button playGenerate = (Button) findViewById(R.id.buttonPlayZimp);
		playGenerate.setEnabled(false);
		
		playGenerate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchPlayScenario();				
			}
		});

		// Play button allows the user to try to play once
		Button exportStory = (Button) findViewById(R.id.buttonExport);
		exportStory.setEnabled(false);
		
		exportStory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				exportStory();				
			}
		});
		
		
	}

	
	/*
	 * Export ZIP File to SD Card
	 */
	protected void exportStory() {
				
		String packBasePath = globalPreferences.getPackagePathBase(); 
		String storyPack = packBasePath + "/" + storyName + ".zip";
		String storyFolder = packBasePath + "/" + storyName;
		EditText outputTextBox = (EditText)findViewById(R.id.editGenerateResults); 
		
		// Step 1: If SD Card does not exists flag error
		if(packBasePath == null) {
			/// TODO : showDialog NO_SDCARD
//			showDialog(DIALOG_NO_SDCARD);
			outputTextBox.setText("Please insert SD Card to export");
			return;
		}
			
		
		// Step 2: If older zip exists, unpack it
		if((ZimpPackage.checkmakeDirectory(storyFolder) == true) &&
				(ZimpPackage.fileExists(storyPack) == true)) {
			ZimpPackage.unzipToTemp(storyPack, storyFolder);
		}
		
		// Step 3: Copy file to right directory spot in temp directory
		if(ZimpPackage.copyFile(globalPreferences.getDataDir() + rulesJsonPath, scenarioBasePath, storyFolder, rulesJsonPath) == false)	{
			Log.e(GlobalPreferences.TAG, "Unable to copy to " + storyFolder + " file " + rulesJsonPath);
			outputTextBox.setText("Please insert SD Card to export");;
		}
		
		// Step 4: Create zip package of the story
		try {
			ZimpPackage.zipFolder(storyFolder, storyPack);
		} catch (Exception e) {
			Log.e(GlobalPreferences.TAG, "Unable to zip make" + storyPack + " from " + storyFolder);
			Log.e(GlobalPreferences.TAG, e.getMessage());
			e.printStackTrace();
			outputTextBox.setText("Error exporting " + e.getMessage());
			return;
		}
			
		// Step 5: Update Edit Box to mention
		outputTextBox.setText("Exported file " + storyPack);
				
		
	}

	///////////////////////////////////////////////////////////////////
	// Generate the final JSON
	// errorText is set, if anything goes wrong and is displayed later. 
	// Imp to set errorText, as we treat it == null for going to next toll gate.
	protected void generateFinalJSON() {
		
		JSONObject jGenObject = new JSONObject();
		EditText ed;
		
		
		EditText outputTextBox = (EditText)findViewById(R.id.editGenerateResults); 
		outputTextBox.setText("");
		errorText  = null;
		
		try {
		
			//////////////////////////////////////////////////////////////
			// Section 1 : Meta Info
			JSONObject jMetaInfoObject = new JSONObject();

			// Scene Name
			ed = (EditText)findViewById(R.id.sceneName);
			scenarioJSONName = ed.getEditableText().toString();
			jMetaInfoObject.put("ScenarioName", scenarioJSONName);
			
			ed = (EditText)findViewById(R.id.storyName);
			scenarioBasePath = "/data/rules/stories/" + ed.getEditableText().toString() + "/";
			storyName = ed.getEditableText().toString();
			jMetaInfoObject.put("ScenarioBasePath", scenarioBasePath);
			
			// FIXME: Remember to copy image to directory after creation.
			if(sceneImage != null ) {
				jMetaInfoObject.put("ScenarioImage", scenarioBasePath+selectedImage.getLastPathSegment());
			}
			 
			ed = (EditText)findViewById(R.id.sceneDesc);
			String sceneDesc = ed.getEditableText().toString();
			jMetaInfoObject.put("ScenarioDescription", sceneDesc);
			
			jGenObject.put("MetaInfo", jMetaInfoObject);
						
			////////////////////////////////////////////////////////////
			// Section 2: Rules
			JSONObject jRulesObject = new JSONObject();

			ed = (EditText)findViewById(R.id.maxHitPoints);
			String res = checkNum(ed.getEditableText());
			if(res != null) {
				errorTextappend("MaxHitPoints "+res+"\n");
			} else {
				gameRules.setMaxHP(Integer.parseInt(ed.getEditableText().toString()));
				jRulesObject.put("MaxHP", gameRules.getMaxHP());
			}

			
			// Start Hit Points
			ed = (EditText)findViewById(R.id.startHitPoints);
			res = checkNum(ed.getEditableText());
			if(res != null) {
				errorTextappend("StartHitPoints "+res+"\n");
			} else {
				gameRules.setStartHP(Integer.parseInt(ed.getEditableText().toString()));
				if(gameRules.getMaxHP() < gameRules.getStartHP()) {
					errorTextappend("Max Hit Points should be more than Start Hit Points");
				} else {
					jRulesObject.put("StartHP", gameRules.getStartHP());
				}
			}
			
			// Max Discard Rounds
			ed = (EditText)findViewById(R.id.discardRounds);
			res = checkNum(ed.getEditableText());
			if(res != null) {
				errorTextappend("Discard Rounds "+res+"\n");
			} else {
				gameRules.setMaxDiscardRounds(Integer.parseInt(ed.getEditableText().toString()));
				jRulesObject.put("MaxDiscardRounds", gameRules.getMaxDiscardRounds());
			}

			// Start Time
			ed = (EditText)findViewById(R.id.editStartTime);
			res = checkNum(ed.getEditableText());
			if(res != null) {
				errorTextappend("Start Time "+res+"\n");
			} else {
				int hour = Integer.parseInt(ed.getEditableText().toString()); 
				jRulesObject.put("StartTime", hour);
			}
			
			// End Time
			ed = (EditText)findViewById(R.id.editEndTime);
			res = checkNum(ed.getEditableText());
			if(res != null) {
				errorTextappend("End Time "+res+"\n");
			} else {
				int hour = Integer.parseInt(ed.getEditableText().toString()); 
				jRulesObject.put("EndTime", hour);
			}
			
			// Constant values
			jRulesObject.put("HoursPerDiscard", 1);
			jRulesObject.put("MaxCarryItems", 2);
			
			// Start and Dest Tiles
			Spinner sp = (Spinner)findViewById(R.id.startTileSpinner);
			gameRules.setStartTileName(sp.getSelectedItem().toString());
			jRulesObject.put("StartTile", gameRules.getStartTileName());
			
			sp = (Spinner)findViewById(R.id.destTileSpinner);
			gameRules.setFinalPlotDestTileName(sp.getSelectedItem().toString());
			jRulesObject.put("DestTile", gameRules.getFinalPlotDestTileName());
		
			
			////////////////////////////////////////////////////
			// Section 3: Objectives Parsing.
			
			JSONArray jObjectiveArray = new JSONArray();
			
			for (int i = 0; i < NUM_OBJECTIVES; i++) {
				if(jsonObj[i] != null) {
					JSONObject jObjectiveObject = new JSONObject(jsonObj[i]);
					jObjectiveArray.put(jObjectiveObject);
				}
			}
			
			jRulesObject.put("Objectives", jObjectiveArray);
			
			// Now Rules are over after addition of objectives
			jGenObject.put("Rules", jRulesObject);
			
			//////////////////////////////////////////////////////
			// Section 4: Register the paths
			JSONObject jPathsObject = new JSONObject();
			jPathsObject.put("ItemJsonPath", gameRules.getItemsJsonPath());
			jPathsObject.put("DevCardsJsonPath", gameRules.getDevJsonPath());
			jPathsObject.put("MapTilesJsonPath", gameRules.getMapTilesJsonPath());
			jGenObject.put("Paths", jPathsObject);
			
			//////////////////////////////////////////////////////
			// Section 5: Copy files and images
			if((mkdir(scenarioBasePath) == false)) {
				errorTextappend("Internal Error accessing file system at" + scenarioBasePath );
			}
			
			if(selectedImage != null) {
				res = copyFile(selectedImage.getPath(), scenarioBasePath+selectedImage.getLastPathSegment());
				if(res != null) {
					errorTextappend(res);
				}
			}
			
			/////////////////////////////////////////////////////////
			// Section 6: Complete the JSON Generation
			rulesJsonPath = scenarioBasePath + scenarioJSONName + "play.json";
			res = writeStringToFile(rulesJsonPath, jGenObject.toString(3));
			if( res != null) {
				errorTextappend(res);
			}
			
			Button buttonPlay = (Button) (findViewById(R.id.buttonPlayZimp)); 
			if(errorText != null) {
				outputTextBox.setText(errorText);
				buttonPlay.setEnabled(false);
			} else {
				outputTextBox.setText("Success - Play Now");
				buttonPlay.setEnabled(true);
			}
			
		} catch (JSONException e) {
			
			Log.i(TAG, "Error Generating JSON" + e.getMessage());
			e.printStackTrace();
		}

		
		// Complete JSON Generation Done
		
	}

	// Append to Error Text String that is used to communicate errors in Scene Generation.
	private void errorTextappend(String in) {
		if(errorText == null) {
			errorText = in;
		} else {
			errorText.concat(in);
		}			
	}
	
	
	// Write a string to a file
	private String writeStringToFile(String outFile, String inputString) {

		try {
			FileWriter out = new FileWriter(globalPreferences.getDataDir()+outFile);
			out.write(inputString);
			out.close();
			Log.i(TAG, "Writing Json to " + outFile);
			Log.i(TAG, inputString);
		} catch (IOException e) {
			return e.getMessage() + "\n";
		} 
		return null; 
	}
	
	private boolean mkdir(String path) {
		File dir = new File(globalPreferences.getDataDir() + path);
		if(dir.exists() == true) {
			return true;
		} else {
			return dir.mkdirs();
		}
	}
	
	
	// Copies input file to output file
	private String copyFile(String inputFile, String outputFile) {
		try {
			FileReader in = new FileReader(inputFile);
			FileWriter out = new FileWriter(globalPreferences.getDataDir()+outputFile);
			int c;

		    while ((c = in.read()) != -1)
		      out.write(c);

		    in.close();
		    out.close();
		} catch (FileNotFoundException e) {
			return "File Not Found : " + inputFile + "\n";
		} catch (IOException e) {
			return e.getMessage() + "\n";
		}

		
		return null;		
	}
	
	private void changeTabImage(int tabNo, int drawable){
        ImageView iv = (ImageView) tabHost.getTabWidget().getChildTabViewAt(tabNo).findViewById(android.R.id.icon);
        iv.setImageDrawable( res.getDrawable(drawable));
        Log.i(TAG, "Tab Changed :" + tabNo);
        tabMarker[tabNo] = drawable;
	}
	
	// Are all the TABS certified ok to enable generation?
	private boolean areTabsOk() {
		for (int i = 0; i < 5; i++) {
			if(tabMarker[i] == R.layout.sc_tab_metainfo) {
				return false;
			}
		}
		return true;
	}
	
	// Parse Game Rules According to the tiles	
	private void parseGameRules() {
		Log.d(TAG, "Parsed Map = "+parsedMap + "; Selected Map = "+ selectedMap + "Reparsing Map...");
		try {
			gameRules = new GameRules(items[selectedItem], maps[selectedMap], devCards[selectedDevCards]);
			
			parsedItem = selectedItem;
			parsedDevCards = selectedDevCards;
			parsedMap = selectedMap;
		} catch (JSONException e) {
			Log.e(TAG, "JSON Parsing Error:"+ e.getMessage());
			e.printStackTrace();
		}
    	// Update the spinners based on the maps
		// The map itself has changed, so the selection is invalidated
    	startMapTileSpinner = (Spinner) findViewById(R.id.startTileSpinner);
    	destMapTileSpinner = (Spinner) findViewById(R.id.destTileSpinner);
    	
    	startTileSpinAdapter.clear();
    	destTileSpinAdapter.clear();
    	int numMapTiles = gameRules.MapTilesList.size();
    	for(int i = 0; i < numMapTiles; i++) {
        	startTileSpinAdapter.add(gameRules.MapTilesList.get(i).getName());
        	destTileSpinAdapter.add(gameRules.MapTilesList.get(i).getName());
    	}
    	startMapTileSpinner.setAdapter(startTileSpinAdapter);
    	destMapTileSpinner.setAdapter(startTileSpinAdapter);

	}	
	
	private void setupObjectiveHandlers() {

		butObj = new Button[5];
		jsonObj = new String[5];
		
		butObj[0] = (Button) findViewById(R.id.objective0);
		butObj[1] = (Button) findViewById(R.id.objective1);		
		butObj[2] = (Button) findViewById(R.id.objective2);		
		butObj[3] = (Button) findViewById(R.id.objective3);		
		butObj[4] = (Button) findViewById(R.id.objective4);
		
		butObj[0].setOnClickListener(buttonObjClick);
		butObj[1].setOnClickListener(buttonObjClick);
		butObj[2].setOnClickListener(buttonObjClick);
		butObj[3].setOnClickListener(buttonObjClick);
		butObj[4].setOnClickListener(buttonObjClick);
		
		butObj[0].setEnabled(true);
		butObj[1].setEnabled(false);
		butObj[2].setEnabled(false);
		butObj[3].setEnabled(false);
		butObj[4].setEnabled(false);

	}
	
	private OnClickListener buttonObjClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int objNo = 0;
			
			switch (v.getId()) {
				case R.id.objective0:
					objNo = 0;
					break;
				case R.id.objective1:
					objNo = 1;
					break;
				case R.id.objective2:
					objNo = 2;
					break;
				case R.id.objective3:
					objNo = 3;
					break;
				case R.id.objective4:
					objNo = 4;
					break;
			}
			launchObjectiveEditor(REQ_CODE_OBJECTIVE, objNo);
			
		}
	};

	// Launch editor activity for a particular objective
	private void launchObjectiveEditor(int intentCode, int objNo) {
		
		Intent intentObjective = new Intent(this, ObjectivesEditor.class);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_NO, objNo);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_JSON, jsonObj[objNo]);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_DEVCARDS, gameRules.getDevJsonPath());
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_ITEMS, gameRules.getItemsJsonPath());
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_MAP, gameRules.getMapTilesJsonPath());
				
		startActivityForResult(intentObjective, REQ_CODE_OBJECTIVE);
		
	}
	
	// Play the scenario of to try it out
	protected void launchPlayScenario() {

		Intent intentPlay = new Intent(this, GameActivity.class);
		intentPlay.putExtra(GlobalPreferences.KEY_GAME_RULES, rulesJsonPath);
		
		startActivityForResult(intentPlay, REQ_CODE_PLAY);
	}

	// Launch image chooser
	private void launchImageChooser(int intentCode) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
	    Log.i(TAG, "Launching image chooser");
		
		startActivityForResult(photoPickerIntent, intentCode);
	}

	// Process mainly responses from the image picker regarding various chosen photos
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    Log.i(TAG, "Intent gotten back. Activity Callback successful");
	    switch(requestCode) { 
	    case REQ_CODE_PLAY:
	        	Button buttonExport = (Button)findViewById(R.id.buttonExport);
	        	buttonExport.setEnabled(true);	        	
	        	
	    		EditText outputTextBox = (EditText)findViewById(R.id.editGenerateResults); 
	        	outputTextBox.setText("Good Enough for export locally, you decide?");
	        break;	    
	    case REQ_CODE_PICK_SCENE_IMAGE:
	        if(resultCode == RESULT_OK){  
	            selectedImage = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};

	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();
	            
	            // update the sceneImage
	            sceneImage = filePath;
	            ImageButton sceneImageButton = (ImageButton) findViewById(R.id.sceneImage);
	            sceneImageButton.setImageURI(selectedImage);
	            
	            TextView tv = (TextView) findViewById(R.id.labelImageName);
	            tv.setText(selectedImage.getLastPathSegment());
	            
//	            sceneImageButton.setImageBitmap(chosenImage);	            
	        }
	        break;
	    case REQ_CODE_OBJECTIVE:
	    	if(resultCode == RESULT_OK) {
	    		Bundle extras  = imageReturnedIntent.getExtras();
	    		String textObj = extras.getString(GlobalPreferences.KEY_OBJ_TEXT);
	    		int	   objNo   = extras.getInt(GlobalPreferences.KEY_OBJ_NO);
	    			    		
	    		if(textObj != null) {
	    			butObj[objNo+1].setEnabled(true);
	    			butObj[objNo].setText(textObj);
	    			jsonObj[objNo] = extras.getString(GlobalPreferences.KEY_OBJ_JSON);
	    		} else {
	    			butObj[objNo].setText(getString(R.string.seObjective));
	    		}
				changeTabImage(TAB_OBJ, R.layout.sc_tab_tick);
			    	    		
		    } else if(resultCode == RESULT_CANCELED) {
		    	if(imageReturnedIntent != null) {
		    		Bundle extras  = imageReturnedIntent.getExtras();
		    		if(extras != null) {
		    			int	   objNo = extras.getInt(GlobalPreferences.KEY_OBJ_NO);
		    			butObj[objNo].setText(getString(R.string.seObjective));
		    			jsonObj[objNo] = null;
		    			
		    			for(int i=objNo+1; i<5; i++) {
			    			butObj[i].setText(getString(R.string.seObjective));
			    			butObj[i].setEnabled(false);
		    			}
		    			// Mark it as not ok when the first objective is discarded
		    			if(objNo == 0)
		    				changeTabImage(TAB_OBJ, R.layout.sc_tab_metainfo);
		    		}
		    	}
		    }
	    	break;
	    	default:
	    		Log.e(TAG, "UNKNOWN requestCode in SceneEditor Activity Result : " + requestCode);
	    		break;
    	}
	}
	
	// Check if all has been entered properly in the InfoTab
	// Returns back the error string that can be easily displayed.
	// If nothing is wrong, returns NULL
	private String checkInfoTab() {
		
		EditText sceneName = (EditText) findViewById(R.id.sceneName);
		EditText storyName = (EditText) findViewById(R.id.storyName);
		EditText description = (EditText) findViewById(R.id.sceneDesc);
		String error = null;
		
		error = checkAlphaNum(sceneName.getText());
		if(error != null) {
			return getString(R.string.seErrorSceneName)+ error;
		}

		error = checkAlphaNum(storyName.getText());
		if(error != null) {
			return getString(R.string.seErrorStoryName)+ error;
		}

		if(description.getText().length() == 0) {
			return (getString(R.string.seErrorSceneDesc)+ getString(R.string.seErrorEmpty));
		}
		
		// error should be null here
		return error;				
	}       
	
	// Check if entered string is alphanumeric and not empty
	private String checkAlphaNum(Editable str) {
		
		int strLen = str.length();
		if(strLen == 0)
			return getString(R.string.seErrorEmpty);
	
		for(int i=0; i<strLen; i++) {
			if(Character.isLetterOrDigit(str.charAt(i)) == false) { 
				return getString(R.string.seErrorAlpha);
			}
		}
		return null;
	}

	// Check if entered string is numeric and not empty
	private String checkNum(Editable str) {
		
		int strLen = str.length();
		if(strLen == 0)
			return getString(R.string.seErrorEmpty);
	
		for(int i=0; i<strLen; i++) {
			if(Character.isDigit(str.charAt(i)) == false) { 
				return getString(R.string.seErrorNum);
			}
		}
		return null;
	}
	
	// Look at current state of selection of the map, item and dev cards spinner and update selection variables.
	private void updateSelectedTiles() {
		selectedMap = mapSpinner.getSelectedItemPosition();
		selectedDevCards = devCardsSpinner.getSelectedItemPosition();
		selectedItem = itemsSpinner.getSelectedItemPosition();
	}
	
	private void setupMapSpinner() {
		mapSpinner = (Spinner) findViewById(R.id.mapSpinner);
		maps = globalPreferences.getMaps();
		spinMaps = new String[maps.length+1];
		
		for(int i =0; i<maps.length; i++) {
			File extractor = new File(maps[i]);
			
			int lastIndex = extractor.getName().lastIndexOf("map.json");
			spinMaps[i] = extractor.getName().substring(0, lastIndex);
		}
		spinMaps[maps.length] = getString(R.string.seCreateNewMap);
		
		ArrayAdapter<CharSequence> mapSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinMaps);
		mapSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mapSpinner.setAdapter(mapSpinAdapter);
	
	}

	private void setupItemsSpinner() {
		itemsSpinner = (Spinner) findViewById(R.id.itemSpinner);
		items = globalPreferences.getItems();
		spinItems = new String[items.length+1];
		
		for(int i =0; i<items.length; i++) {
			File extractor = new File(items[i]);
			
			int lastIndex = extractor.getName().lastIndexOf("items.json");
			spinItems[i] = extractor.getName().substring(0, lastIndex);
		}
		spinItems[items.length] = getString(R.string.seCreateNewItemSet);
		
		ArrayAdapter<CharSequence> itemsSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinItems);
		itemsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		itemsSpinner.setAdapter(itemsSpinAdapter);
	}

	private void setupDevCardsSpinner() {
		devCardsSpinner = (Spinner) findViewById(R.id.devCardsSpinner);
		devCards = globalPreferences.getDevCards();
		spinDevCards = new String[devCards.length+1];
		
		for(int i =0; i<devCards.length; i++) {
			File extractor = new File(devCards[i]);
			
			int lastIndex = extractor.getName().lastIndexOf("devcards.json");
			spinDevCards[i] = extractor.getName().substring(0, lastIndex);
		}
		spinDevCards[devCards.length] = getString(R.string.seCreateNewCardsSet);
		
		
		ArrayAdapter<CharSequence> devCardsSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinDevCards);
		devCardsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		devCardsSpinner.setAdapter(devCardsSpinAdapter);
	}
	
	private void setupTilesTab() {
		
		setupMapSpinner();
		setupItemsSpinner();
		setupDevCardsSpinner();
	}	
}


