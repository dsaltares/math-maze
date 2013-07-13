package com.siondream.math.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class DoorComponent extends Component implements Poolable {

	public int id;
	
	public DoorComponent() {
		super();
		reset();
	}
	
	@Override
	public void reset() {
		id = 0;
	}
}
