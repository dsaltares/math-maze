package com.siondream.math.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class ValueComponent extends Component implements Poolable {

	public int value;
	
	public ValueComponent() {
		super();
		reset();
	}
	
	@Override
	public void reset() {
		value = 0;
	}
}
