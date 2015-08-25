package com.gamemaharaja.zombie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AppGlobalData {
	
	// Active Screens
	public static final int SCREEN_MAIN_MENU = 0;				// In menu
	public static final int SCREEN_CLASSIC_PLAY_BASE = 1001;	// 
	public static final int SCREEN_CLASSIC_PLAY_MAP = 1002;		
	public static final int SCREEN_CLASSIC_PLAY_ALERT = 1003;
	
	// State of application
	public static final int STATE_NO_PLAY      = 0;		// no game is being played
	public static final int STATE_CLASSIC_PLAY = 1;		// active classic game being played
	public static final int STATE_STORY_PLAY   = 2;		// active story being played
		
	private Stage stage;
	private Skin  sknButton;
	
	// Screens
	private int 			statePlay = 0;				// State of play
	private int 			stateScreen = 0;			// Current Screen
	private StartupScreen 	screenStartup; 
	private ClassicPlay 	screenClassicPlay;
	
	AppGlobalData() {
//		stage     = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); 
		stage     = new Stage(800, 480, true);
		sknButton = new Skin(Gdx.files.internal("data/uizimp.json"), Gdx.files.internal("data/uiskin.png"));
				
		Gdx.input.setInputProcessor(stage);
		
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Skin getSknButton() {
		return sknButton;
	}

	public void setSknButton(Skin sknButton) {
		this.sknButton = sknButton;
	}

	public void setStatePlay(int statePlay) {
		Gdx.app.log(getClass().getName() + "." + "setStatePlay" + ":" + "Enter", null);
		this.statePlay = statePlay;
		Gdx.app.log(getClass().getName() + "." + "setStatePlay" + ":" + "Exit", null);

	}

	public int getStatePlay() {
		return statePlay;
	}

	public int getStateScreen() {
		return stateScreen;
	}

	public void setStateScreen(int stateScreen) {
		this.stateScreen = stateScreen;
	}

	public StartupScreen getScreenStartup() {
		return screenStartup;
	}

	public void setScreenStartup(StartupScreen screenStartup) {
		this.screenStartup = screenStartup;
	}

	public ClassicPlay getScreenClassicPlay() {
		return screenClassicPlay;
	}

	public void setScreenClassicPlay(ClassicPlay screenClassicPlay) {
		this.screenClassicPlay = screenClassicPlay;
	}
	
}
