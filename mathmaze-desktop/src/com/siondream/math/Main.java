package com.siondream.math;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.siondream.core.Env;
import com.siondream.math.MathMaze;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "mathmaze";
		cfg.useGL20 = true;
		cfg.width = 540;
		cfg.height = 960;
		
		Env.platform = new DesktopResolver();
		
		new LwjglApplication(new MathMaze(), cfg);
	}
}
