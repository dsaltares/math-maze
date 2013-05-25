package com.siondream.core.entity.components;

import com.badlogic.gdx.maps.tiled.TiledMap;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class MapComponent extends Component implements Poolable {

	public TiledMap map;
	
	@Override
	public void reset() {
		map = null;
	}
}
