package com.gamemaharaja.zombie;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class DesktopStarter {

	public static void main(String[] args) {
		new JoglApplication(new ZombieApp(), 
				"Zombie in my Pocket",
				800,480, false);
	}
}
