package com.gamemaharaja.zombie;

import org.lwjgl.opencl.CLKernel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;

public class StartupScreen {
	private AppGlobalData zimpGlobalData = null;
	private Texture 	texBackground = null;
	private SpriteBatch sprStartup;
	private Image 		imgBG = null;
	private Group		grpMainMenu = null;
	private Button[] 	btnMenuArray;
	
	
	// Drawing Constants
	final private int			MENU_START_XPOS  = 480;
	final private int			MENU_START_YPOS  = 352;
	final private int			MENU_SINGLE_SIZE = 64;

	public StartupScreen(AppGlobalData globalData) {
		// Logs are causing crash on Emulator
		//Gdx.app.log(getClass().getName() + "." + "StartupScreen" + ":" + "Enter", null);
//		sprStartup = new SpriteBatch();
		zimpGlobalData = globalData;
		
		if(grpMainMenu == null)
			grpMainMenu = new Group();
		
		// Zombie in my pocket loading screen
		if(texBackground == null) {
			 texBackground = new Texture(Gdx.files.internal("data/LoadingScreen.png"));
			 texBackground.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			 
			 // Position on left top of screen
			 imgBG = new Image("ZimpImage", texBackground);
			 imgBG.x = 0;
			 imgBG.y = 0;
			 imgBG.height = 480;
			 imgBG.touchable = true;
			 
			 //Clicklistener can't be added to image
			 /*imgBG.setClickListener(new ClickListener() {
					@Override
					public void click(Actor arg0) {
						Gdx.app.log(getClass().getName() + "." + "StartupScreen" + ":" + "click", null);
						zimpGlobalData.setStatePlay(zimpGlobalData.STATE_CLASSIC_PLAY);				
					}
				});*/
			 
			 zimpGlobalData.getStage().addActor(imgBG);			 
		}
		
		// Add Buttons for
		// 1. Play Single Game
		// 2. Zombie Stories
		// 3. How to Play
		// 4. Zombie Stats
		// 5. Exit
		
		btnMenuArray = new Button[6];
//		for(int i=0; i<btnMenuArray.length; i++) {
//			btnMenuArray[i] = new Button(zimpGlobalData.getSknButton());		
//		}

		btnMenuArray[0] = new Button("Classic Play", zimpGlobalData.getSknButton());
		btnMenuArray[0].setClickListener(new ClickListener() {
			@Override
			public void click(Actor arg0) {
				Gdx.app.log(getClass().getName() + "." + "StartupScreen" + ":" + "click", null);
				zimpGlobalData.setStatePlay(zimpGlobalData.STATE_CLASSIC_PLAY);				
			}
		});
		
		btnMenuArray[1] = new Button("Zombie Storyline", zimpGlobalData.getSknButton());
		btnMenuArray[2] = new Button("How to play", zimpGlobalData.getSknButton());
		btnMenuArray[3] = new Button("Statistics", zimpGlobalData.getSknButton());
		btnMenuArray[4] = new Button("Exit", zimpGlobalData.getSknButton());
		
		for(int i=0; i<5; i++) {
			btnMenuArray[i].y = MENU_START_YPOS - i*MENU_SINGLE_SIZE;
			btnMenuArray[i].touchable = true;
			grpMainMenu.addActor(btnMenuArray[i]);
		}
		
		grpMainMenu.x = MENU_START_XPOS;
		zimpGlobalData.getStage().addActor(grpMainMenu);
		// Logs are causing crash on Emulator
		//Gdx.app.log(getClass().getName() + "." + "StartupScreen" + ":" + "Exit", null);
		
	}
	
	
	// Render the initial screen
	public void render() {
//		 Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
	}
		 
	
	
	
	
}
