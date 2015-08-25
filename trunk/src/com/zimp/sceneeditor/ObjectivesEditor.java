package com.zimp.sceneeditor;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.zimp.GlobalPreferences;
import com.zimp.R;
import com.zimp.gamedata.GameRules;
import com.zimp.gamedata.Item;
import com.zimp.gamedata.MapTiles;
import com.zimp.gamedata.Objectives;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

// Objectives Editor takes input of following fields
// a. ObjectiveText - EditText      
// b. TriggerTile   - Spinner
// c. Need Items    - Dialog
// d. Need Objectives - Dialog
// e. Action        - Spinner (Constant)
// f. Preaction Text - EditText
// g. Postaction Text - EditText
// h. Gain Health    - RatingsBar
// i. Gain Item      - Spinner
// j. Enable Tile    - Spinner
public class ObjectivesEditor extends Activity {

	GlobalPreferences globalPreferences;
	
	
	private static final int DIALOG_ITEMS = 0;
	private static final int DIALOG_OBJECTIVES = 1;
	private static final int DIALOG_ERROR = 2;
	
	// Intent input Parameters
	private int 	objNo;				// Just to be passed back
	private String 	objText;			// Text of the objectives
	private String	strJson = null;		// JSON String
	private	String	itemsPath = null;
	private String  mapPath   = null; 	
	private String  devCardsPath  = null;
	
	JSONObject      jObject = null;
	Objectives		objective = null;	// Objective
	GameRules 		gameRules = null;

	String			jsonObjective;		// final output objective of json
	
	String 			dialogMessage = null; 
		
	private ArrayAdapter<CharSequence> mapSpinAdapter = null;
	private ArrayAdapter<CharSequence> itemsSpinAdapter = null;
	private ArrayAdapter<CharSequence> actionSpinAdapter = null;
	
	protected String[] itemOptions = null;
	protected String[] objOptions = null;
	protected boolean[] itemSelections = null;
	protected boolean[] objSelections = null;
	
	Boolean screenSetup = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.sc_objectives_edit);
    	
    	globalPreferences = GlobalPreferences.getGlobalPreferences();
    	
    	Bundle extras = getIntent().getExtras();
    	if(extras != null) {
    		itemsPath = extras.getString(GlobalPreferences.KEY_OBJ_ITEMS);
    		mapPath   = extras.getString(GlobalPreferences.KEY_OBJ_MAP);
    		devCardsPath  = extras.getString(GlobalPreferences.KEY_OBJ_DEVCARDS);
    		
    		objNo   = extras.getInt(GlobalPreferences.KEY_OBJ_NO);
    		strJson = extras.getString(GlobalPreferences.KEY_OBJ_JSON);
    		// Fill the details in the UE from older objectives
   			setupFieldWithOldObjective();
    	}
    	
    	setupClickHandlers();
	}

	@Override
	protected Dialog onCreateDialog( int id ) 
	{
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch(id) {
		case DIALOG_ITEMS: {
        	builder.setTitle( "Choose One or More Items" );
        	builder.setMultiChoiceItems( itemOptions, itemSelections, new ItemDialogSelectionClickHandler() );
        	builder.setPositiveButton( "OK", null );
        	// TODO: Mark the check box as deselected in case no item is selected. 
        	dialog = builder.create();
			break;
		}
		case DIALOG_OBJECTIVES:{
			// TODO: FIX this to allow selection of objectives
		}
		case DIALOG_ERROR: {
			builder.setMessage(dialogMessage)
				   .setPositiveButton("OK", null);
			dialog = builder.create();
			break;			
		}
		} 
		return dialog;
	}
	
	public class ItemDialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
	{
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{
			itemSelections[ clicked ] = selected;
			Log.d( "Chosen", itemOptions[ clicked ] + " selected: " + selected );
		}
	}

	
	// - Parse existing Json
	// - Fill in the UE fields with Json data
	private void setupFieldWithOldObjective() {

		try {
			Spinner  sp = null;
			
			// Parse game rules from the json files
			gameRules = new  GameRules(itemsPath, mapPath, devCardsPath); 

			/////////////////////////////////////////////////////////////////////////////////////
			// Phase 1: Setup all fields with defaults
			// a. ObjectiveText - EditText      
			// d. Need Objectives - Dialog
			// f. Preaction Text - EditText
			// g. Postaction Text - EditText
			// h. Gain Health    - RatingsBar
		
			// e. Action        - Spinner (Constant)
			// Action Spinner
			sp = (Spinner) findViewById(R.id.spinAction);
			actionSpinAdapter = ArrayAdapter.createFromResource(this, R.array.obj_ActionArray, android.R.layout.simple_spinner_item);
			actionSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp.setAdapter(actionSpinAdapter);
		

			// b. TriggerTile   - Spinner
			// j. Enable Tile    - Spinner

			// Map adapter is used for trigger tile and for enable tile
			// Used for [ Trigger Tile, Enable Tile ] 
			sp = (Spinner) findViewById(R.id.spinNeedTile);
			mapSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
			mapSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
			mapSpinAdapter.clear();
			int numMapTiles = gameRules.MapTilesList.size();
			for(int i = 0; i < numMapTiles; i++) {
		    	mapSpinAdapter.add(gameRules.MapTilesList.get(i).getName());
			}
			mapSpinAdapter.add(GlobalPreferences.JSON_NULL);
			
			sp.setAdapter(mapSpinAdapter);
			sp.setSelection(numMapTiles);
		
			sp = (Spinner) findViewById(R.id.spinEnableTile);
			sp.setAdapter(mapSpinAdapter);
			sp.setSelection(numMapTiles);
			
			// i. Gain Item      - Spinner
			// c. Need Items    - Dialog

			// Tile adapter is used for [Gain Item] 
			itemsSpinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
			itemsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			Set<String> itemKeys = gameRules.ItemsMap.keySet();
			
			int numItems = itemKeys.size();
			itemOptions = new String[numItems];
			itemSelections = new boolean[numItems];
			
			int i = 0;
			for(String key : itemKeys) {
				Item item = gameRules.ItemsMap.get(key);
				itemsSpinAdapter.add(item.name);
				itemOptions[i] = item.name;
				itemSelections[i] = false;
				i++;
			}
			
			itemsSpinAdapter.add(GlobalPreferences.JSON_NULL);
			sp = (Spinner) findViewById(R.id.spinGainItem);			
			sp.setAdapter(itemsSpinAdapter);
			sp.setSelection(numItems);

			CheckBox cb = (CheckBox) findViewById(R.id.checkNeedItems);
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if((isChecked == true) && screenSetup != true ) {
						showDialog(DIALOG_ITEMS);
					}
				}
			});

			/////////////////////////////////////////////////////////////////////////////////////
			// Phase 2: Restore Old Objective Fields
			// Section to place older objective
			// Parse older objective, if older objectives are present
			if(strJson == null) {
				screenSetup = false;
				return;
			}
			jObject   = new JSONObject(strJson);
		
			objective = new Objectives(jObject);
	
			// a. Objective Text
			EditText ed = (EditText) findViewById(R.id.editObjective);
			ed.setText(objective.ObjectiveText);

			// b. Trigger Tile
			sp = (Spinner) findViewById(R.id.spinNeedTile);
			if((objective.NeedTile == true) && (objective.NeededTile != null)) {
				sp.setSelection(mapSpinAdapter.getPosition(objective.NeededTile));
			} else {
				sp.setSelection(mapSpinAdapter.getPosition(GlobalPreferences.JSON_NULL));
			}
				
			// c. Need Items    - Dialog
			if(objective.NeedItems == true) {
				// Make sure that we actually have items only then turn it on.
				objective.NeedItems = false; 
				for(int k = 0; k < objective.NeededItems.size(); k++) {
					for(int j = 0; j<gameRules.ItemsMap.size(); j++) {
						if(objective.NeededItems.get(k).compareTo(itemOptions[j]) == 0) {
							itemSelections[j] = true;
							objective.NeedItems = true;							
						}
					}
				}
			}
			cb = (CheckBox) findViewById(R.id.checkNeedItems);
			cb.setChecked(objective.NeedItems);				
			
			// TODO : d. Need Objectives - Dialog
			
			// e. Action        - Spinner (Constant) 
			sp = (Spinner) findViewById(R.id.spinAction);
			sp.setSelection(objective.Action);
			
			// f. Preaction Text - EditText
			ed = (EditText) findViewById(R.id.editPreActionText);
			ed.setText(objective.PreActionText);
			
			// g. Postaction Text - EditText
			ed = (EditText) findViewById(R.id.editPostActionText);
			ed.setText(objective.PostActionText);

			// TODO FIXME not showing the achieved health back after reedit. Need to fix this
			// h. Gain Health    - RatingsBar
			RatingBar rt = (RatingBar) findViewById(R.id.ratingGainHealth);
			rt.setRating(objective.AchieveHealth);
			
			// i. Gain Item      - Spinner
			sp = (Spinner) findViewById(R.id.spinGainItem);
			if((objective.AchieveItems == true) && (objective.AchievedItems != null)) {
				sp.setSelection(itemsSpinAdapter.getPosition(objective.AchievedItems.get(0)));				
			} else {
				sp.setSelection(itemsSpinAdapter.getPosition(GlobalPreferences.JSON_NULL));
			}			
			
			// j. Enable Tile    - Spinner
			if(objective.AchievedTile != null) {
				sp = (Spinner) findViewById(R.id.spinEnableTile);
				sp.setSelection(mapSpinAdapter.getPosition(objective.AchievedTile));
			}
		
			// Setup of the screens is done
			screenSetup = false;
			
			} catch (JSONException e) {
				
			Log.e(globalPreferences.TAG, "Internal Json Parse Error - Objectives " + e.getMessage());
			e.printStackTrace();
			discardResponse();
		}
	}

	private void setupClickHandlers() {
		Button saveButton = (Button) findViewById(R.id.buttonSaveObj);
		Button discardButton = (Button) findViewById(R.id.buttonDiscardObj);
		
		discardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				discardResponse();
			}
		});
		
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveResponse();
			}
		});
	}
	
	// - Validates the fields
	// - Creates the json
	// - Sends response back to the caller 
	protected void saveResponse() {
		
		// TODO: Validate all fields
		if(objective == null)  
			objective = new Objectives();
		
		// a. Objective Text
		objective.ObjectiveText  = ((EditText) findViewById(R.id.editObjective)).getEditableText().toString();
		if((objective.ObjectiveText == null) || (objective.ObjectiveText.length() == 0) ) {
			dialogMessage = getString(R.string.seobjDialogObjTextEmpty);
			objective.ObjectiveText = null;
			showDialog(DIALOG_ERROR);
		}

		// b. Trigger Tile
		objective.NeededTile     = ((Spinner) findViewById(R.id.spinNeedTile)).getSelectedItem().toString();
		if(objective.NeededTile.compareTo(GlobalPreferences.JSON_NULL) == 0) {
			objective.NeededTile = null;
			objective.NeedTile = false;
		} else { 
			objective.NeedTile = true;
		}
		// c. Need Items 
		for(int i = 0; i < gameRules.ItemsMap.size(); i++) {
			if(itemSelections[i] == true) {
				objective.NeedItems = true;
				if(objective.NeededItems == null) {
					objective.NeededItems = new ArrayList<String>();
				}
				objective.NeededItems.add(itemOptions[i]);
				
			}
		}
		// TODO: d. Fix Needed Objectives
		// e. Action        - Spinner (Constant)
		
		objective.Action         = ((Spinner) findViewById(R.id.spinAction)).getSelectedItemPosition();
		// f. Preaction Text - EditText

		objective.PreActionText  = ((EditText) findViewById(R.id.editPreActionText)).getEditableText().toString();
		if(objective.PreActionText.length() == 0) 
			objective.PreActionText = null;
		
		// g. Postaction Text - EditText
		objective.PostActionText = ((EditText) findViewById(R.id.editPostActionText)).getEditableText().toString();
		if(objective.PostActionText.length() == 0)
			objective.PostActionText = null;
			
		// h. Gain Health    - RatingsBar
		objective.AchieveHealth  = (int)((RatingBar) findViewById(R.id.ratingGainHealth)).getRating();
		// i. Gain Item      - Spinner
		objective.AchievedItems  = new ArrayList<String>();
		String achievedItem = ((Spinner) findViewById(R.id.spinGainItem)).getSelectedItem().toString();
		if(achievedItem != null) {
			objective.AchievedItems.add(achievedItem);
			objective.AchieveItems = true;
		} else {
			objective.AchieveItems = false;
		}
		
		// j. Enable Tile    - Spinner
		objective.AchievedTile   = ((Spinner) findViewById(R.id.spinEnableTile)).getSelectedItem().toString();
		if(objective.AchievedTile.compareTo(GlobalPreferences.JSON_NULL) == 0) {
			objective.AchievedTile = null;
			objective.AchieveTile = false;
		} else {
			objective.AchieveTile = true;
		}
		
		try {
			jsonObjective = objective.convertToJSON(getResources());
		} catch (JSONException e) {
			Log.e(GlobalPreferences.TAG, "Internal Error constructing JSON : "+ e.getMessage());
			e.printStackTrace();
			discardResponse();
		}
		
		Intent intentObjective = new Intent();
		
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_NO, objNo);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_TEXT, objective.ObjectiveText);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_JSON, jsonObjective);
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_DEVCARDS, gameRules.getDevJsonPath());
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_ITEMS, gameRules.getItemsJsonPath());
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_MAP, gameRules.getMapTilesJsonPath());

		setResult(Activity.RESULT_OK, intentObjective);
		finish();				
	}

	// Makes a blank json to clear the objectives
	private void discardResponse() {
	
		Intent intentObjective = new Intent();
		intentObjective.putExtra(GlobalPreferences.KEY_OBJ_NO, objNo);
		setResult(Activity.RESULT_CANCELED, intentObjective);
		finish();
	}
	
}
