package com.siondream.math.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class GridPositionComponent extends Component implements Poolable {

	public int x;
	public int y;
	
	public GridPositionComponent() {
		reset();
	}
	
	
	@Override
	public void reset() {
		x = 0;
		y = 0;
	}
}
