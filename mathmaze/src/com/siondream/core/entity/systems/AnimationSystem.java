package com.siondream.core.entity.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.siondream.core.entity.components.AnimationComponent;
import com.siondream.core.entity.components.StateComponent;
import com.siondream.core.entity.components.TextureComponent;

import ashley.core.Entity;
import ashley.core.Family;
import ashley.systems.IteratingSystem;

public class AnimationSystem extends IteratingSystem {

	public AnimationSystem() {
		super(Family.getFamilyFor(AnimationComponent.class,
								  StateComponent.class,
								  TextureComponent.class));
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		AnimationComponent animation = entity.getComponent(AnimationComponent.class);
		StateComponent state = entity.getComponent(StateComponent.class);
		TextureComponent texture = entity.getComponent(TextureComponent.class);
		
		Animation newAnimation = animation.data.getAnimation(StateComponent.getName(state.id));
		
		if (animation.currentAnimation != newAnimation) {
			animation.time = 0.0f;
			animation.currentAnimation = newAnimation;
		}
		else {		
			animation.time += deltaTime;
		}
		
		texture.region = animation.currentAnimation.getKeyFrame(animation.time);
	}
}
