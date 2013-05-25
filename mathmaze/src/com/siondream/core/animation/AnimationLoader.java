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

package com.siondream.core.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.siondream.core.Env;

/**
 * @class AnimationLoader
 * @author David Saltares Márquez
 * @date 09/09/2012
 * 
 * @brief Asynchronous asset loader for AnimationData objects
 *
 */
public class AnimationLoader extends AsynchronousAssetLoader<AnimationData, AnimationLoader.AnimationParameter > {

	static public class AnimationParameter extends AssetLoaderParameters<AnimationData> {
	}
	
	private AnimationData animationData = null;
	private Logger logger;
	
	/**
	 * Creates a new AnimationLoader
	 * 
	 * @param resolver file resolver to be used
	 */
	public AnimationLoader(FileHandleResolver resolver) {
		super(resolver);
		
		animationData = null;
		logger = new Logger("Animation", Env.debugLevel);
	}
	
	/**
	 * Aynchronously loads the animation data animations
	 */
	@Override
	public void loadAsync(AssetManager manager, String fileName, AnimationParameter parameter) {
		logger.info("loading " + fileName);
		
		animationData = new AnimationData();
		
		// Retrieve texture
		animationData.texture = manager.get(stripExtension(fileName) + ".png", Texture.class);
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(fileName));
			
			animationData.rows = Integer.parseInt(root.getAttribute("rows"));
			animationData.columns = Integer.parseInt(root.getAttribute("columns"));
			animationData.frameDuration = Float.parseFloat(root.getAttribute("frameDuration"));
			
			Array<Element> animationNodes = root.getChildrenByName("animation");
			
			for (int i = 0; i < animationNodes.size; ++i) {
				Element animationNode = animationNodes.get(i);
				String name = animationNode.getAttribute("name");
				String frames = animationNode.getAttribute("frames");
				
				Animation animation = new Animation(animationData.frameDuration,
													getAnimationFrames(animationData.texture, frames),
													getPlayMode(animationNode.get("mode", "normal")));
				animationData.animations.put(name, animation);
				
				logger.info("" + fileName + " loaded animation " + name);
				
				if (i == 0) {
					animationData.defaultAnimation = animation;
				}
			}
			
		} catch (Exception e) {
			logger.error("error loading file " + fileName + " " + e.getMessage());
		}
	}

	/**
	 * Retrieves the animation data as it is (without loading anything, this is strictly asynchronous)
	 */
	@Override
	public AnimationData loadSync(AssetManager manager, String fileName, AnimationParameter parameter) {
		return animationData;
	}
	
	/**
	 * Gets animation data dependencies, this is, the spreadsheet texture to load 
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, AnimationParameter parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		dependencies.add(new AssetDescriptor<Texture>(stripExtension(fileName) + ".png", Texture.class));
		
		return dependencies;
	}
	
	private String stripExtension (String fileName) {
        if (fileName == null) return null;
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) return fileName;
        return fileName.substring(0, pos);
    }
	
	private int getPlayMode(String mode) {
		if (mode.equals("normal")) {
			return Animation.NORMAL; 
		}
		else if (mode.equals("loop")) {
			return Animation.LOOP;
		}
		else if (mode.equals("loop_pingpong")) {
			return Animation.LOOP_PINGPONG;
		}
		else if (mode.equals("loop_random")) {
			return Animation.LOOP_RANDOM;
		}
		else if (mode.equals("loop_reversed")) {
			return Animation.LOOP_REVERSED;
		}
		else if (mode.equals("reversed")) {
			return Animation.REVERSED;
		}
		else {
			return Animation.NORMAL;
		}
	}
	
	private Array<TextureRegion> getAnimationFrames(Texture texture, String frames) {
		Array<TextureRegion> regions = new Array<TextureRegion>();
		
		if (frames != null) {
			String[] framesArray = frames.split(",");
			int numFrames = framesArray.length;
			int width = texture.getWidth() / animationData.columns;
			int height = texture.getHeight() / animationData.rows;
			
			for (int i = 0; i < numFrames; ++i) {
				int frame = Integer.parseInt(framesArray[i]);
				int x = ((frame % animationData.columns) % animationData.rows) * width;
				int y = (frame / animationData.columns) * height;
				
				regions.add(new TextureRegion(texture, x, y, width, height));
			}
		}
		
		return regions;
	}
}
