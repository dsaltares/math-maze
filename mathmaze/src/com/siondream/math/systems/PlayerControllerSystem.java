package com.siondream.math.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.core.entity.components.ColorComponent;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.ParticleComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.core.tweeners.TransformTweener;
import com.siondream.math.Condition;
import com.siondream.math.GameEnv;
import com.siondream.math.GameScreen;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.DoorComponent;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.KeyComponent;
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.EntitySystem;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Keys;
import ashley.utils.IntMap.Values;
import ashley.utils.MathUtils;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

public class PlayerControllerSystem extends EntitySystem implements GestureListener, InputProcessor {
	
	private static final String TAG = "PlayerControllerSystem";
	
	private Logger logger;
	private OrthographicCamera camera;
	private Vector3 mousePos3;
	private Vector2 direction;
	private Vector2 destination;
	private Vector2 rightDirection;
	private IntMap<Entity> conditionEntities;
	private IntMap<Entity> operationEntities;
	private IntMap<Entity> doorEntities;
	private IntMap<Entity> keyEntities;
	private GestureDetector gestureDetector;
	
	private Sound sfxTap;
	private Sound sfxSwipe;
	private Sound sfxShake;
	private Sound sfxError;
	
	private boolean enabled;
	
	public PlayerControllerSystem() {
		super();
		
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		camera = Env.game.getCamera();
		mousePos3 = new Vector3();
		direction = new Vector2();
		destination = new Vector2();
		rightDirection = Vector2.X.cpy();
		gestureDetector = new GestureDetector(this);
		Env.game.getInputMultiplexer().addProcessor(gestureDetector);
		Env.game.getInputMultiplexer().addProcessor(this);
		
		Assets assets = Env.game.getAssets();
		sfxTap = assets.get("data/sfx/tap.mp3", Sound.class);
		sfxSwipe = assets.get("data/sfx/swipe.mp3", Sound.class);
		sfxShake = assets.get("data/sfx/shake.mp3", Sound.class);
		sfxError = assets.get("data/sfx/error.wav", Sound.class);
		
		enabled = false;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		operationEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
																	  OperationComponent.class));
		
		conditionEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
																	  ConditionComponent.class));
		
		doorEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
				  												 DoorComponent.class));
		
		keyEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
					 											KeyComponent.class));
	}
	
	public void enable(boolean enable) {
		enabled = enable;
	}
	
	public void cancelMovement() {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		TransformComponent transform = player.getComponent(TransformComponent.class);
		Env.game.getTweenManager().killTarget(transform);
	}
	
	private boolean isExitAt(Vector2 destination) {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity exit = tagSystem.getEntity(GameEnv.exitTag);
		GridPositionComponent position = exit != null? exit.getComponent(GridPositionComponent.class) : null;
		
		return position != null ? position.x == (int)destination.x && position.y == (int)destination.y
								: false;
	}
	
	private boolean isValidGridPosition(TiledMapTileLayer grid, ValueComponent value, Vector2 destination) {
		Cell cell = grid.getCell((int)destination.x, grid.getHeight() - (int)destination.y - 1);
		
		if (cell == null)
			return false;
 
		TiledMapTile tile = cell.getTile();
		if (!Boolean.parseBoolean(tile.getProperties().get("walkable", "false", String.class))) {
			return false;
		}
		
		Values<Entity> values = conditionEntities.values();
		
		while(values.hasNext()) {
			Entity condition = values.next();
			GridPositionComponent gridPos = condition.getComponent(GridPositionComponent.class);
			
			if (gridPos.x == (int)destination.x && gridPos.y == (int)destination.y) {
				ConditionComponent conditionComponent = condition.getComponent(ConditionComponent.class);
				
				for (Condition c : conditionComponent.conditions) {
					if (!c.check(value.value)) {
						return false;
					}
				}
			}
		}
		
		values = doorEntities.values();
		
		while(values.hasNext()) {
			Entity condition = values.next();
			GridPositionComponent gridPos = condition.getComponent(GridPositionComponent.class);
			
			if (gridPos.x == (int)destination.x && gridPos.y == (int)destination.y) {
				return false;
			}
		}
		
		return true;
	}
	
	private Entity getOperationAt(Vector2 position) {
		Values<Entity> values = operationEntities.values();
		
		while(values.hasNext()) {
			Entity operation = values.next();
			GridPositionComponent gridPos = operation.getComponent(GridPositionComponent.class);
			
			if (gridPos.x == (int)position.x && gridPos.y == (int)position.y) {
				return operation;
			}
		}
		
		return null;
	}
	
	private Entity getKeyAt(Vector2 position) {
		Values<Entity> values = keyEntities.values();
		
		while(values.hasNext()) {
			Entity operation = values.next();
			GridPositionComponent gridPos = operation.getComponent(GridPositionComponent.class);
			
			if (gridPos.x == (int)position.x && gridPos.y == (int)position.y) {
				return operation;
			}
		}
		
		return null;
	}
	
	private void getDestinationFor(float angle, GridPositionComponent position, Vector2 destination) {
		float angle45 = MathUtils.degreesToRadians * 45.0f;
		float angle135 = MathUtils.degreesToRadians * 135.0f;
		float angle225 = MathUtils.degreesToRadians * 225.0f;
		float angle315 = MathUtils.degreesToRadians * 315.0f;
		
		angle = angle < 0.0f ? 2 * MathUtils.PI + angle : angle;

		if (angle > angle45 && angle < angle135) {
			destination.set(position.x, position.y + 1);
		}
		else if (angle > angle135 && angle < angle225) {
			destination.set(position.x - 1, position.y);
		}
		else if (angle > angle225 && angle < angle315) {
			destination.set(position.x, position.y - 1);
		}
		else {
			destination.set(position.x + 1, position.y);
		}
	}
	
	private void spawnParticleEffect (float x, float y) {
		Entity entity = new Entity();
		ParticleComponent particle = new ParticleComponent();
		ColorComponent color = new ColorComponent();
		particle.effect = Env.game.getParticlePools().obtain("data/particles/starsParticle.project");
		particle.effect.setPosition(x, y);
		particle.effect.allowCompletion();
		color.color.r = GameEnv.starsOperationColor.r;
		color.color.g = GameEnv.starsOperationColor.g;
		color.color.b = GameEnv.starsOperationColor.b;
		color.color.a = GameEnv.starsOperationColor.a;
		entity.add(particle);
//		entity.add(color);
		Env.game.getEngine().addEntity(entity);
	}
	
	private boolean move() {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		Entity map = tagSystem.getEntity(GameEnv.mapTag);
		
		if (player == null || map == null) {
			return false;
		}
		
		MapComponent mapComponent = map.getComponent(MapComponent.class);
		TiledMapTileLayer grid = (TiledMapTileLayer)mapComponent.map.getLayers().get(GameEnv.backgroundLayer);
		
		GridPositionComponent position = player.getComponent(GridPositionComponent.class);
		TransformComponent transform = player.getComponent(TransformComponent.class);
		ValueComponent value = player.getComponent(ValueComponent.class);
		
		Entity operation = getOperationAt(destination);
		
		// Use operation block
		if (operation != null) {
			OperationComponent operationComponent = operation.getComponent(OperationComponent.class);
			int newValue = operationComponent.operation.run(value.value);
			
			if (Math.abs(newValue) < GameEnv.playerMaxValue) {
				value.value = newValue;
				Env.game.getEngine().getSystem(CameraControllerSystem.class).startShakeEffect(((int)destination.x - position.x) != 0);
				
				if (GameEnv.vibrationEnabled) {
					Gdx.input.vibrate(GameEnv.playerVibrateTimeMs);
				}
				
				if (!operationComponent.persist) {
					Env.game.getEngine().removeEntity(operation);
				}
				
				TransformComponent operationTransform = operation.getComponent(TransformComponent.class);
				spawnParticleEffect(operationTransform.position.x, operationTransform.position.y);
				
				if (GameEnv.soundEnabled) {
					sfxShake.play();
				}
			}
			
			return false;
		}
		
		Engine engine = Env.game.getEngine();
		Entity keyEntity = getKeyAt(destination);
		
		if (keyEntity != null) {
			KeyComponent keyComponent = keyEntity.getComponent(KeyComponent.class);
			Values<Entity> values = doorEntities.values();
			
			while (values.hasNext()) {
				Entity doorEntity = values.next();
				DoorComponent doorComponent = doorEntity.getComponent(DoorComponent.class);
				
				if (doorComponent.id == keyComponent.id) {
					engine.removeEntity(doorEntity);
				}
			}
			
			engine.removeEntity(keyEntity);
			
			if (GameEnv.vibrationEnabled) {
				Gdx.input.vibrate(GameEnv.playerVibrateTimeMs);
			}
			
			if (GameEnv.soundEnabled) {
				sfxShake.play();
			}
		}
		
		if (isExitAt(destination)) {
			if (GameEnv.vibrationEnabled) {
				Gdx.input.vibrate(GameEnv.playerVibrateTimeMs);
			}
			
			GameEnv.game.getScreen(GameScreen.class).victory();
			
			if (GameEnv.soundEnabled) {
				sfxShake.play();
			}
			
			return false;
		}
		
		// Move (potentially through condition gate)
		if (isValidGridPosition(grid, value, destination)) {
			TextureRegion region = player.getComponent(TextureComponent.class).region;
			float destX = destination.x * region.getRegionWidth() * Env.pixelsToMetres + region.getRegionWidth() * 0.5f * Env.pixelsToMetres;
			float destY = (grid.getHeight() - destination.y - 1.0f) * region.getRegionHeight() * Env.pixelsToMetres + region.getRegionHeight() * 0.5f * Env.pixelsToMetres;
			
			Tween.to(transform, TransformTweener.Position, GameEnv.playerMoveTime)
				 .target(destX, destY, 0.0f)
				 .ease(TweenEquations.easeInOutQuad)
				 .start(Env.game.getTweenManager());
			
			position.x = (int)destination.x;
			position.y = (int)destination.y;
			
			return true;
		}
		else {
			if (GameEnv.soundEnabled) {
				sfxError.play();
			}
		}
		
		return false;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		if (!enabled) {
			return false;
		}
		
		if (GameEnv.soundEnabled) {
			sfxTap.play();
		}
		
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		
		if (player == null) {
			return false;
		}
		
		GridPositionComponent position = player.getComponent(GridPositionComponent.class);
		TransformComponent transform = player.getComponent(TransformComponent.class);
		
		mousePos3.set(x, y, 0);
		camera.unproject(mousePos3);
		
		direction.set(mousePos3.x, mousePos3.y);
		direction.sub(transform.position.x, transform.position.y);
		direction.nor();
		
		getDestinationFor(MathUtils.atan2(rightDirection.y, rightDirection.x) - MathUtils.atan2(direction.y, direction.x),
						  position,
						  destination);
		
		return move();
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		if (!enabled) {
			return false;
		}
		
		if (GameEnv.soundEnabled) {
			sfxSwipe.play();
		}
		
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		
		if (player == null) {
			return false;
		}
		
		GridPositionComponent position = player.getComponent(GridPositionComponent.class);
		
		float absVelocityX = Math.abs(velocityX);
		float absVelocityY = Math.abs(velocityY);

		destination.set(position.x, position.y);
		
		if (absVelocityX > absVelocityY) {
			destination.x += 1 * Math.signum(velocityX);
		}
		else {
			destination.y += 1 * Math.signum(velocityY);
		}
		
		return move();
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (!enabled) {
			return false;
		}
		
		if (keycode != Input.Keys.LEFT && 
			keycode != Input.Keys.RIGHT &&
			keycode != Input.Keys.UP &&
			keycode != Input.Keys.DOWN) {
			return false;
		}
		
		if (GameEnv.soundEnabled) {
			sfxTap.play();
		}
		
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		
		if (player == null) {
			return false;
		}
		
		GridPositionComponent position = player.getComponent(GridPositionComponent.class);
		
		if (keycode == Input.Keys.LEFT) {
			destination.set(position.x - 1, position.y);
		}
		else if (keycode == Input.Keys.RIGHT) {
			destination.set(position.x + 1, position.y);
		}
		else if (keycode == Input.Keys.UP) {
			destination.set(position.x, position.y - 1);
		}
		else if (keycode == Input.Keys.DOWN) {
			destination.set(position.x, position.y + 1);
		}
			
		return move();
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
