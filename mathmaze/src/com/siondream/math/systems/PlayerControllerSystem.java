package com.siondream.math.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.core.tweeners.TransformTweener;
import com.siondream.math.Condition;
import com.siondream.math.GameEnv;
import com.siondream.math.GameScreen;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.EntitySystem;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Values;
import ashley.utils.MathUtils;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

public class PlayerControllerSystem extends EntitySystem implements InputProcessor {
	
	private static final String TAG = "PlayerControllerSystem";
	
	private float moveTimer;
	private Logger logger;
	private OrthographicCamera camera;
	private Vector3 mousePos3;
	private Vector2 mousePos;
	private Vector2 direction;
	private Vector2 destination;
	private Vector2 rightDirection;
	private boolean moving;
	private PlayerMoveCallback callback;
	private IntMap<Entity> conditionEntities;
	private IntMap<Entity> operationEntities;
	
	public PlayerControllerSystem() {
		super();
		
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		moveTimer = 0.0f;
		camera = Env.game.getCamera();
		mousePos3 = new Vector3();
		mousePos = new Vector2();
		direction = new Vector2();
		destination = new Vector2();
		rightDirection = Vector2.X.cpy();
		moving = false;
		callback = new PlayerMoveCallback();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		operationEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
																	  OperationComponent.class));
		
		conditionEntities = engine.getEntitiesFor(Family.getFamilyFor(GridPositionComponent.class,
																	  ConditionComponent.class));
	}
	
	@Override
	public void update(float deltaTime) {
		if (moveTimer > 0.0f) {
			moveTimer -= deltaTime;
			return;
		}
		
		GameScreen screen = Env.game.getScreen(GameScreen.TAG, GameScreen.class);
		
		if (!moving && Gdx.input.isTouched()) {
			TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
			Entity player = tagSystem.getEntity(GameEnv.playerTag);
			Entity map = tagSystem.getEntity(GameEnv.mapTag);
			
			if (player == null || map == null) {
				return;
			}
			
			MapComponent mapComponent = map.getComponent(MapComponent.class);
			TiledMapTileLayer grid = (TiledMapTileLayer)mapComponent.map.getLayers().get(GameEnv.backgroundLayer);
			
			GridPositionComponent position = player.getComponent(GridPositionComponent.class);
			TransformComponent transform = player.getComponent(TransformComponent.class);
			
			mousePos3.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
			camera.unproject(mousePos3);
			
			direction.set(mousePos3.x, mousePos3.y);
			direction.sub(transform.position.x, transform.position.y);
			direction.nor();
			
			getDestinationFor(MathUtils.atan2(rightDirection.y, rightDirection.x) - MathUtils.atan2(direction.y, direction.x),
							  position,
							  destination);
			
			ValueComponent value = player.getComponent(ValueComponent.class);
			
			Entity operation = getOperationAt(destination);
			
			// Use operation block
			if (operation != null) {
				OperationComponent operationComponent = operation.getComponent(OperationComponent.class);
				value.value = operationComponent.operation.run(value.value);
				moveTimer = GameEnv.playerMoveCooldown * 2.0f;
			}
			// Move (potentially through condition gate)
			else if (isValidGridPosition(grid, value, destination)) {
				TextureRegion region = player.getComponent(TextureComponent.class).region;
				float destX = destination.x * region.getRegionWidth() * Env.pixelsToMetres + region.getRegionWidth() * 0.5f * Env.pixelsToMetres;
				float destY = (grid.getHeight() - destination.y - 1.0f) * region.getRegionHeight() * Env.pixelsToMetres + region.getRegionHeight() * 0.5f * Env.pixelsToMetres;
				
				Tween.to(transform, TransformTweener.Position, GameEnv.playerMoveTime)
					 .target(destX, destY, 0.0f)
					 .ease(TweenEquations.easeInOutQuad)
					 .setCallback(callback)
					 .start(Env.game.getTweenManager());
				
				moving = true;
			}
		}
	}
	
	public void cancelMovement() {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		TransformComponent transform = player.getComponent(TransformComponent.class);
		Env.game.getTweenManager().killTarget(transform);
		moving = false;
		moveTimer = 0.0f;
	}
	
	private void getGridPosition(TiledMapTileLayer grid, Vector3 worldPosition, Vector2 gridPosition) {
		gridPosition.set((int)(worldPosition.x * Env.metresToPixels / grid.getTileWidth()),
						 (int)(grid.getHeight() - worldPosition.y * Env.metresToPixels / grid.getTileHeight()));
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
	
	private class PlayerMoveCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			if (type == TweenCallback.COMPLETE) {
				TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
				Entity player = tagSystem.getEntity(GameEnv.playerTag);
				GridPositionComponent position = player.getComponent(GridPositionComponent.class);
				position.x = (int)destination.x;
				position.y = (int)destination.y;
				moving = false;
				moveTimer = GameEnv.playerMoveCooldown;
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
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
		
		
		
		
		
		
		return true;
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
