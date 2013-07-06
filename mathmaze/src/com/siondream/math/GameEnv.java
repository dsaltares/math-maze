package com.siondream.math;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
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
	public static float cameraShakeTime;
	public static int cameraShakeRepetitions;
	public static float cameraWidth;
	public static float cameraHeight;
	public static Vector3 cameraScreenPos;
	
	public static float playerMoveCooldown;
	public static float playerMoveTime;
	public static int playerMaxValue;
	public static int playerVibrateTimeMs;
	
	private static String TAG = "GameGlobals";
	private static Settings settings;
	private static Logger logger = new Logger(TAG, Logger.INFO);

	public static int mathLabelsMaxCount;
	public static float mathLabelsSpawnTime;
	public static float mathLabelsMaxTweenTime;
	public static float mathLabelsMinTweenTime;
	public static Color mathLabelsColor;
	
	public static Color starsOperationColor;

	public static void init(MathMaze game) {
		logger.info("initialising");
		
		GameEnv.game = game;
		
		settings = new Settings("data/config/gameGlobals.xml");
		
		debugCamera = settings.getBoolean("debugCamera", false);
		cameraScrollSpeed = settings.getFloat("cameraScrollSpeed", 2.0f);
		cameraZoomSpeed = settings.getFloat("cameraZoomSpeed", 1.0f);
		cameraShakeTime = settings.getFloat("cameraShakeTime", 0.1f);
		cameraShakeRepetitions = settings.getInt("cameraShakeRepetitions", 1);
		cameraWidth = settings.getFloat("cameraWidth", Env.virtualWidth);
		cameraHeight = settings.getFloat("cameraHeight", Env.virtualHeight);
		cameraScreenPos = settings.getVector("cameraScreenPos", Vector3.Zero.cpy());
		
		playerMoveCooldown = settings.getFloat("playerMoveCooldown", 0.2f);
		playerMoveTime = settings.getFloat("playerMoveTime", 0.f);
		playerMaxValue = settings.getInt("playerMaxValue", 1000);
		playerVibrateTimeMs = settings.getInt("playerVibrateTimeMs", 250);
		
		mathLabelsMaxCount = settings.getInt("mathLabelMaxCount", 20);
		mathLabelsSpawnTime = settings.getFloat("mathLabelSpawnTime", 0.5f);
		mathLabelsMaxTweenTime = settings.getFloat("mathLabelMaxTweenTime", 10.0f);
		mathLabelsMinTweenTime = settings.getFloat("mathLabelMinTweenTime", 7.0f);
		mathLabelsColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
		
		starsOperationColor = new Color(0.6f, 0.8f, 0.0f, 1.0f);
	}
}
