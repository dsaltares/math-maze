package com.siondream.core.entity.components;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.siondream.core.animation.AnimationData;

public class AnimationComponent extends Component implements Poolable {
	public AnimationData data;
	public Animation currentAnimation;
	public float time;

	public AnimationComponent() {
		reset();
	}
	
	@Override
	public void reset() {
		data = null;
		time = 0.0f;
		currentAnimation = null;
	}
}
