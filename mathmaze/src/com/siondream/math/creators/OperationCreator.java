package com.siondream.math.creators;

import ashley.core.Engine;
import ashley.core.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.entity.components.ColorComponent;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.ShaderComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.Operation;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.OperationComponent;

public class OperationCreator implements EntityCreator {

	public static class OperationParams {
		public OperationParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public Map map;
		public MapObject object;
	}
	
	private static final String TAG = "OperationCreator";
	
	private Logger logger;
	
	public OperationCreator() {
		logger = new Logger(TAG, Env.debugLevel);
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof OperationParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		OperationParams operationParams = (OperationParams)params;
		
		String name = operationParams.object.getName();
		String[] parts = name.split(":");
		
		if (parts.length != 2) {
			logger.error("invalid operation entity " + name);
			return null;
		}
		
		MapProperties properties = operationParams.object.getProperties();
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		Engine engine = Env.game.getEngine();
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		RectangleMapObject rectangleObject = (RectangleMapObject)operationParams.object;
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)operationParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		Operation operation = new Operation(parts[0], Integer.parseInt(parts[1]));
		
		Entity operationEntity = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		OperationComponent operationComponent = new OperationComponent();
		FontComponent font = new FontComponent();
		ShaderComponent shader = new ShaderComponent();
		ColorComponent color = new ColorComponent();
		
		operationEntity.add(texture);
		operationEntity.add(position);
		operationEntity.add(operationComponent);
		operationEntity.add(transform);
		operationEntity.add(font);
		operationEntity.add(shader);
		operationEntity.add(color);
		
		operationComponent.operation = operation;
		operationComponent.persist = Boolean.parseBoolean(properties.get("persist", "false", String.class));
		
		if (operationComponent.persist) {
			texture.region = new TextureRegion(atlas.findRegion("operation-persist"));
		}
		else {
			texture.region = new TextureRegion(atlas.findRegion("operation"));
		}
		
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = skin.getFont("gameFont");
		shader.shader = Env.game.getShaderManager().get("font");
		color.color = Color.BLACK.cpy();
		
		engine.addEntity(operationEntity);
		
		groupSystem.register(operationEntity, GameEnv.operationsGroup);
		
		return operationEntity;
	}

}
