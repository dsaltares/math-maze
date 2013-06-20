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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Assets;
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
	
	private Logger logger;
	private TmxMapLoader mapLoader;
	private Level level;
	
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private Image imgMapBackground;
	private ShaderButton btnPause;
	private ShaderLabel lblTime;
	private Texture fontTexture;
	private BitmapFont fontMap;
	private ShaderProgram fontShader;
	
	public GameScreen() {
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		mapLoader = new TmxMapLoader();
	}
	
	public String getName() {
		return TAG;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
	
	public void reset() {
		hide();
		show();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		Env.game.getStage().clear();
		fontMap.dispose();
		fontTexture.dispose();
		
		logger.info("shutting down");
		
		Env.game.getEngine().getSystem(PlayerControllerSystem.class).cancelMovement();
		
		PooledEngine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		
		// Remove entities
		engine.removeEntity(tagSystem.getEntity(GameEnv.mapTag));
		engine.removeEntity(tagSystem.getEntity(GameEnv.playerTag));
		engine.removeEntity(tagSystem.getEntity(GameEnv.exitTag));
		
		Values<Entity> conditions = groupSystem.getGroup(GameEnv.conditionsGroup).values();
		while (conditions.hasNext()) {
			engine.removeEntity(conditions.next());
		}
		
		Values<Entity> operations = groupSystem.getGroup(GameEnv.operationsGroup).values();
		while (operations.hasNext()) {
			engine.removeEntity(operations.next());
		}
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
	
	private void initGame() {
		PooledEngine engine = Env.game.getEngine();
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
		Entity map = engine.createEntity();
		MapComponent mapComponent = engine.createComponent(MapComponent.class);
		mapComponent.map = mapLoader.load(level.file);
		map.add(mapComponent);
		engine.addEntity(map);
		tagSystem.setTag(map, GameEnv.mapTag);
		
		// Relevant layers
		MapLayer objectLayer = mapComponent.map.getLayers().get(GameEnv.objectLayer);
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)mapComponent.map.getLayers().get(GameEnv.backgroundLayer);
		
		// Player entity
		Entity player = engine.createEntity();
		TextureComponent texture = engine.createComponent(TextureComponent.class);
		texture.region = new TextureRegion(Env.game.getAssets().get("data/player.png", Texture.class));
		GridPositionComponent position = engine.createComponent(GridPositionComponent.class);
		TransformComponent transform = engine.createComponent(TransformComponent.class);
		ValueComponent value = engine.createComponent(ValueComponent.class);
		FontComponent font = engine.createComponent(FontComponent.class);
		ShaderComponent shader = engine.createComponent(ShaderComponent.class);
		player.add(position);
		player.add(texture);
		player.add(transform);
		player.add(value);
		player.add(font);
		player.add(shader);
		engine.addEntity(player);
		tagSystem.setTag(player, GameEnv.playerTag);
		RectangleMapObject rectangleObject = (RectangleMapObject)objectLayer.getObjects().get(GameEnv.playerTag);
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = fontMap;
		shader.shader = fontShader;
		
		// Exit entity
		Entity exit = engine.createEntity();
		texture = engine.createComponent(TextureComponent.class);
		texture.region = new TextureRegion(Env.game.getAssets().get("data/exit.png", Texture.class));
		position = engine.createComponent(GridPositionComponent.class);
		transform = engine.createComponent(TransformComponent.class);
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
			MapProperties properties = mapObject.getProperties();
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
				
				Entity condition = engine.createEntity();
				texture = engine.createComponent(TextureComponent.class);
				texture.region = new TextureRegion(Env.game.getAssets().get("data/checkpoint.png", Texture.class));
				position = engine.createComponent(GridPositionComponent.class);
				transform = engine.createComponent(TransformComponent.class);
				font = engine.createComponent(FontComponent.class);
				shader = engine.createComponent(ShaderComponent.class);
				ConditionComponent conditionComponent = engine.createComponent(ConditionComponent.class);
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
				
				Entity operationEntity = engine.createEntity();
				texture = engine.createComponent(TextureComponent.class);
				texture.region = new TextureRegion(Env.game.getAssets().get("data/operation.png", Texture.class));
				position = engine.createComponent(GridPositionComponent.class);
				transform = engine.createComponent(TransformComponent.class);
				OperationComponent operationComponent = engine.createComponent(OperationComponent.class);
				font = engine.createComponent(FontComponent.class);
				shader = engine.createComponent(ShaderComponent.class);
				operationComponent.operation = operation;
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
		Skin skin = Env.game.getSkin();
		Stage stage = Env.game.getStage();
		Assets assets = Env.game.getAssets();
		
		TextButton button = new TextButton("Reset", skin);
		button.setX(Env.virtualWidth - button.getWidth() - 20.0f);
		button.setY(Env.virtualHeight - button.getHeight() - 20.0f);
		
		button.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				reset();
			}
		});
		
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
		lblTime = new ShaderLabel("Time: 00:00", labelStyle, fontShader);
		lblTime.setFontScale(2.0f);
		
		stage.addActor(imgBackground);
		stage.addActor(imgLand);
		stage.addActor(imgTitle);
		stage.addActor(imgMapBackground);
		stage.addActor(btnPause);
		stage.addActor(lblTime);
		stage.addActor(button);
		
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		btnPause.setPosition((Env.virtualWidth - btnPause.getWidth()) * 0.5f, -btnPause.getHeight());
		lblTime.setPosition(Env.virtualWidth, GameEnv.cameraScreenPos.y + GameEnv.cameraHeight + 10.0f);
		imgMapBackground.setY(GameEnv.cameraScreenPos.y - 5.0f);
		imgMapBackground.setColor(1.0f, 1.0f, 1.0f, 0.0f);
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
					.push(Tween.to(lblTime, ActorTweener.Position, 0.4f)
							   .target(20.0f, lblTime.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(btnPause, ActorTweener.Position, 0.40f)
							   .target((Env.virtualWidth - btnPause.getWidth()) * 0.5f, 50.0f)
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	public void animateOut(final Class<? extends SionScreen> screenType) {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					Env.game.setScreen(screenType);
				}
			}
		};
		
		timeline.beginSequence()
					.push(Tween.to(btnPause, ActorTweener.Position, 0.20f)
							   .target((Env.virtualWidth - btnPause.getWidth()) * 0.5f, -btnPause.getHeight())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(lblTime, ActorTweener.Position, 0.2f)
							   .target(Env.virtualWidth, lblTime.getY())
						   	   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.25f)
							   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight())
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
		
	}
	
	private void onGameStart() {
		Engine engine = Env.game.getEngine();
		engine.getSystem(MathRenderingSystem.class).setRenderMap(true);
		engine.getSystem(PlayerControllerSystem.class).enable(true);
	}
}
