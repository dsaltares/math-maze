package com.siondream.math.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class KeyComponent extends Component implements Poolable {
	public int id;
	
	public KeyComponent() {
		super();
		reset();
	}
	
	@Override
	public void reset() {
		id = 0;
	}
}
