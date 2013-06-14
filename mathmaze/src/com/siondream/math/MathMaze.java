package com.siondream.math;

import ashley.core.Engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.siondream.core.Env;
import com.siondream.core.SionGame;
import com.siondream.core.entity.systems.AnimationSystem;
import com.siondream.core.entity.systems.DisposingSystem;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.core.entity.systems.PhysicsSystem;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.systems.CameraControllerSystem;
import com.siondream.math.systems.CheckpointSystem;
import com.siondream.math.systems.MathRenderingSystem;
import com.siondream.math.systems.PlayerControllerSystem;


public class MathMaze extends SionGame {
	
	private AnimationSystem animationSystem;
	private MathRenderingSystem renderingSystem;
	private PhysicsSystem physicsSystem;
	private DisposingSystem disposingSystem;
	private TagSystem tagSystem;
	private GroupSystem groupSystem;
	private CameraControllerSystem cameraControllerSystem;
	private PlayerControllerSystem playerControllerSystem;
	private CheckpointSystem checkpointSystem;
	private Preferences preferences;
	private LevelManager levelManager;
	private int currentLevel;
	
	@Override
	public void create() {		
		super.create();
		
		GameEnv.init(this);
		
		preferences = Gdx.app.getPreferences("MathMazePrefs");
		
		levelManager = new LevelManager("data/levels/levels.xml", preferences);
		this.currentLevel = 0;

		Engine engine = Env.game.getEngine();
		
		animationSystem = new AnimationSystem();
		renderingSystem = new MathRenderingSystem();
		physicsSystem = new PhysicsSystem();
		disposingSystem = new DisposingSystem();
		tagSystem = new TagSystem();
		groupSystem = new GroupSystem();
		cameraControllerSystem = new CameraControllerSystem();
		playerControllerSystem = new PlayerControllerSystem();
		checkpointSystem = new CheckpointSystem();
				
		playerControllerSystem.priority = 1;
		cameraControllerSystem.priority = 2;
		checkpointSystem.priority = 3;
		animationSystem.priority = 4;
		physicsSystem.priority = 5;
		disposingSystem.priority = 6;
		renderingSystem.priority = 7;
		tagSystem.priority = 8;
		groupSystem.priority = 9;
		
		engine.addSystem(animationSystem);
		engine.addSystem(renderingSystem);
		engine.addSystem(disposingSystem);
		engine.addSystem(physicsSystem);
		engine.addSystem(tagSystem);
		engine.addSystem(groupSystem);
		engine.addSystem(cameraControllerSystem);
		engine.addSystem(playerControllerSystem);
		engine.addSystem(checkpointSystem);
		
		this.addScreen(new GameScreen());
		this.addScreen(new LevelSelectionScreen());
		this.addScreen(new MenuScreen());
		this.setScreen(MenuScreen.class);
	}

	public Preferences getPreferences() {
		return preferences;
	}
	
	public LevelManager getLevelManager() {
		return levelManager;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		renderingSystem.dispose();
	}
}