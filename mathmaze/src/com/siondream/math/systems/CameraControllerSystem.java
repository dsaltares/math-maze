package com.siondream.math.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.core.tweeners.CameraTweener;
import com.siondream.math.GameEnv;
import com.siondream.math.GameScreen;
import com.siondream.math.components.GridPositionComponent;

import ashley.core.Entity;
import ashley.core.EntitySystem;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

public class CameraControllerSystem extends EntitySystem {
	
	private enum State {
		Debug,
		FollowTarget,
		Shake,
		NoTarget,
	}
	
	private static final String TAG = "CameraControllerSystem";
	
	private Logger logger; 
	private OrthographicCamera camera;
	private Entity target;
	private State state;
	private CameraShakeEffectCallback cameraShakeCallback;
	
	public CameraControllerSystem() {
		super();
		this.logger = new Logger(TAG, Env.debugLevel);
		this.camera = Env.game.getCamera();
		this.state = State.NoTarget;
		this.cameraShakeCallback = new CameraShakeEffectCallback();
	}
	
	public void setTarget(Entity target) {
		if (!target.hasComponent(GridPositionComponent.class)) {
			this.target = null;
			logger.error("invalid target entity, no position available");
		}
		
		this.target = target;
		this.state = State.FollowTarget;
	}
	
	public void startShakeEffect(boolean horizontal) {
		this.state = State.Shake;
		
		Timeline timeline = Timeline.createSequence();
		
		if (horizontal) {
			timeline.beginSequence()
					.push(Tween.to(camera, CameraTweener.Position, GameEnv.cameraShakeTime)
							   .target(camera.position.x - 10.0f, camera.position.y, camera.position.z)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(camera, CameraTweener.Position, GameEnv.cameraShakeTime)
							   .target(camera.position.x + 10.0f, camera.position.y, camera.position.z)
							   .ease(TweenEquations.easeInOutQuad))
					.end()
					.repeat(GameEnv.cameraShakeRepetitions, 0.0f)
					.setCallback(cameraShakeCallback)
					.start(Env.game.getTweenManager());
		}
		else {
			timeline.beginSequence()
			.push(Tween.to(camera, CameraTweener.Position, GameEnv.cameraShakeTime)
					   .target(camera.position.x, camera.position.y - 10.0f, camera.position.z)
					   .ease(TweenEquations.easeInOutQuad))
			.push(Tween.to(camera, CameraTweener.Position, GameEnv.cameraShakeTime)
					   .target(camera.position.x, camera.position.y + 10.0f, camera.position.z)
					   .ease(TweenEquations.easeInOutQuad))
			.end()
			.repeat(GameEnv.cameraShakeRepetitions, 0.0f)
			.setCallback(cameraShakeCallback)
			.start(Env.game.getTweenManager());
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (!(GameEnv.game.getScreen() instanceof GameScreen)) {
			return;
		}
		
		switch (state) {
		case Debug:
			if (!GameEnv.debugCamera) {
				state = target != null ? State.FollowTarget : State.NoTarget;
				return;
			}
			
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				camera.position.x -= GameEnv.cameraScrollSpeed * deltaTime;
			}
			else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				camera.position.x += GameEnv.cameraScrollSpeed * deltaTime;
			}
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				camera.position.y += GameEnv.cameraScrollSpeed * deltaTime;
			}
			else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				camera.position.y -= GameEnv.cameraScrollSpeed * deltaTime;
			}
			
			if (Gdx.input.isKeyPressed(Keys.A)) {
				camera.zoom -= GameEnv.cameraZoomSpeed * deltaTime;
			}
			else if (Gdx.input.isKeyPressed(Keys.S)) {
				camera.zoom += GameEnv.cameraZoomSpeed * deltaTime;
			}
			
			break;
		case FollowTarget:
			if (GameEnv.debugCamera) {
				state = State.Debug;
				return;
			}
			
			Entity player = Env.game.getEngine().getSystem(TagSystem.class).getEntity(GameEnv.playerTag);
			camera.position.set(player.getComponent(TransformComponent.class).position);
			
			break;
		case NoTarget:
		case Shake:
			break;
		}
	}
	
	private class CameraShakeEffectCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			if (type == TweenCallback.COMPLETE) {
				state = State.FollowTarget;
			}
		}
	}
}
