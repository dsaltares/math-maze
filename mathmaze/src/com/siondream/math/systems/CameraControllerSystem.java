package com.siondream.math.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.components.GridPositionComponent;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.EntitySystem;

public class CameraControllerSystem extends EntitySystem {
	
	private static final String TAG = "CameraControllerSystem";
	
	private Logger logger; 
	private OrthographicCamera camera;
	private Entity target;
	
	public CameraControllerSystem() {
		super();
		this.logger = new Logger(TAG, Env.debugLevel);
		this.camera = Env.game.getCamera();
		
	}
	
	public void setTarget(Entity target) {
		if (!target.hasComponent(GridPositionComponent.class)) {
			this.target = null;
			logger.error("invalid target entity, no position available");
		}
		
		this.target = target;
	}
	
	@Override
	public void update(float deltaTime) {
		if (this.target == null) {
			return;
		}
		
		if (!GameEnv.debugCamera) {
			Entity player = Env.game.getEngine().getSystem(TagSystem.class).getEntity(GameEnv.playerTag);
			camera.position.set(player.getComponent(TransformComponent.class).position);
		}
		else {
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
		}
	}
}
