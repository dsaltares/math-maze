package com.siondream.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Values;

public class ParticleEffectPools implements Disposable {
	private final static String TAG = "ParticleEffectPools";
	
	private Logger logger;
	private ObjectMap<String, ParticleEffectPool> pools;
	
	public ParticleEffectPools() {
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		pools = new ObjectMap<String, ParticleEffectPool>();
	}
	
	public PooledEffect obtain(String name) {
		ParticleEffectPool pool = pools.get(name);
		
		if (pool == null) {
			ParticleEffect sampleEffect = new ParticleEffect();
			sampleEffect.load(Gdx.files.internal(name), Gdx.files.internal("data/particles"));
			pool = new ParticleEffectPool(sampleEffect,
										  Env.particlePoolInitialCapacity,
										  Env.particlePoolMaxCapacity);
			pools.put(name, pool);
		}
		
		return pool.obtain();
	}
	
	public void free(PooledEffect effect) {
		Values<ParticleEffectPool> values = pools.values();
		
		while (values.hasNext) {
			values.next().free(effect);
		}
	}
	
	public void clear() {
		Values<ParticleEffectPool> values = pools.values();
		
		while (values.hasNext) {
			values.next().clear();
		}
	}

	@Override
	public void dispose() {
		clear();
	}
}
