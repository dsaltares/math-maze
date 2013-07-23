package com.siondream.math;

import ashley.core.Engine;
import ashley.core.Entity;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Assets;
import com.siondream.core.Chrono;
import com.siondream.core.EntityFactory;
import com.siondream.core.Env;
import com.siondream.core.LanguageManager;
import com.siondream.core.SionScreen;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.LevelManager.Level;
import com.siondream.math.creators.ConditionCreator.ConditionParams;
import com.siondream.math.creators.DoorCreator.DoorParams;
import com.siondream.math.creators.ExitCreator.ExitParams;
import com.siondream.math.creators.KeyCreator.KeyParams;
import com.siondream.math.creators.OperationCreator.OperationParams;
import com.siondream.math.creators.PlayerCreator.PlayerParams;
import com.siondream.math.systems.CameraControllerSystem;
import com.siondream.math.systems.MathRenderingSystem;
import com.siondream.math.systems.PlayerControllerSystem;
import com.siondream.math.ui.ShaderLabel;

public class GameScreen extends SionScreen {

	public final static String TAG = "GameScreen";
	
	private enum State {
		HELP,
		GAME,
		PAUSE,
		VICTORY,
	}
	
	private Logger logger;
	private Level level;
	private Chrono chrono;
	
	// Game UI
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private Image imgMapBackground;
	private WidgetGroup labelsGroup;
	private ShaderLabel lblTime;
	private ShaderLabel lblPause;
	private ShaderLabel lblLevel;
	
	// Control buttons
	private ImageButton btnPause;
	private ImageButton btnMenu;
	private ImageButton btnReset;
	private Table controlTable;
	
	// Victory UI
	private ShaderLabel lblCompleted;
	private ShaderLabel lblVictoryMsg;
	private String[] victoryMsgs;
	private ImageButton btnVictoryReset;
	private ImageButton btnVictoryNext;
	private ImageButton btnVictoryMenu;
	private Table victoryTable;
	private Image[] stars;
	private TextureRegionDrawable regionStarOn;
	private TextureRegionDrawable regionStarOff;
	
	// Help UI
	private ImageButton btnClose;
	private Image imgHelp;
	private ShaderLabel lblHelp;
	
	private Sound sfxVictory;
	private Sound sfxTap;
	
	private State state;
	
	public GameScreen() {
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		chrono = new Chrono();
		
		LanguageManager lang = Env.game.getLang();
		
		victoryMsgs = new String[3];
		victoryMsgs[0] = lang.getString("Well done!");
		victoryMsgs[1] = lang.getString("Awesome!");
		victoryMsgs[2] = lang.getString("You're a star!");
	}
	
	public String getName() {
		return TAG;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		Env.game.getStage().clear();
		
		logger.info("shutting down");
		
		Engine engine = Env.game.getEngine();
		
		engine.getSystem(PlayerControllerSystem.class).cancelMovement();
		engine.removeAllEntities();
	}
	
	@Override
	public void show() {
		super.show();
		initGame();
		createUI();
		animateIn();
	}

	@Override
	public void hide() {
		super.hide();
		dispose();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
		GameEnv.game.getLabelManager().update(delta);
		lblTime.setText(Env.game.getLang().getString("Time") + ": " + chrono.getTime());
	}
	
	public void victory() {
		state = State.VICTORY;
		chrono.pause();
		
		Engine engine = Env.game.getEngine();
		engine.getSystem(PlayerControllerSystem.class).enable(false);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		
		int numStars = getStars();
		
		lblVictoryMsg.setText(victoryMsgs[numStars - 1]);
		TextBounds bounds = lblVictoryMsg.getTextBounds();
		
		if (GameEnv.debugMap.length() == 0 && level.stars < numStars) {
			GameEnv.game.getLevelManager().saveStars(level, numStars);
		}
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence();
		
		
		timeline.push(Tween.to(controlTable, ActorTweener.Position, 0.12f)
						   .target(controlTable.getX(), -controlTable.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblCompleted, ActorTweener.Position, 0.2f)
						   .target((Env.virtualWidth - lblCompleted.getWidth() * lblCompleted.getFontScaleX()) * 0.5f, lblCompleted.getY())
						   .ease(TweenEquations.easeInQuad));
		
		timeline.beginParallel();
		for (int i = 0; i < stars.length; ++i) {
			Image star = stars[i];
			
			star.setVisible(true);
			
			star.setDrawable((i < numStars) ? regionStarOn : regionStarOff);
			
			timeline.push(Tween.to(star, ActorTweener.Scale, 0.3f)
							   .target(1.0f, 1.0f)
							   .ease(TweenEquations.easeInQuad)
							   .delay(0.2f * i));
		}
		timeline.end();
		
		timeline.push(Tween.to(lblVictoryMsg, ActorTweener.Position, 0.25f)
						   .target((Env.virtualWidth - bounds.width) * 0.5f, lblVictoryMsg.getY())
						   .ease(TweenEquations.easeInQuad));
		
		timeline.push(Tween.to(victoryTable, ActorTweener.Position, 0.25f)
						   .target(victoryTable.getX(), 20.0f)
						   .ease(TweenEquations.easeInQuad));
		
		timeline.end()
				.start(Env.game.getTweenManager());
		
		if (GameEnv.soundEnabled) {
			sfxVictory.play();
		}
	}
	
	private void initGame() {
		EntityFactory factory = Env.game.getEntityFactory();
		
		Entity map = factory.createEntity(GameEnv.mapTag, GameEnv.debugMap.length() > 0 ? GameEnv.debugMap : level.file);
		MapComponent mapComponent = map.getComponent(MapComponent.class);
		
		// Relevant layers
		MapLayer objectLayer = mapComponent.map.getLayers().get(GameEnv.objectLayer);
		
		// Player entity
		PlayerParams playerParams = new PlayerParams(mapComponent.map,
													 objectLayer.getObjects().get(GameEnv.playerTag));
		Entity player = factory.createEntity(GameEnv.playerTag, playerParams);

		// Exit entity
		ExitParams exitParams = new ExitParams(mapComponent.map,
											   objectLayer.getObjects().get(GameEnv.exitTag));
		factory.createEntity(GameEnv.exitTag, exitParams);
		
		// Load conditions and operations
		MapObjects mapObjects = objectLayer.getObjects();
		int numObjects = mapObjects.getCount();
		
		ConditionParams conditionParams = new ConditionParams(mapComponent.map, null);
		OperationParams operationParams = new OperationParams(mapComponent.map, null);
		DoorParams doorParams = new DoorParams(mapComponent.map, null);
		KeyParams keyParams = new KeyParams(mapComponent.map, null);
		
		for (int i = 0; i < numObjects; ++i) {
			MapObject mapObject = mapObjects.get(i);
			MapProperties properties = mapObject.getProperties();
			String type = properties.get("type", "Unknown", String.class);
			String name = mapObject.getName();
			
			logger.info("processing " + name);
			
			if (type.equals("condition")) {
				conditionParams.object = mapObject;
				factory.createEntity(GameEnv.conditionTag, conditionParams);
			}
			else if (type.equals("operation")) {
				operationParams.object = mapObject;
				factory.createEntity(GameEnv.operationTag, operationParams);
			}
			else if (type.equals("door")) {
				doorParams.object = mapObject;
				factory.createEntity(GameEnv.doorTag, doorParams);
			}
			else if (type.equals("key")) {
				keyParams.object = mapObject;
				factory.createEntity(GameEnv.keyTag, keyParams);
			}
		}
		
		Engine engine = Env.game.getEngine();
		engine.getSystem(CameraControllerSystem.class).setTarget(player);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		engine.getSystem(PlayerControllerSystem.class).enable(false);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		LanguageManager lang = Env.game.getLang();

		imgBackground = new Image(skin, "background");
		imgLand = new Image(skin, "land");
		imgTitle = new Image(skin, "title");
		imgMapBackground = new Image(skin, "mapBackground");
		
		btnPause = new ImageButton(skin, "pause");
		btnMenu = new ImageButton(skin, "levels");
		btnReset = new ImageButton(skin, "reset");
		
		btnPause.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				onPauseClicked();
			}
		});
		
		btnMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		btnReset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(GameScreen.class);
			}
		});
		
		controlTable = new Table();
		controlTable.setSize(Env.virtualWidth, btnPause.getHeight());
		controlTable.row();
		controlTable.add(btnReset).padRight(20.0f);
		controlTable.add(btnPause).padRight(20.0f);
		controlTable.add(btnMenu).padRight(20.0f);
		controlTable.validate();
		
		lblLevel = new ShaderLabel(GameEnv.debugMap.length() > 0 ? "Test level" : lang.getString("Level %d", level.index), skin, "game");
		lblLevel.setFontScale(2.0f);
		
		lblTime = new ShaderLabel(lang.getString("Time") + ": " + "00:00", skin, "game");
		lblTime.setFontScale(2.0f);
		
		lblPause = new ShaderLabel(lang.getString("Paused"), skin, "game");
		lblPause.setFontScale(2.0f);
		
		lblCompleted = new ShaderLabel(lang.getString("Level completed!"), skin, "game");
		lblCompleted.setFontScale(3.25f);
		lblCompleted.invalidate();
		lblCompleted.layout();
		lblCompleted.setPosition(Env.virtualWidth, 850);
		
		lblVictoryMsg = new ShaderLabel("", skin, "game");
		lblVictoryMsg.setFontScale(3.0f);
		lblVictoryMsg.setPosition(Env.virtualWidth, 400.0f);
		
		btnVictoryReset = new ImageButton(skin, "reset");
		
		btnVictoryReset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(GameScreen.class);
			}
		});
		
		btnVictoryMenu = new ImageButton(skin, "levels");
		
		btnVictoryMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		btnVictoryNext = new ImageButton(skin, "next");
		
		btnVictoryNext.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				Array<Level> levels = GameEnv.game.getLevelManager().getLevels();
				int index = levels.indexOf(level, true);
				
				if (index < levels.size - 1) {
					setLevel(levels.get(index + 1));
					animateOut(GameScreen.class);
				}
				else {
					animateOut(LevelSelectionScreen.class);
				}
			}
		});
		
		victoryTable = new Table();
		victoryTable.setSize(Env.virtualWidth, btnVictoryNext.getHeight());
		victoryTable.setPosition(0.0f, -victoryTable.getHeight());
		victoryTable.row();
		victoryTable.add(btnVictoryReset).padRight(30.0f);
		victoryTable.add(btnVictoryNext).padRight(30.0f);
		victoryTable.add(btnVictoryMenu);
		victoryTable.validate();
		
		regionStarOn = new TextureRegionDrawable(new TextureRegion(atlas.findRegion("staronbig")));
		regionStarOff = new TextureRegionDrawable(new TextureRegion(atlas.findRegion("staroffbig")));
		
		stars = new Image[3];
		
		for (int i = 0; i < stars.length; ++i) {
			stars[i] = new Image(regionStarOn);
			Image star = stars[i];
			
			star.setOrigin(star.getWidth() * 0.5f, star.getHeight() * 0.5f);
			
			float padding = 20.0f;
			float startX = (GameEnv.cameraWidth - (star.getWidth() * stars.length) - (padding * (stars.length - 1))) * 0.5f;
			star.setPosition(startX + (star.getWidth() + padding) * i, 550.0f);
			star.setScale(0.0f);
			star.setVisible(false);
		}
		
		labelsGroup = new WidgetGroup();
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		btnClose = new ImageButton(skin, "close");
		imgHelp = new Image(skin, "help");
		lblHelp = new ShaderLabel("", skin, "game");
		
		btnClose.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				helpOut();
			}
		});
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(imgLand);
		stage.addActor(imgTitle);
		stage.addActor(imgMapBackground);
		stage.addActor(lblTime);
		stage.addActor(lblPause);
		stage.addActor(lblLevel);
		stage.addActor(controlTable);
		stage.addActor(victoryTable);
		stage.addActor(lblCompleted);
		stage.addActor(lblVictoryMsg);
		stage.addActor(controlTable);
		stage.addActor(btnClose);
		stage.addActor(imgHelp);
		stage.addActor(lblHelp);
		
		for (Image star : stars) {
			stage.addActor(star);
		}
		
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		lblTime.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		lblPause.setPosition(Env.virtualWidth - lblPause.getWidth() * lblPause.getFontScaleX() - 20.0f, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		lblPause.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		lblLevel.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		imgMapBackground.setY(GameEnv.cameraScreenPos.y - 5.0f);
		imgMapBackground.setColor(1.0f, 1.0f, 1.0f, 0.0f);
		controlTable.setPosition(0.0f, -controlTable.getHeight());
		imgHelp.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight - imgHelp.getHeight() - 35.0f);
		lblHelp.setVisible(false);
		lblHelp.setFontScale(1.0f);
		btnClose.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + 35.0f);
		
		chrono.reset();
		chrono.pause();
		
		Assets assets = Env.game.getAssets();
		sfxVictory = assets.get("data/sfx/victory.ogg", Sound.class);
		sfxTap = assets.get("data/sfx/tap.mp3", Sound.class);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK) {
			if (state == State.GAME) {
				pauseIn();
			}
			else {
				animateOut(LevelSelectionScreen.class);	
			}
			
			return true;
		}
		
		return false;
	}
	
	private void animateIn() {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					animateTitle();
					
					TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
					Entity mapEntity = tagSystem.getEntity(GameEnv.mapTag);
					MapComponent map = mapEntity != null ? mapEntity.getComponent(MapComponent.class) : null;
					String helpText = map.map.getProperties().get("help", "", String.class);
					
					if (!helpText.isEmpty() && (level.stars == 0 || Env.debug)) {
						lblHelp.setText(helpText);
						helpIn();
					}
					else {
						gameIn();
					}
				}
			}
		};
		
		timeline.beginSequence()
					// Animate in sequence
					.push(Tween.to(imgLand, ActorTweener.Position, 0.20f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.2f)
				      	   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight - imgTitle.getHeight() - 60.0f)
						   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgMapBackground, ActorTweener.Color, 0.2f)
					      	   .target(1.0f, 1.0f, 1.0f, 1.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblLevel, ActorTweener.Position, 0.2f)
							   .target(20.0f, lblLevel.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblTime, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth - lblTime.getWidth() * lblTime.getFontScaleX() - 20.0f, lblTime.getY())
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}

	public void animateOut(final Class<? extends SionScreen> screenType) {
		Env.game.getEngine().getSystem(MathRenderingSystem.class).setRenderMap(false);
		
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					Env.game.setScreen(screenType);
				}
			}
		};
		
		timeline.beginSequence();
		
		if (state != State.HELP) {
			timeline.push(Tween.to(controlTable, ActorTweener.Position, 0.12f)
					.target(controlTable.getX(), -controlTable.getHeight())
			   		.ease(TweenEquations.easeInOutQuad));
		}
		
		timeline.push(Tween.to(lblTime, ActorTweener.Position, 0.1f)
						   .target(Env.virtualWidth, lblTime.getY())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblLevel, ActorTweener.Position, 0.2f)
						   .target(Env.virtualWidth, lblLevel.getY())
						   .ease(TweenEquations.easeInOutQuad));
		
		if (state == State.VICTORY) {
			timeline.push(Tween.to(victoryTable, ActorTweener.Position, 0.12f)
								.target(victoryTable.getX(), -victoryTable.getHeight())
								.ease(TweenEquations.easeInOutQuad));
			
			timeline.beginParallel();
			for (int i = 0; i < stars.length; ++i) {
				Image star = stars[i];
				
				star.setVisible(true);
				
				timeline.push(Tween.to(star, ActorTweener.Scale, 0.12f)
								   .target(0.0f, 0.0f)
								   .ease(TweenEquations.easeInQuad)
								   .delay(0.08f * i));
			}
			timeline.end();
			
			timeline.push(Tween.to(lblVictoryMsg, ActorTweener.Position, 0.12f)
							   .target(Env.virtualWidth, lblVictoryMsg.getY())
							   .ease(TweenEquations.easeInOutQuad));
			
			timeline.push(Tween.to(lblCompleted, ActorTweener.Position, 0.12f)
							   .target(Env.virtualWidth, lblCompleted.getY())
							   .ease(TweenEquations.easeInOutQuad));
		}
		else if (state == State.HELP) {
			timeline.push(Tween.to(btnClose, ActorTweener.Position, 0.2f)
					   		   .target(Env.virtualWidth, btnClose.getY())
					   		   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblHelp, ActorTweener.Color, 0.2f)
					      	   .target(0.0f, 0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgHelp, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, imgHelp.getY())
							   .ease(TweenEquations.easeInOutQuad));
		}
		
		timeline.push(Tween.to(imgMapBackground, ActorTweener.Color, 0.2f)
						   .target(1.0f, 1.0f, 1.0f, 0.0f)
						   .ease(TweenEquations.easeInOutQuad));
		
		timeline.push(Tween.to(imgTitle, ActorTweener.Position, 0.12f)
						   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgLand, ActorTweener.Position, 0.12f)
						   .target(imgLand.getX(), -imgLand.getHeight())
						   .ease(TweenEquations.easeInOutQuad))		  
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private void gameIn() {
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					onGameStart();
				}
			}
		};
		
		Tween.to(controlTable, ActorTweener.Position, 0.2f)
		     .target(controlTable.getX(), 20.0f)
			 .ease(TweenEquations.easeInOutQuad)
			 .setCallback(callback)
			 .start(Env.game.getTweenManager());
	}
	
	private void animateTitle() {
		Timeline timeline = Timeline.createSequence();

		timeline.beginSequence()
				.push(Tween.to(imgTitle, ActorTweener.Scale, 0.5f)
						   .target(1.1f, 1.1f)
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgTitle, ActorTweener.Scale, 0.5f)
						   .target(1.0f, 1.0f)
						   .ease(TweenEquations.easeInOutQuad))
				.end()
				.repeat(Tween.INFINITY, 0.3f)
				.start(Env.game.getTweenManager());
	}
	
	private void onPauseClicked() {
		if (state == State.PAUSE) {
			pauseOut();
		}
		else if (state != State.VICTORY) {
			pauseIn();
		}
	}
	
	private void onGameStart() {
		Engine engine = Env.game.getEngine();
		engine.getSystem(MathRenderingSystem.class).setRenderMap(true);
		engine.getSystem(PlayerControllerSystem.class).enable(true);
		chrono.reset();
		state = State.GAME;
	}
	
	private void pauseIn() {
		chrono.pause();
		state = State.PAUSE;
		btnPause.setStyle(GameEnv.game.getSkin().get("resume", ImageButtonStyle.class));
		Engine engine = Env.game.getEngine();
		engine.getSystem(PlayerControllerSystem.class).enable(false);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(lblTime, ActorTweener.Color, 0.2f)
							   .target(0.0f, 0.0f, 0.0f, 0.0f)
						       .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblPause, ActorTweener.Color, 0.2f)
						   .target(0.0f, 0.0f, 0.0f, 1.0f)
						   .ease(TweenEquations.easeInOutQuad))	       
				.end()
				.start(Env.game.getTweenManager());
	}
	
	private void pauseOut() {
		btnPause.setStyle(GameEnv.game.getSkin().get("pause", ImageButtonStyle.class));
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					chrono.start();
					state = State.GAME;
					Engine engine = Env.game.getEngine();
					engine.getSystem(PlayerControllerSystem.class).enable(true);
					engine.getSystem(MathRenderingSystem.class).setRenderMap(true);
				}
			}
		};
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(lblPause, ActorTweener.Color, 0.2f)
					      	   .target(0.0f, 0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblTime, ActorTweener.Color, 0.2f)
							   .target(0.0f, 0.0f, 0.0f, 1.0f)
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private void helpIn() {
		state = State.HELP;
		
		lblHelp.setVisible(true);
		lblHelp.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		lblHelp.setWrap(true);
		lblHelp.setWidth(Env.virtualWidth - 40.0f);
		lblHelp.setHeight(0.0f);
		lblHelp.setAlignment(Align.center, Align.left);
		lblHelp.layout();
		lblHelp.setPosition((Env.virtualWidth - lblHelp.getWidth()) * 0.5f,
							 GameEnv.cameraScreenPos.y + GameEnv.cameraHeight - 275.0f);

		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(imgHelp, ActorTweener.Position, 0.2f)
							   .target((Env.virtualWidth - imgHelp.getWidth()) * 0.5f, imgHelp.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblHelp, ActorTweener.Color, 0.2f)
					      	   .target(0.0f, 0.0f, 0.0f, 1.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(btnClose, ActorTweener.Position, 0.2f)
							   .target((Env.virtualWidth - btnClose.getWidth()) * 0.5f, btnClose.getY())
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.start(Env.game.getTweenManager());
	}
	
	private void helpOut() {
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					lblHelp.setVisible(false);
					gameIn();
				}
			}
		};
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(btnClose, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, btnClose.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblHelp, ActorTweener.Color, 0.2f)
					      	   .target(0.0f, 0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgHelp, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, imgHelp.getY())
							   .ease(TweenEquations.easeInOutQuad))
					
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private int getStars() {
		Engine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class); 
		Entity mapEntity = tagSystem.getEntity(GameEnv.mapTag);
		MapComponent map = mapEntity.getComponent(MapComponent.class);
		String timeString = map.map.getProperties().get("time", "0", String.class);
		
		int starsTime = Integer.parseInt(timeString);
		int seconds = (int)chrono.getSeconds();
		
		if (seconds < starsTime) {
			return 3;
		}
		else if (seconds < starsTime * 1.5f) {
			return 2;
		}
		else {
			return 1;
		}
	}
}
