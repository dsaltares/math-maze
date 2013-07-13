package com.siondream.math.creators;

import ashley.core.Engine;
import ashley.core.Entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.components.DoorComponent;
import com.siondream.math.components.GridPositionComponent;

public class DoorCreator implements EntityCreator {

	public static class DoorParams {
		public DoorParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public MapObject object;
		public Map map;
	}
	
	private static final String TAG = "DoorCreator";
	
	private Logger logger;
	private IntMap<String> regions;
	
	public DoorCreator() {
		logger = new Logger(TAG, Env.debugLevel);
		regions = new IntMap<String>();
		regions.put(0, "redDoor");
		regions.put(1, "greenDoor");
		regions.put(2, "blueDoor");
		regions.put(3, "yellowDoor");
		regions.put(4, "purpleDoor");
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof DoorParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		DoorParams doorParams = (DoorParams)params;
		
		Engine engine = Env.game.getEngine();
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		RectangleMapObject rectangleObject = (RectangleMapObject)doorParams.object;
		TextureAtlas atlas = GameEnv.game.getSkin().getAtlas();
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)doorParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		Entity door = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		DoorComponent doorComponent = new DoorComponent();
		
		door.add(transform);
		door.add(texture);
		door.add(position);
		door.add(doorComponent);
		
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		doorComponent.id = Integer.parseInt(doorParams.object.getName());
		texture.region = new TextureRegion(atlas.findRegion(regions.get(doorComponent.id)));
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		
		groupSystem.register(door, GameEnv.doorTag);
		
		engine.addEntity(door);
		
		return door;
	}

}
