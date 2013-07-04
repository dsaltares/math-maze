package com.siondream.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.ObjectMap.Values;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ShaderManager implements Disposable {
	private static final String TAG = "ShaderManager";
	private static final String file = "data/shaders.xml";
	
	private Logger logger;
	private ObjectMap<String, ShaderProgram> shaders;
	
	public ShaderManager() {
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		shaders = new ObjectMap<String, ShaderProgram>();
		
		loadXML();
	}
	
	public ShaderProgram get(String name) {
		ShaderProgram shader = shaders.get(name, null);
		
		if (shader == null) {
			logger.error("shader " + name + " not found");
		}
		
		return shader;
	}

	@Override
	public void dispose() {
		Values<ShaderProgram> values = shaders.values();
		
		while (values.hasNext) {
			values.next().dispose();
		}
	}
	
	private void loadXML() {
		logger.info("loading file " + file);
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(file));

			int numAssets = root.getChildCount();
			for (int i = 0; i < numAssets; ++i) {
				Element node = root.getChild(i);
				String name = node.getAttribute("name");
				String vertex = node.getAttribute("vertex");
				String fragment = node.getAttribute("fragment");
				
				logger.info("loading shader " + name + " vertex: " + vertex + " fragment: " + fragment);
				
				ShaderProgram shader = new ShaderProgram(Gdx.files.internal(vertex), Gdx.files.internal(fragment));
				
				if (!shader.isCompiled()) {
					logger.error("error loading shader " + name);
					logger.equals(shader.getLog());
				}
				else {
					shaders.put(name, shader);
				}
			}
		}
		catch (Exception e) {
			logger.error("error loading file " + file + " " + e.getMessage());
		}
	}
}
