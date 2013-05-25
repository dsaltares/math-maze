/*  Copyright 2012 SionEngine
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siondream.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * @class LanguageManager
 * @author David Saltares Márquez
 * @date 05/09/2012
 * 
 * @brief Provides i18n support
 *
 */
public class LanguageManager {
	private static final String DEFAULT_LANGUAGE = "en";
	
	private Logger logger;
	private String folder;
	private ObjectMap<String, String> strings = new ObjectMap<String, String>();
	private String languageName;
	
	private static final String TAG = "LanguageManager";
	
	/**
	 * Will load lang/en.xml
	 */
	public LanguageManager() {
		this("data/lang", DEFAULT_LANGUAGE);
	}
	
	/**
	 * Will load the folder + / + language + .xml file
	 * 
	 * @param folder
	 * @param language
	 * @param loggingLevel
	 */
	public LanguageManager(String folder, String language) {
		this.logger = new Logger(TAG, Env.debugLevel);
		this.folder = folder;
		this.languageName = language;
		
		if (!loadLanguage(languageName) && !languageName.equals(DEFAULT_LANGUAGE)) {
			loadLanguage(DEFAULT_LANGUAGE);
		}
	}
	
	/**
	 * @return current language name
	 */
	public String getLanguage() {
		return languageName;
	}
	
	/**
	 * @param key key for the string to translate
	 * @return translated string in the current language, the key if it's not found
	 */
	public String getString(String key) {
		String string = strings.get(key);
		
		if (string != null) {
			return string;
		}
	
		logger.error("string " + key + " not found");
		return key;
	}
	
	public String getString(String key, Object... args) {
		return String.format(getString(key, args));
	}
	
	/**
	 * Tries to load a new language replacing the old one. If it fails, nothing it's done.
	 * 
	 * @param languageName name of the language to load
	 * @return true if succeeded, false in any other case
	 */
	public boolean loadLanguage(String languageName) {
		logger.info("loading " + languageName);
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(folder + "/" + languageName + ".xml").read());
			
			// Don't clear the previous lang just yet
			Array<Element> stringElements = root.getChildrenByName("string");
			ObjectMap<String, String> newStrings = new ObjectMap<String, String>(strings.size);
			
			// Load all the strings for that language
			for (int j = 0; j < strings.size; ++j) {
				Element stringNode = stringElements.get(j);
				String key = stringNode.getAttribute("key");
				String value = stringNode.getAttribute("value");
				value = value.replace("<br />", "\n");
				newStrings.put(key, value);
				logger.info("LanguageManager: loading key " + key);
			}
			
			// Swap the languages now that is safe to do so
			this.languageName = languageName;
			this.strings = newStrings;
			
			logger.info(languageName + " language sucessfully loaded");
		}
		catch (Exception e) {
			logger.error("error loading " + languageName + " we keep the previous one");
			return false;
		}
		
		return true;
	}
}
