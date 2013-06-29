package com.siondream.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class AbsoluteFileHandleResolver implements FileHandleResolver {

	@Override
	public FileHandle resolve(String fileName) {
		return Gdx.files.absolute(fileName);
	}

}
