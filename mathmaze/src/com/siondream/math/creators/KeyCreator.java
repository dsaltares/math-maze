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
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.Env;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.KeyComponent;

public class KeyCreator implements EntityCreator {
	
	public static class KeyParams {
		public KeyParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public MapObject object;
		public Map map;
	}
	
	private static final String TAG = "KeyCreator";
	
	private Logger logger;
	private IntMap<String> regions;
	
	public KeyCreator() {
		logger = new Logger(TAG, Env.debugLevel);
		regions = new IntMap<String>();
		regions.put(0, "redKey");
		regions.put(1, "greenKey");
		regions.put(2, "blueKey");
		regions.put(3, "yellowKey");
		regions.put(4, "purpleKey");
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof KeyParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		KeyParams keyParams = (KeyParams)params;
		
		Engine engine = Env.game.getEngine();
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		RectangleMapObject rectangleObject = (RectangleMapObject)keyParams.object;
		TextureAtlas atlas = GameEnv.game.getSkin().getAtlas();
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)keyParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		Entity door = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		KeyComponent keyComponent = new KeyComponent();
		
		door.add(transform);
		door.add(texture);
		door.add(position);
		door.add(keyComponent);
		
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		keyComponent.id = Integer.parseInt(keyParams.object.getName());
		texture.region = new TextureRegion(atlas.findRegion(regions.get(keyComponent.id)));
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		
		groupSystem.register(door, GameEnv.keyTag);
		
		engine.addEntity(door);
		
		return door;
	}
}
