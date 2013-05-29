package com.siondream.math;

import ashley.core.Entity;
import ashley.core.PooledEngine;
import ashley.utils.IntMap.Values;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.LevelManager.Level;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;
import com.siondream.math.systems.CameraControllerSystem;
import com.siondream.math.systems.PlayerControllerSystem;

public class GameScreen extends SionScreen {

	public final static String TAG = "GameScreen";
	
	private Logger logger;
	private TmxMapLoader mapLoader;
	private Level level;
	
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

		GameEnv.game.getStage().clear();
	}
	
	@Override
	public void render(float delta) {
		
	}
	
	@Override
	public void show() {
		super.show();
		init();
	}

	@Override
	public void hide() {
		super.hide();
		dispose();
	}
	
	private void init() {
		PooledEngine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		
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
		player.add(position);
		player.add(texture);
		player.add(transform);
		player.add(value);
		player.add(font);
		engine.addEntity(player);
		tagSystem.setTag(player, GameEnv.playerTag);
		RectangleMapObject rectangleObject = (RectangleMapObject)objectLayer.getObjects().get(GameEnv.playerTag);
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = Env.game.getAssets().get("data/game.fnt", BitmapFont.class);
		font.font.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		
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
				ConditionComponent conditionComponent = engine.createComponent(ConditionComponent.class);
				conditionComponent.conditions = conditions;
				condition.add(texture);
				condition.add(position);
				condition.add(transform);
				condition.add(conditionComponent);
				condition.add(font);
				engine.addEntity(condition);
				groupSystem.register(condition, GameEnv.conditionsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				font.font = Env.game.getAssets().get("data/game.fnt", BitmapFont.class);
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
				operationComponent.operation = operation;
				operationEntity.add(texture);
				operationEntity.add(position);
				operationEntity.add(operationComponent);
				operationEntity.add(transform);
				operationEntity.add(font);
				engine.addEntity(operationEntity);
				groupSystem.register(operationEntity, GameEnv.operationsGroup);
				rectangleObject = (RectangleMapObject)mapObject;
				rectangle = rectangleObject.getRectangle();
				position.x = (int)(rectangle.x / rectangle.width);
				position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
				transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
				transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
				font.font = Env.game.getAssets().get("data/game.fnt", BitmapFont.class);
			}
		}
		
		engine.getSystem(CameraControllerSystem.class).setTarget(player);
		
		PlayerControllerSystem controller = Env.game.getEngine().getSystem(PlayerControllerSystem.class);
		
		// Create UI
		createUI();
	}
	
	private void createUI() {
		Skin skin = Env.game.getSkin();
		Stage stage = Env.game.getStage();
		
		TextButton button = new TextButton("Reset", skin);
		button.setX(Env.virtualWidth - button.getWidth() - 20.0f);
		button.setY(Env.virtualHeight - button.getHeight() - 20.0f);
		
		button.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				reset();
			}
		});
		
		stage.addActor(button);
	}
	
	
}
