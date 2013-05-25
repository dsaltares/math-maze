package com.siondream.core.entity.components;

import com.badlogic.gdx.physics.box2d.Body;
import com.siondream.core.Env;

import ashley.core.Component;
import ashley.utils.Pool.Poolable;

public class PhysicsComponent extends Component implements Poolable {
	public Body body;

	@Override
	public void reset() {
		Env.game.getWorld().destroyBody(body);
		body = null;
	}
}
