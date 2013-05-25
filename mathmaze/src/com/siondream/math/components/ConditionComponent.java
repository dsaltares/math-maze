package com.siondream.math.components;

import com.badlogic.gdx.utils.Array;
import com.siondream.math.Condition;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class ConditionComponent extends Component implements Poolable {
	
	public Array<Condition> conditions;
	
	public ConditionComponent() {
		conditions = new Array<Condition>();
	}

	@Override
	public void reset() {
		conditions.clear();
	}}
