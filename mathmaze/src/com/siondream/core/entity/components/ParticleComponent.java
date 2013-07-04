package com.siondream.core.entity.components;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.Disposable;
import com.siondream.core.Env;

import ashley.core.Component;

public class ParticleComponent extends Component implements Disposable {

	public PooledEffect effect;
	
	public ParticleComponent() {
		super();
	}
	
	@Override
	public void dispose() {
		Env.game.getParticlePools().free(effect);
		effect = null;
	}
}
