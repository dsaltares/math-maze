package com.siondream.core.entity.components;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class FontComponent extends Component implements Poolable {

	public BitmapFont font;
	
	@Override
	public void reset() {
		font = null;
	}
}
