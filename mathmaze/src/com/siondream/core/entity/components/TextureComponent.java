package com.siondream.core.entity.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class TextureComponent extends Component implements Poolable {
	public TextureRegion region;
	
	@Override
	public void reset() {
		region = null;
	}
}
