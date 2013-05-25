package com.siondream.core.entity.systems;

import java.util.Comparator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.siondream.core.Env;
import com.siondream.core.entity.components.MapComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.EntitySystem;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Values;

public class RenderingSystem extends EntitySystem implements Disposable {

	protected SpriteBatch batch;
	protected OrthographicCamera camera;
	protected ShapeRenderer shapeRenderer;
	protected IntMap<Entity> mapEntities;
	
	private IntMap<Entity> worldEntities;
	private Array<Entity> sortedEntities;
	private DepthSorter sorter;
	private Box2DDebugRenderer box2DRenderer;
	private TiledMap map;
	private OrthogonalTiledMapRenderer mapRenderer;
	
	public RenderingSystem() {
		super();
		
		this.sortedEntities = new Array<Entity>(100);
		this.batch = new SpriteBatch();
		this.camera = Env.game.getCamera();
		this.sorter = new DepthSorter();
		this.shapeRenderer = new ShapeRenderer();
		this.box2DRenderer = new Box2DDebugRenderer(Env.drawBodies,
													Env.drawJoints,
													Env.drawABBs,
													Env.drawInactiveBodies,
													Env.drawVelocities,
													Env.drawContacts);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		worldEntities = engine.getEntitiesFor(Family.getFamilyFor(TextureComponent.class, TransformComponent.class));
		mapEntities = engine.getEntitiesFor(Family.getFamilyFor(MapComponent.class));
	}

	@Override
	public void update(float deltaTime) {
		renderMap();
		
		batch.begin();
		renderWorldEntities();
		batch.end();
		
		debugDraw();
	}
	
	@Override
	public void dispose() {
		if (mapRenderer != null) {
			mapRenderer.dispose();
			mapRenderer = null;
		}
		
		batch.dispose();
		shapeRenderer.dispose();
		box2DRenderer.dispose();
	}
	
	protected void renderWorldEntities() {
		
		Values<Entity> values = worldEntities.values();
		
		while (values.hasNext()) {
			Entity entity = values.next();
			
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			
			if (isInFustrum(texture, transform)) {
				sortedEntities.add(entity);
			}
		}
		
		sortedEntities.sort(sorter);
		batch.setProjectionMatrix(camera.combined);
		
		for (Entity entity : sortedEntities) {
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			TransformComponent transform = entity.getComponent(TransformComponent.class);
		
			float scale = transform.scale * Env.pixelsToMetres;
			float width = texture.region.getRegionWidth();
			float height = texture.region.getRegionHeight();
			float originX = width * 0.5f;
			float originY = height * 0.5f;
			
			batch.draw(texture.region,
					   transform.position.x - originX,
					   transform.position.y - originY,
					   originX,
					   originY,
					   width,
					   height,
					   scale,
					   scale,
					   MathUtils.radiansToDegrees * transform.angle);
		}
		
		sortedEntities.clear();
	}
	
	protected void renderMap() {
		// If there are no map entities, dispose the renderer
		if (mapEntities.size == 0) {
			if (mapRenderer != null) {
				mapRenderer.dispose();
				mapRenderer = null;
			}
		}
		
		if (mapEntities.size > 0) {
			Entity mapEntity = mapEntities.values().next();
			MapComponent mapComponent = mapEntity.getComponent(MapComponent.class);
			
			if (map != mapComponent.map) {
				if (mapRenderer != null) {
					mapRenderer.dispose();
				}
				
				map = mapComponent.map;
				mapRenderer = new OrthogonalTiledMapRenderer(map, Env.pixelsToMetres);
			}
			
			// Render
			mapRenderer.setView(camera);
			mapRenderer.render();
		}
	}
	
	protected void renderUI() {
		Stage stage = Env.game.getStage();
		OrthographicCamera uiCamera = Env.game.getUICamera();
		stage.setCamera(uiCamera);
		stage.draw();
	}
	
	protected void debugDraw() {
		if (Env.debug) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			
			if (Env.drawGrid) {
				// Debug shapes
				shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.line(-Env.virtualWidth * 0.5f, 0.0f, Env.virtualWidth * 0.5f, 0.0f);
				shapeRenderer.line(0.0f, -Env.virtualHeight * 0.5f, 0.0f, Env.virtualHeight * 0.5f);
				
				shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
				
				for (int i = -100; i <= 100; ++i) {
					if (i == 0)
						continue;
					
					shapeRenderer.line(-Env.virtualWidth * 0.5f, i, Env.virtualWidth * 0.5f, i);
				}
				
				for (int i = -100; i <= 100; ++i) {
					if (i == 0)
						continue;
					
					shapeRenderer.line(i, -Env.virtualHeight * 0.5f, i, Env.virtualHeight * 0.5f);
				}
				
				shapeRenderer.end();
			}
			
			box2DRenderer.setDrawAABBs(Env.drawABBs);
			box2DRenderer.setDrawBodies(Env.drawBodies);
			box2DRenderer.setDrawContacts(Env.drawContacts);
			box2DRenderer.setDrawInactiveBodies(Env.drawInactiveBodies);
			box2DRenderer.setDrawJoints(Env.drawJoints);
			box2DRenderer.setDrawVelocities(Env.drawVelocities);
			box2DRenderer.render(Env.game.getWorld(), camera.combined);
		}
	}
	
	private boolean isInFustrum(TextureComponent texture, TransformComponent transform) {
		if (camera == null) {
			return false;
		}
		
		Vector3 cameraPos = camera.position;
		Vector3 position = transform.position;
		float width = texture.region.getRegionWidth();
		float height = texture.region.getRegionHeight();
		float originX = width * 0.5f;
		float originY = height * 0.5f;
		float scale = transform.scale;
		float halfWidth = camera.viewportWidth * 0.5f;
		float halfHeight = camera.viewportHeight * 0.5f;

		if (position.x + width * scale - originX < cameraPos.x - halfWidth || position.x - originX > cameraPos.x + halfWidth) return false;
		if (position.y + height * scale - originY < cameraPos.y - halfHeight || position.y - originY > cameraPos.y + halfHeight) return false;
		
		return true;
	}
	
	private class DepthSorter implements Comparator<Entity> {

		@Override
		public int compare(Entity e1, Entity e2) {
			TransformComponent t1 = e1.getComponent(TransformComponent.class);
			TransformComponent t2 = e2.getComponent(TransformComponent.class);
			
			return (int)(t1.position.z - t2.position.z);
		}
	}
}
