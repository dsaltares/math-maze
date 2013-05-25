package com.siondream.core.entity.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class OverridePhysicsComponent extends Component implements Poolable {

	public boolean enable;
	
	public OverridePhysicsComponent() {
		reset();
	}
	
	@Override
	public void reset() {
		enable = true;
	}

}
