package com.siondream.math.systems;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Values;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.entity.components.ColorComponent;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.ShaderComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.RenderingSystem;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.Condition;
import com.siondream.math.GameEnv;
import com.siondream.math.Operation;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;

public class MathRenderingSystem extends RenderingSystem {
	private final static String TAG = "MathRenderingSystem";
	
	private Logger logger;
	private IntMap<Entity> fontEntities;
	private StringBuilder string;
	private TextBounds textBounds;
	
	private boolean renderMap;
	
	public MathRenderingSystem() {
		super();
		
		logger = new Logger(TAG, Env.debugLevel);
		string = new StringBuilder();
		textBounds = new TextBounds();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		fontEntities = engine.getEntitiesFor(Family.getFamilyFor(FontComponent.class,
																 TextureComponent.class,
																 TransformComponent.class,
																 ColorComponent.class));
		
		renderMap = true;
	}
	
	@Override
	public void update(float deltaTime) {
		renderUI();
		
		if (renderMap) {
			Gdx.gl.glEnable(GL11.GL_SCISSOR_TEST);
			
			// Neeed to figure out real coordinates
			Rectangle viewport = Env.game.getViewport();
			float ratioWidth = viewport.width / Env.virtualWidth;
			float ratioHeight = viewport.height / Env.virtualHeight;
			float x = viewport.x + GameEnv.cameraScreenPos.x * ratioWidth;
			float y = viewport.y + GameEnv.cameraScreenPos.y * ratioHeight;
			float width = GameEnv.cameraWidth * ratioWidth;
			float height = GameEnv.cameraHeight * ratioHeight;
			
			Gdx.gl.glScissor(MathUtils.ceil(x),
							 MathUtils.ceil(y),
							 MathUtils.floor(width),
							 MathUtils.floor(height));
			
			renderMap();
			
			batch.setProjectionMatrix(camera.combined);
			batch.enableBlending();
			batch.begin();
			renderWorldEntities();
			renderFontEntities();
			batch.end();
			
			//renderParticles();
			
			Gdx.gl.glDisable(GL11.GL_SCISSOR_TEST);
		}
		
		debugDraw();
	}
	
	public void setRenderMap(boolean renderMap) {
		this.renderMap = renderMap;
	}
	
	private void renderFontEntities() {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		GridPositionComponent playerGridPos = player != null ? player.getComponent(GridPositionComponent.class) : null;
		
		Values<Entity> entities = fontEntities.values();
		
		while (entities.hasNext) {
			Entity entity = entities.next();
			
			GridPositionComponent gridPos = entity.getComponent(GridPositionComponent.class);
			
			boolean isPlayer = entity.hasComponent(ValueComponent.class);
			
			if (gridPos != null &&
				playerGridPos != null &&
				!isPlayer && 
				gridPos.x == playerGridPos.x &&
				gridPos.y == playerGridPos.y) {
				continue;
			}
			
			FontComponent font = entity.getComponent(FontComponent.class);
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			ShaderComponent shader = entity.getComponent(ShaderComponent.class);
			ColorComponent color = entity.getComponent(ColorComponent.class);
			
			boolean validEntity = false;
			
			if (entity.hasComponent(OperationComponent.class)) {
				OperationComponent operation = entity.getComponent(OperationComponent.class);
				operationToString(operation.operation, string);
				validEntity = true;
			}
			else if (entity.hasComponent(ConditionComponent.class)) {
				ConditionComponent condition = entity.getComponent(ConditionComponent.class);
				conditionsToString(condition.conditions, string);
				validEntity = true;
			}
			else if (isPlayer) {
				ValueComponent value = entity.getComponent(ValueComponent.class);
				
				if (string.length() > 0)
					string.delete(0, string.length());
				
				string.append(value.value);
				
				validEntity = true;
			}
			
			if (!validEntity) {
				continue;
			}
			
			float scale = font.font.getScaleX();
			font.font.setScale(1.75f);
			
			Color oldColor = font.font.getColor();
			font.font.setColor(color.color);
			
			font.font.getBounds(string, textBounds);
			
			float width = texture.region.getRegionWidth() * Env.pixelsToMetres;
			float height = texture.region.getRegionHeight() * Env.pixelsToMetres;
			float startX = transform.position.x - width * 0.5f;
			float startY = transform.position.y - height * 0.5f;
			
			if (shader != null) batch.setShader(shader.shader);
			
			font.font.draw(batch,
						   string,
						   startX + (width - textBounds.width) * 0.5f,
						   startY + height * 0.55f * font.font.getScaleY() + (height - textBounds.height) * 0.5f);
			
			if (shader != null) batch.setShader(null);
			font.font.setScale(scale);
			font.font.setColor(oldColor);
		}
	}
	
	private void operationToString(Operation operation, StringBuilder string) {
		if (string.length() > 0)
			string.delete(0, string.length());
		
		switch (operation.getType()) {
		case Addition:
			string.append("+");
			break;
		case Division:
			string.append("/");
			break;
		case Mod:
			string.append("%");
			break;
		case Product:
			string.append("x");
			break;
		case Substraction:
			string.append("-");
			break;
		default:
			break;
		}
		
		string.append(operation.getValue());
	}
	
	private void conditionsToString(Array<Condition> conditions, StringBuilder string) {
		string.delete(0, string.length());
		
		for (Condition condition : conditions) {
			switch (condition.getType()) {
			case Equals:
				string.append("=");
				break;
			case Greater:
				string.append(">");
				break;
			case GreaterOrEquals:
				string.append(">=");
				break;
			case Lesser:
				string.append("<");
				break;
			case LesserOrEquals:
				string.append("<=");
				break;
			case NotEquals:
				string.append("!=");
				break;
			default:
				break;
			
			}
			
			string.append(condition.getValue());
			string.append("\n");
		}
	}
}
