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
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.components.GridPositionComponent;

public class ExitCreator implements EntityCreator {

	public static class ExitParams {
		public ExitParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public Map map;
		public MapObject object;
	}
	
	private static final String TAG = "ExitCreator";
	
	private Logger logger;
	
	public ExitCreator() {
		logger = new Logger(TAG, Env.debugLevel);
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof ExitParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		ExitParams exitParams = (ExitParams)params;
		
		Engine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		RectangleMapObject rectangleObject = (RectangleMapObject)exitParams.object;
		TextureAtlas atlas = GameEnv.game.getSkin().getAtlas();
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)exitParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		Entity exit = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		
		exit.add(transform);
		exit.add(texture);
		exit.add(position);
		
		texture.region = new TextureRegion(atlas.findRegion("exit"));
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		
		tagSystem.setTag(exit, GameEnv.exitTag);
		
		engine.addEntity(exit);
		
		return exit;
	}

}
