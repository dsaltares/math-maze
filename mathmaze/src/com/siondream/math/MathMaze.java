package com.siondream.math;

import ashley.core.Engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.siondream.core.Assets;
import com.siondream.core.EntityFactory;
import com.siondream.core.Env;
import com.siondream.core.SionGame;
import com.siondream.core.entity.systems.AnimationSystem;
import com.siondream.core.entity.systems.DisposingSystem;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.core.entity.systems.ParticleEffectSystem;
import com.siondream.core.entity.systems.PhysicsSystem;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.creators.ConditionCreator;
import com.siondream.math.creators.DoorCreator;
import com.siondream.math.creators.ExitCreator;
import com.siondream.math.creators.KeyCreator;
import com.siondream.math.creators.MapCreator;
import com.siondream.math.creators.OperationCreator;
import com.siondream.math.creators.PlayerCreator;
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
	private ParticleEffectSystem particleSystem;
	private Preferences preferences;
	private LevelManager levelManager;
	private FallingLabelManager labelManager;
	
	private Music song;

	private Skin skin;
	private Skin skinNearest;
	
	@Override
	public void create() {		
		super.create();
		
		GameEnv.init(this);
		
		Assets assets = Env.game.getAssets();
		skin = assets.get("data/ui.json", Skin.class);
		skinNearest = assets.get("data/uiNearest.json", Skin.class);
		
		preferences = Gdx.app.getPreferences("MathMazePrefs");
		
		levelManager = new LevelManager("data/levels/levels.xml", preferences);

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
		particleSystem = new ParticleEffectSystem();
				
		playerControllerSystem.priority = 1;
		cameraControllerSystem.priority = 2;
		checkpointSystem.priority = 3;
		particleSystem.priority = 4;
		animationSystem.priority = 5;
		physicsSystem.priority = 6;
		disposingSystem.priority = 7;
		renderingSystem.priority = 8;
		tagSystem.priority = 9;
		groupSystem.priority = 10;
		
		engine.addSystem(animationSystem);
		engine.addSystem(renderingSystem);
		engine.addSystem(disposingSystem);
		engine.addSystem(physicsSystem);
		engine.addSystem(tagSystem);
		engine.addSystem(groupSystem);
		engine.addSystem(cameraControllerSystem);
		engine.addSystem(playerControllerSystem);
		engine.addSystem(checkpointSystem);
		engine.addSystem(particleSystem);
		
		this.addScreen(new GameScreen());
		this.addScreen(new LevelSelectionScreen());
		this.addScreen(new MenuScreen());
		this.addScreen(new SplashScreen());
		this.addScreen(new CreditsScreen());
		
		if (GameEnv.debugMap.length() > 0) {
			this.setScreen(GameScreen.class);
		}
		else {
			this.setScreen(SplashScreen.class);	
		}
		
		TextureRegion fontRegion = skinNearest.getFont("gameFont").getRegion(); 
		Texture fontTexture = fontRegion.getTexture();
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		fontRegion = skin.getFont("gameFont").getRegion(); 
		fontTexture = fontRegion.getTexture();
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		labelManager = new FallingLabelManager();
		
		EntityFactory entityFactory = getEntityFactory();
		entityFactory.addCreator(GameEnv.playerTag, new PlayerCreator());
		entityFactory.addCreator(GameEnv.exitTag, new ExitCreator());
		entityFactory.addCreator(GameEnv.operationTag, new OperationCreator());
		entityFactory.addCreator(GameEnv.conditionTag, new ConditionCreator());
		entityFactory.addCreator(GameEnv.mapTag, new MapCreator());
		entityFactory.addCreator(GameEnv.doorTag, new DoorCreator());
		entityFactory.addCreator(GameEnv.keyTag, new KeyCreator());
		
		GameEnv.soundEnabled = preferences.getBoolean("soundEnabled", true);
		
		song = assets.get("data/music/song.mp3", Music.class);
	}

	public Preferences getPreferences() {
		return preferences;
	}
	
	public LevelManager getLevelManager() {
		return levelManager;
	}
	
	public FallingLabelManager getLabelManager() {
		return labelManager;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public Skin getSkinNearest() {
		return skinNearest;
	}
	
	public Music getMusic() {
		return song;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		labelManager.dispose();
		renderingSystem.dispose();
		skin.dispose();
	}
}