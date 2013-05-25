package com.siondream.math;

import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.Settings;

public class GameEnv {
	public static MathMaze game;
	
	public static final String mapTag = "map";
	public static final String playerTag = "player";
	public static final String exitTag = "exit";
	public static final String conditionsGroup = "conditions";
	public static final String operationsGroup = "operations";
	public static final String objectLayer = "objects";
	public static final String backgroundLayer = "background";
	
	public static boolean debugCamera;
	public static float cameraScrollSpeed;
	public static float cameraZoomSpeed;
	
	public static float playerMoveCooldown;
	public static float playerMoveTime;
	
	private static String TAG = "GameGlobals";
	private static Settings settings;
	private static Logger logger = new Logger(TAG, Logger.INFO);
	
	public static void init(MathMaze game) {
		logger.info("initialising");
		
		Env.game = game;
		
		settings = new Settings("data/config/gameGlobals.xml");
		
		debugCamera = settings.getBoolean("debugCamera", false);
		cameraScrollSpeed = settings.getFloat("cameraScrollSpeed", 2.0f);
		cameraZoomSpeed = settings.getFloat("cameraZoomSpeed", 1.0f);
		
		playerMoveCooldown = settings.getFloat("playerMoveCooldown", 0.2f);
		playerMoveTime = settings.getFloat("playerMoveTime", 0.f);
	}
}
