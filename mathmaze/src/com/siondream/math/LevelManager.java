package com.siondream.math;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.siondream.core.Env;

public class LevelManager {
		
	private static final String TAG = "LevelManager";
	private static final String LAST_UNLOCKED_LEVEL = "lastUnlockedLevel";
	
	private Logger logger;
	private Array<Level> levels;
	private Preferences preferences;
	
	public LevelManager(String file, Preferences preferences) {
		this.logger = new Logger(TAG, Env.debugLevel);
		this.preferences = preferences;
		
		logger.info("initialising");
		
		int lastUnlockedLevel = preferences.getInteger("lastUnlockedLevel", 1);
		this.levels = new Array<Level>();
		
		try {
			FileHandle fileHandle = Gdx.files.internal(file);
			XmlReader reader = new XmlReader();
			Element root = reader.parse(fileHandle);
			boolean first = true;
			int index = 1;
			
			for (Element levelElement : root.getChildrenByName("level")) {
				String fileName = levelElement.get("file");
				String name = levelElement.get("name");
				String assetGroup = levelElement.get("assetGroup", "");
				int stars = preferences.getInteger(name + ".stars", 0);
				boolean unlocked = index <= lastUnlockedLevel;
				first = false;
				
				Level level = new Level(fileName, assetGroup, name, stars, index, unlocked); 
				levels.add(level);
				logger.info("found " + level);
				++index;
			}
			
		} catch (Exception e) {
			logger.error("error processing file " + file + " " + e.getMessage());
		}
	}
	
	public Array<Level> getLevels() {
		return levels;
	}
	
	public void saveStars(Level level, int stars) {
		int index = levels.indexOf(level, true);
		
		if (index == -1) {
			logger.error(level.name + " is not registered");
			return;
		}
		
		if (level.stars < stars) {
			logger.info(level.name + " has now " + stars + " stars");
			level.stars = stars;
			preferences.putInteger(level.name + ".stars", stars);
		}
		
		int nextIndex = index + 1;
		
		if (nextIndex < levels.size) {
			Level nextLevel = levels.get(nextIndex);
			nextLevel.unlocked = true;
			logger.info("unlocking " + nextLevel.name);
			
			int lastUnlocked = preferences.getInteger(LAST_UNLOCKED_LEVEL, 0);
			
			if (lastUnlocked < nextIndex + 1) {
				preferences.putInteger(LAST_UNLOCKED_LEVEL, nextIndex + 1);
			}
		}
		
		preferences.flush();
	}
	
	public static class Level {
		public String file;
		public String assetGroup;
		public String name;
		public int stars;
		public int index;
		public boolean unlocked;

		public Level(String file, String assetGroup, String name, int stars, int index, boolean unlocked) {
			this.file = file;
			this.assetGroup = assetGroup;
			this.name = name;
			this.stars = stars;
			this.index = index;
			this.unlocked = unlocked;
		}
		
		@Override
		public String toString() {
			return "level " + name + " - " + stars + " stars - unlocked " + unlocked;  
		}
	}
}
