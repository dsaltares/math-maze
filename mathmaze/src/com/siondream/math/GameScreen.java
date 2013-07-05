package com.siondream.math;

import ashley.core.Engine;
import ashley.core.Entity;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.AbsoluteFileHandleResolver;
import com.siondream.core.Chrono;
import com.siondream.core.Env;
import com.siondream.core.ShaderManager;
import com.siondream.core.SionScreen;
import com.siondream.core.entity.components.ColorComponent;
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
import com.siondream.math.ui.ShaderLabel;

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
	private ImageButton btnVictoryReset;
	private ImageButton btnVictoryNext;
	private ImageButton btnVictoryMenu;
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
		Skin skin = GameEnv.game.getSkin();
		ShaderManager shaderManager = Env.game.getShaderManager();
		TextureAtlas atlas = skin.getAtlas();
		
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
		texture.region = new TextureRegion(atlas.findRegion("player"));
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		ValueComponent value = new ValueComponent();
		FontComponent font = new FontComponent();
		ShaderComponent shader = new ShaderComponent();
		ColorComponent color = new ColorComponent();
		player.add(position);
		player.add(texture);
		player.add(transform);
		player.add(value);
		player.add(font);
		player.add(shader);
		player.add(color);
		engine.addEntity(player);
		tagSystem.setTag(player, GameEnv.playerTag);
		color.color = Color.BLACK.cpy();
		
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = skin.getFont("gameFont");
		shader.shader = shaderManager.get("font");
		value.value = Integer.parseInt(properties.get("value", "0", String.class));
		
		// Exit entity
		Entity exit = new Entity();
		texture = new TextureComponent();
		texture.region = new TextureRegion(atlas.findRegion("exit"));
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
				texture.region = new TextureRegion(atlas.findRegion("checkpoint"));
				position = new GridPositionComponent();
				transform = new TransformComponent();
				font = new FontComponent();
				shader = new ShaderComponent();
				color = new ColorComponent();
				ConditionComponent conditionComponent = new ConditionComponent();
				conditionComponent.conditions = conditions;
				condition.add(texture);
				condition.add(position);
				condition.add(transform);
				condition.add(conditionComponent);
				condition.add(font);
				condition.add(shader);
				condition.add(color);
				engine.addEntity(condition);
				groupSystem.register(condition, GameEnv.conditionsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				transform.position.z = 100.0f;
				font.font = skin.getFont("gameFont");
				shader.shader = shaderManager.get("font");
				color.color = Color.BLACK.cpy();
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
				color = new ColorComponent();
				operationComponent.operation = operation;
				operationComponent.persist = Boolean.parseBoolean(properties.get("persist", "false", String.class));
				
				
				if (operationComponent.persist) {
					texture.region = new TextureRegion(atlas.findRegion("operation-persist"));
				}
				else {
					texture.region = new TextureRegion(atlas.findRegion("operation"));
				}
				
				operationEntity.add(texture);
				operationEntity.add(position);
				operationEntity.add(operationComponent);
				operationEntity.add(transform);
				operationEntity.add(font);
				operationEntity.add(shader);
				operationEntity.add(color);
				engine.addEntity(operationEntity);
				groupSystem.register(operationEntity, GameEnv.operationsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				font.font = skin.getFont("gameFont");
				shader.shader = shaderManager.get("font");;
				color.color = Color.BLACK.cpy();
			}
		}
		
		engine.getSystem(CameraControllerSystem.class).setTarget(player);
		engine.getSystem(MathRenderingSystem.class).setRenderMap(false);
		engine.getSystem(PlayerControllerSystem.class).enable(false);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		ShaderManager shaderManager = Env.game.getShaderManager();
		
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
				onPauseClicked();
			}
		});
		
		btnMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		btnReset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
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
		
		LabelStyle labelStyle = skin.get("game", LabelStyle.class);
		ShaderProgram shader = shaderManager.get("font");
		
		lblLevel = new ShaderLabel(level.name, labelStyle, shader);
		lblLevel.setFontScale(2.0f);
		
		lblTime = new ShaderLabel("Time: 00:00", labelStyle, shader);
		lblTime.setFontScale(2.0f);
		
		lblPause = new ShaderLabel("Paused", labelStyle, shader);
		lblPause.setFontScale(2.0f);
		
		lblCompleted = new ShaderLabel("Level completed!", labelStyle, shader);
		lblCompleted.setFontScale(3.25f);
		lblCompleted.invalidate();
		lblCompleted.layout();
		lblCompleted.setPosition(Env.virtualWidth, 850);
		
		btnVictoryReset = new ImageButton(skin, "reset");
		
		btnVictoryReset.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(GameScreen.class);
			}
		});
		
		btnVictoryMenu = new ImageButton(skin, "levels");
		
		btnVictoryMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		btnVictoryNext = new ImageButton(skin, "next");
		
		btnVictoryNext.addListener(new ClickListener() {
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
		stage.addActor(controlTable);
		
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
					.push(Tween.to(controlTable, ActorTweener.Position, 0.2f)
							   .target(controlTable.getX(), 20.0f)
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
		
		timeline.push(Tween.to(controlTable, ActorTweener.Position, 0.12f)
				   		   .target(controlTable.getX(), -controlTable.getHeight())
				   		   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblTime, ActorTweener.Position, 0.1f)
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
			
			timeline.push(Tween.to(lblCompleted, ActorTweener.Position, 0.12f)
					.target(Env.virtualWidth, lblCompleted.getY())
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
		
		
		timeline.push(Tween.to(victoryTable, ActorTweener.Position, 0.25f)
						   .target(victoryTable.getX(), 20.0f)
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
