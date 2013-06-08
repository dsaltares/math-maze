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
	
	private Logger logger;
	private Array<Level> levels;
	
	public LevelManager(String file, Preferences preferences) {
		this.logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		this.levels = new Array<Level>();
		
		try {
			FileHandle fileHandle = Gdx.files.internal(file);
			XmlReader reader = new XmlReader();
			Element root = reader.parse(fileHandle);

			for (Element levelElement : root.getChildrenByName("level")) {
				String name = levelElement.get("name");
				String assetGroup = levelElement.get("assetGroup", "");
				int stars = preferences.getInteger(name + ".stars", 0);
				boolean unlocked = preferences.getBoolean(name + ".unlocked", false);
				
				Level level = new Level(name, assetGroup, stars, unlocked); 
				levels.add(level);
				logger.info("found " + level);
			}
			
		} catch (Exception e) {
			logger.error("error processing file " + file + " " + e.getMessage());
		}
	}
	
	public Array<Level> getLevels() {
		return levels;
	}
	
	public class Level {
		public String file;
		public String assetGroup;
		public int stars;
		public boolean unlocked;
		
		public Level(String file, String assetGroup, int stars, boolean unlocked) {
			this.file = file;
			this.assetGroup = assetGroup;
			this.stars = stars;
			this.unlocked = unlocked;
		}
		
		@Override
		public String toString() {
			return "level " + file + " - " + stars + " stars - unlocked " + unlocked;  
		}
	}
}
