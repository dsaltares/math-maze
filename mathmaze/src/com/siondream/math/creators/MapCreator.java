package com.siondream.math.creators;

import ashley.core.Engine;
import ashley.core.Entity;

import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.AbsoluteFileHandleResolver;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.GameEnv;

public class MapCreator implements EntityCreator {

	private static final String TAG = "MapCreator";
	
	private Logger logger;
	private TmxMapLoader mapLoader;
	
	public MapCreator() {
		logger = new Logger(TAG, Env.debugLevel);
		
		// Map entity
		if (GameEnv.debugMap.length() > 0) {
			mapLoader = new TmxMapLoader(new AbsoluteFileHandleResolver());
		}
		else {
			mapLoader = new TmxMapLoader();
		}
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof String)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		String levelName = (String)params;
		Engine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		
		Entity map = new Entity();
		MapComponent mapComponent = new MapComponent();
		mapComponent.map = mapLoader.load(levelName);
		map.add(mapComponent);
		
		engine.addEntity(map);
		
		tagSystem.setTag(map, GameEnv.mapTag);
		
		return map;
	}

}
