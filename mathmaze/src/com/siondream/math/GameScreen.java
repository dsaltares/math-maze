package com.siondream.math;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.PooledEngine;
import ashley.utils.IntMap.Values;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.AbsoluteFileHandleResolver;
import com.siondream.core.Assets;
import com.siondream.core.Chrono;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.ShaderComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.LevelManager.Level;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;
import com.siondream.math.systems.CameraControllerSystem;
import com.siondream.math.systems.MathRenderingSystem;
import com.siondream.math.systems.PlayerControllerSystem;
import com.siondream.math.ui.ShaderButton;
import com.siondream.math.ui.ShaderLabel;
import com.siondream.math.ui.ShaderButton.ShaderButtonStyle;

public class GameScreen extends SionScreen {

	public final static String TAG = "GameScreen";
	
	private enum State {
		GAME,
		PAUSE,
		VICTORY,
	}
	
	private Logger logger;
	private TmxMapLoader mapLoader;
	private Level level;
	private Chrono chrono;
	
	// Fonts
	private Texture fontTexture;
	private BitmapFont fontMap;
	private ShaderProgram fontShader;
	
	// Game UI
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private Image imgMapBackground;
	private WidgetGroup labelsGroup;
	private ShaderButton btnPause;
	private ShaderLabel lblTime;
	private ShaderLabel lblLevel;
	
	// Pause UI
	private Table pauseTable;
	private ShaderButton btnReset;
	private ShaderButton btnBack;
	
	// Victory UI
	private ShaderLabel lblCompleted;
	private ShaderButton btnRetry;
	private ShaderButton btnNext;
	private Table victoryTable;
	private Image[] stars;
	private TextureRegionDrawable regionStarOn;
	private TextureRegionDrawable regionStarOff;
	
	private State state;
	
	public GameScreen() {
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		chrono = new Chrono();
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
		fontMap.dispose();
		fontTexture.dispose();
		
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
		lblTime.setText("Time " + chrono.getTime());
	}
	
	private void initGame() {
		Engine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		
		// Font stuff
		fontTexture = new Texture(Gdx.files.internal("data/ui/chicken.png"), true);
		fontTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);
		fontMap = new BitmapFont(Gdx.files.internal("data/ui/chicken.fnt"), new TextureRegion(fontTexture), false);
		fontMap.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		fontShader = new ShaderProgram(Gdx.files.internal("data/ui/font.vert"), 
													 Gdx.files.internal("data/ui/font.frag"));
		
		
		// Map entity
		if (level.debug) {
			mapLoader = new TmxMapLoader(new AbsoluteFileHandleResolver());
		}
		else {
			mapLoader = new TmxMapLoader();
		}
		
		Entity map = new Entity();
		MapComponent mapComponent = new MapComponent();
		mapComponent.map = mapLoader.load(level.file);
		map.add(mapComponent);
		engine.addEntity(map);
		tagSystem.setTag(map, GameEnv.mapTag);
		
		// Relevant layers
		MapLayer objectLayer = mapComponent.map.getLayers().get(GameEnv.objectLayer);
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)mapComponent.map.getLayers().get(GameEnv.backgroundLayer);
		RectangleMapObject rectangleObject = (RectangleMapObject)objectLayer.getObjects().get(GameEnv.playerTag);
		MapProperties properties = rectangleObject.getProperties();
		
		// Player entity
		Entity player = new Entity();
		TextureComponent texture = new TextureComponent();
		texture.region = new TextureRegion(Env.game.getAssets().get("data/player.png", Texture.class));
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		ValueComponent value = new ValueComponent();
		FontComponent font = new FontComponent();
		ShaderComponent shader = new ShaderComponent();
		player.add(position);
		player.add(texture);
		player.add(transform);
		player.add(value);
		player.add(font);
		player.add(shader);
		engine.addEntity(player);
		tagSystem.setTag(player, GameEnv.playerTag);
		
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = fontMap;
		shader.shader = fontShader;
		value.value = Integer.parseInt(properties.get("value", "0", String.class));
		
		// Exit entity
		Entity exit = new Entity();
		texture = new TextureComponent();
		texture.region = new TextureRegion(Env.game.getAssets().get("data/exit.png", Texture.class));
		position = new GridPositionComponent();
		transform = new TransformComponent();
		exit.add(transform);
		exit.add(texture);
		exit.add(position);
		engine.addEntity(exit);
		tagSystem.setTag(exit, GameEnv.exitTag);
		rectangleObject = (RectangleMapObject)objectLayer.getObjects().get(GameEnv.exitTag);
		rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		
		// Load conditions and operations
		MapObjects mapObjects = objectLayer.getObjects();
		int numObjects = mapObjects.getCount();
		
		for (int i = 0; i < numObjects; ++i) {
			MapObject mapObject = mapObjects.get(i);
			properties = mapObject.getProperties();
			String type = properties.get("type", "Unknown", String.class);
			String name = mapObject.getName();
			String[] parts = name.split(":");
			
			logger.info("processing " + name);
			
			if (type.equals("condition")) {
				
				if (parts.length < 2 || parts.length % 2 != 0) {
					logger.error("invalid condition entity " + name);
					continue;
				}
				
				Array<Condition> conditions = new Array<Condition>();
				
				for (int j = 0; j < parts.length; j = j + 2) {
					conditions.add(new Condition(parts[j], Integer.parseInt(parts[j + 1])));
				}
				
				Entity condition = new Entity();
				texture = new TextureComponent();
				texture.region = new TextureRegion(Env.game.getAssets().get("data/checkpoint.png", Texture.class));
				position = new GridPositionComponent();
				transform = new TransformComponent();
				font = new FontComponent();
				shader = new ShaderComponent();
				ConditionComponent conditionComponent = new ConditionComponent();
				conditionComponent.conditions = conditions;
				condition.add(texture);
				condition.add(position);
				condition.add(transform);
				condition.add(conditionComponent);
				condition.add(font);
				condition.add(shader);
				engine.addEntity(condition);
				groupSystem.register(condition, GameEnv.conditionsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				font.font = fontMap;
				shader.shader = fontShader;
			}
			else if (type.equals("operation")) {

				if (parts.length != 2) {
					logger.error("invalid operation entity " + name);
					continue;
				}
				
				Operation operation = new Operation(parts[0], Integer.parseInt(parts[1]));
				
				Entity operationEntity = new Entity();
				texture = new TextureComponent();
				position = new GridPositionComponent();
				transform = new TransformComponent();
				OperationComponent operationComponent = new OperationComponent();
				font = new FontComponent();
				shader = new ShaderComponent();
				operationComponent.operation = operation;
				operationComponent.persist = Boolean.parseBoolean(properties.get("persist", "false", String.class));
				
				
				if (operationComponent.persist) {
					texture.region = new TextureRegion(Env.game.getAssets().get("data/operation-persist.png", Texture.class));
				}
				else {
					texture.region = new TextureRegion(Env.game.getAssets().get("data/operation.png", Texture.class));
				}
				
				operationEntity.add(texture);
				operationEntity.add(position);
				operationEntity.add(operationComponent);
				operationEntity.add(transform);
				operationEntity.add(font);
				operationEntity.add(shader);
				engine.addEntity(operationEntity);
				groupSystem.register(operationEntity, GameEnv.operationsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				font.font = fontMap;
				shader.shader = fontShader;
			}
		}
		
		engine.getSystem(CameraControllerSystem.class).setTarget(player);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		engine.getSystem(PlayerControllerSystem.class).enable(false);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Assets assets = Env.game.getAssets();
		
		imgBackground = new Image(assets.get("data/ui/background.png", Texture.class));
		imgLand = new Image(assets.get("data/ui/land.png", Texture.class));
		imgTitle = new Image(assets.get("data/ui/title.png", Texture.class));
		imgMapBackground = new Image(assets.get("data/mapBackground.png", Texture.class));
		
		Texture upText = assets.get("data/ui/upButton.png", Texture.class);
		upText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		Texture downText = assets.get("data/ui/downButton.png", Texture.class);
		downText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegionDrawable upButton = new TextureRegionDrawable(new TextureRegion(upText));
		TextureRegionDrawable downButton = new TextureRegionDrawable(new TextureRegion(downText));
		
		ShaderButtonStyle buttonStyle = new ShaderButtonStyle();
		buttonStyle.down = downButton;
		buttonStyle.up = upButton;
		buttonStyle.font = fontMap;
		buttonStyle.shader = fontShader;
		buttonStyle.backGroundColor = Color.WHITE;
		buttonStyle.fontColor = Color.BLACK;
		
		btnPause = new ShaderButton("Pause", buttonStyle);
		btnPause.setScale(3.25f);
		btnPause.setWidth(400.0f);
		btnPause.setHeight(125.0f);
		
		btnPause.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onPauseClicked();
			}
		});
		
		LabelStyle labelStyle = new LabelStyle(fontMap, Color.BLACK);
		
		lblLevel = new ShaderLabel(level.name, labelStyle, fontShader);
		lblLevel.setFontScale(2.0f);
		
		lblTime = new ShaderLabel("Time: 00:00", labelStyle, fontShader);
		lblTime.setFontScale(2.0f);
		
		
		btnReset = new ShaderButton("Reset", buttonStyle);
		btnReset.setScale(4.25f);
		btnReset.validate();
		
		btnReset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(GameScreen.class);
			}
		});
		
		btnBack = new ShaderButton("Menu", buttonStyle);
		btnBack.setScale(4.25f);
		btnBack.validate();
		
		btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(LevelSelectionScreen.class);
			}
		});

		pauseTable = new Table();
		pauseTable.setSize(GameEnv.cameraWidth, GameEnv.cameraHeight);
		pauseTable.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y);
		pauseTable.row();
		pauseTable.add(btnReset).width(650.0f).height(150.0f).center().padBottom(30.0f);
		pauseTable.row();
		pauseTable.add(btnBack).width(650.0f).height(150.0f).center();
		pauseTable.validate();
		
		
		lblCompleted = new ShaderLabel("Level completed!", labelStyle, fontShader);
		lblCompleted.setFontScale(3.25f);
		lblCompleted.invalidate();
		lblCompleted.layout();
		lblCompleted.setPosition(Env.virtualWidth, 850);
		
		btnRetry = new ShaderButton("Retry", buttonStyle);
		btnRetry.setScale(3.25f);
		
		btnRetry.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(GameScreen.class);
			}
		});
		
		btnNext = new ShaderButton("Next", buttonStyle);
		btnNext.setScale(3.25f);
		
		btnNext.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
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
		victoryTable.setSize(GameEnv.cameraWidth, 200.0f);
		victoryTable.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y);
		victoryTable.row();
		victoryTable.add(btnRetry).width(315.0f).height(150.0f).padRight(30.0f);
		victoryTable.add(btnNext).width(315.0f).height(150.0f);
		victoryTable.validate();
		
		regionStarOn = new TextureRegionDrawable(new TextureRegion(assets.get("data/ui/staronbig.png", Texture.class)));
		regionStarOff = new TextureRegionDrawable(new TextureRegion(assets.get("data/ui/staroffbig.png", Texture.class)));
		
		stars = new Image[3];
		
		for (int i = 0; i < stars.length; ++i) {
			stars[i] = new Image(regionStarOn);
			Image star = stars[i];
			
			star.setOrigin(star.getWidth() * 0.5f, star.getHeight() * 0.5f);
			
			float padding = 20.0f;
			float startX = (GameEnv.cameraWidth - (star.getWidth() * stars.length) - (padding * (stars.length - 1))) * 0.5f;
			star.setPosition(startX + (star.getWidth() + padding) * i, 600.0f);
			star.setScale(0.0f);
			star.setVisible(false);
		}
		
		labelsGroup = new WidgetGroup();
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(imgLand);
		stage.addActor(imgTitle);
		stage.addActor(imgMapBackground);
		stage.addActor(btnPause);
		stage.addActor(lblTime);
		stage.addActor(lblLevel);
		stage.addActor(pauseTable);
		stage.addActor(victoryTable);
		stage.addActor(lblCompleted);
		
		for (Image star : stars) {
			stage.addActor(star);
		}
		
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		btnPause.setPosition((Env.virtualWidth - btnPause.getWidth()) * 0.5f, -btnPause.getHeight());
		lblTime.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		lblLevel.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		imgMapBackground.setY(GameEnv.cameraScreenPos.y - 5.0f);
		imgMapBackground.setColor(1.0f, 1.0f, 1.0f, 0.0f);
		
		chrono.reset();
		chrono.pause();
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
					onGameStart();
				}
			}
		};
		
		timeline.beginSequence()
					// Animate in sequence
					.push(Tween.to(imgLand, ActorTweener.Position, 0.5f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.4f)
				      	   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight - imgTitle.getHeight() - 60.0f)
						   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgMapBackground, ActorTweener.Color, 0.4f)
					      	   .target(1.0f, 1.0f, 1.0f, 1.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblLevel, ActorTweener.Position, 0.4f)
							   .target(20.0f, lblLevel.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblTime, ActorTweener.Position, 0.4f)
							   .target(Env.virtualWidth - lblTime.getWidth() * lblTime.getFontScaleX() - 20.0f, lblTime.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(btnPause, ActorTweener.Position, 0.40f)
							   .target((Env.virtualWidth - btnPause.getWidth()) * 0.5f, 50.0f)
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
		
		timeline.push(Tween.to(btnPause, ActorTweener.Position, 0.20f)
				   		   .target((Env.virtualWidth - btnPause.getWidth()) * 0.5f, -btnPause.getHeight())
				   		   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblTime, ActorTweener.Position, 0.1f)
						   .target(Env.virtualWidth, lblTime.getY())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblLevel, ActorTweener.Position, 0.2f)
						   .target(Env.virtualWidth, lblLevel.getY())
						   .ease(TweenEquations.easeInOutQuad));
		
		if (state == State.PAUSE) {
			timeline.push(Tween.to(pauseTable, ActorTweener.Position, 0.2f)
					.target(Env.virtualWidth, pauseTable.getY())
					.ease(TweenEquations.easeInOutQuad));
		}
		else {
			
			if (state == State.VICTORY) {
				timeline.push(Tween.to(victoryTable, ActorTweener.Position, 0.2f)
									.target(Env.virtualWidth, victoryTable.getY())
									.ease(TweenEquations.easeInOutQuad));
				
				timeline.beginParallel();
				for (int i = 0; i < stars.length; ++i) {
					Image star = stars[i];
					
					star.setVisible(true);
					
					timeline.push(Tween.to(star, ActorTweener.Scale, 0.16f)
									   .target(0.0f, 0.0f)
									   .ease(TweenEquations.easeInQuad)
									   .delay(0.08f * i));
				}
				timeline.end();
				
				timeline.push(Tween.to(lblCompleted, ActorTweener.Position, 0.2f)
						.target(Env.virtualWidth, lblCompleted.getY())
						.ease(TweenEquations.easeInOutQuad));
			}
			
			timeline.push(Tween.to(imgMapBackground, ActorTweener.Color, 0.4f)
							   .target(1.0f, 1.0f, 1.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad));
		}
		
		timeline.push(Tween.to(imgTitle, ActorTweener.Position, 0.25f)
						   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgLand, ActorTweener.Position, 0.20f)
						   .target(imgLand.getX(), -imgLand.getHeight())
						   .ease(TweenEquations.easeInOutQuad))		  
				.end()
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
		btnPause.setText("Resume");
		Engine engine = Env.game.getEngine();
		engine.getSystem(PlayerControllerSystem.class).enable(false);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(lblTime, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, lblTime.getY())
						       .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblLevel, ActorTweener.Position, 0.2f)
						   .target(Env.virtualWidth, lblLevel.getY())
						   .ease(TweenEquations.easeInOutQuad))	       
					.push(Tween.to(imgMapBackground, ActorTweener.Color, 0.4f)
					      	   .target(1.0f, 1.0f, 1.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(pauseTable, ActorTweener.Position, 0.4f)
							   .target(GameEnv.cameraScreenPos.x, pauseTable.getY())
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.start(Env.game.getTweenManager());
	}
	
	private void pauseOut() {
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					chrono.start();
					state = State.GAME;
					btnPause.setText("Pause");
					Engine engine = Env.game.getEngine();
					engine.getSystem(PlayerControllerSystem.class).enable(true);
					engine.getSystem(MathRenderingSystem.class).setRenderMap(true);
				}
			}
		};
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(pauseTable, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, pauseTable.getY())
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
	
	public void victory() {
		state = State.VICTORY;
		chrono.pause();
		
		Engine engine = Env.game.getEngine();
		engine.getSystem(PlayerControllerSystem.class).enable(false);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		
		int numStars = getStars();
		
		if (level.stars < numStars && !level.debug) {
			GameEnv.game.getLevelManager().saveStars(level, numStars);
		}
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence();
		
		
		timeline.push(Tween.to(btnPause, ActorTweener.Position, 0.20f)
						   .target((Env.virtualWidth - btnPause.getWidth()) * 0.5f, -btnPause.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblCompleted, ActorTweener.Position, 0.4f)
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
		
		
		timeline.push(Tween.to(victoryTable, ActorTweener.Position, 0.4f)
						   .target((Env.virtualWidth - victoryTable.getWidth()) * 0.5f, victoryTable.getY())
						   .ease(TweenEquations.easeInQuad));
		
		timeline.end()
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
