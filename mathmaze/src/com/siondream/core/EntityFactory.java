package com.siondream.core;

import ashley.core.Entity;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;

public class EntityFactory {
	
	private static final String TAG = "EntityFactory";
	
	private Logger logger;
	private ObjectMap<String, EntityCreator> creators;
	
	public EntityFactory () {
		logger = new Logger(TAG, Env.debugLevel);
		logger.info("initialising");
		creators = new ObjectMap<String, EntityCreator>();
	}
	
	public void addCreator(String name, EntityCreator creator) {
		creators.put(name, creator);
	}
	
	public Entity createEntity(String name, Object params) {
		EntityCreator creator = creators.get(name);
		
		if (creator == null) {
			logger.error("failed to find creator of name " + name);
			return null;
		}
		
		logger.info("creating entity with creator " + name);
		return creator.createEntity(params);
	}
	
	public static interface EntityCreator {
		public Entity createEntity(Object params);
	}
}
