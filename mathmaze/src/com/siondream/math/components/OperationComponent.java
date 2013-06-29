package com.siondream.math.components;

import com.siondream.math.Operation;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class OperationComponent extends Component implements Poolable {

	public Operation operation;
	public boolean persist;
	
	@Override
	public void reset() {
		persist = false;
	}
}
