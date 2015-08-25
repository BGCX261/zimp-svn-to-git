package com.gamemaharaja.zombie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ZombieApp implements ApplicationListener {
	private int SCREEN_START = 1; 
	
	private AppGlobalData zimpGlobalData;
	private StartupScreen zimpStartup;
	private int appScreen = SCREEN_START;
	private boolean rendered = false;

	@Override
	public void create() {
		Gdx.app.log("Enter", "ApplicationListener.create");
		
		zimpGlobalData = new AppGlobalData();
		
		// Make the initial screen
		zimpStartup = new StartupScreen(zimpGlobalData);
		rendered = false;
		Gdx.app.log("Exit", "ApplicationListener.create");
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
//		Gdx.app.log("Enter", "ApplicationListener.render");

//		zimpStartup.render();
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		zimpGlobalData.getStage().act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		zimpGlobalData.getStage().draw();

		//Gdx.gl.glClearColor(1,0,0,1);
		//Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//		Gdx.app.log("Exit", "ApplicationListener.render");
		
	}

	@Override	
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

}
