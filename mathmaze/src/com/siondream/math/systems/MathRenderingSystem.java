package com.siondream.math.systems;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Values;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.siondream.core.Env;
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
import com.siondream.math.components.OperationComponent;
import com.siondream.math.components.ValueComponent;

public class MathRenderingSystem extends RenderingSystem {
	private IntMap<Entity> operationEntities;
	private IntMap<Entity> conditionEntities;
	private StringBuilder string;
	private TextBounds textBounds;
	
	private boolean renderMap;
	
	public MathRenderingSystem() {
		super();
		
		string = new StringBuilder();
		textBounds = new TextBounds();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		operationEntities = engine.getEntitiesFor(Family.getFamilyFor(FontComponent.class,
																	  TextureComponent.class,
																	  TransformComponent.class,
																	  OperationComponent.class));
		
		conditionEntities = engine.getEntitiesFor(Family.getFamilyFor(FontComponent.class,
																	  TextureComponent.class,
																	  TransformComponent.class,
																	  ConditionComponent.class));
		
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
			float x = GameEnv.cameraScreenPos.x * ratioWidth;
			float y = GameEnv.cameraScreenPos.y * ratioHeight;
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
			renderOperations();
			renderConditions();
			renderPlayerValue();
			batch.end();
			Gdx.gl.glDisable(GL11.GL_SCISSOR_TEST);
		}
		
		debugDraw();
	}
	
	public void setRenderMap(boolean renderMap) {
		this.renderMap = renderMap;
	}
	
	private void renderOperations() {
		Values<Entity> operations = operationEntities.values();
		
		while(operations.hasNext()) {
			Entity entity = operations.next();
			OperationComponent operation = entity.getComponent(OperationComponent.class);
			FontComponent font = entity.getComponent(FontComponent.class);
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			ShaderComponent shader = entity.getComponent(ShaderComponent.class);
			
			operationToString(operation.operation, string);
			
			font.font.getBounds(string, textBounds);
			
			float width = texture.region.getRegionWidth() * Env.pixelsToMetres;
			float height = texture.region.getRegionHeight() * Env.pixelsToMetres;
			float startX = transform.position.x - width * 0.5f;
			float startY = transform.position.y - height * 0.5f;
			
			if (shader != null) batch.setShader(shader.shader);
			
			font.font.drawWrapped(batch,
								  string,
								  startX + (width - textBounds.width) * 0.5f,
								  startY + height * 0.5f + (height - textBounds.height) * 0.5f,
								  texture.region.getRegionWidth() * Env.pixelsToMetres);
			
			if (shader != null) batch.setShader(null);
		}
	}
	
	private void renderConditions() {
		Values<Entity> conditions = conditionEntities.values();
		
		while(conditions.hasNext()) {
			Entity entity = conditions.next();
			ConditionComponent condition = entity.getComponent(ConditionComponent.class);
			FontComponent font = entity.getComponent(FontComponent.class);
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			ShaderComponent shader = entity.getComponent(ShaderComponent.class);
			
			conditionsToString(condition.conditions, string);
			
			font.font.getBounds(string, textBounds);
			
			float width = texture.region.getRegionWidth() * Env.pixelsToMetres;
			float height = texture.region.getRegionHeight() * Env.pixelsToMetres;
			float startX = transform.position.x - width * 0.5f;
			float startY = transform.position.y - height * 0.5f;
			
			if (shader != null) batch.setShader(shader.shader);
			font.font.drawWrapped(batch,
								  string,
								  startX + (width - textBounds.width) * 0.5f,
								  startY + height * 0.5f + (height - textBounds.height) * 0.5f,
								  texture.region.getRegionWidth() * Env.pixelsToMetres);
			if (shader != null) batch.setShader(null);
		}
	}
	
	private void renderPlayerValue() {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity entity = tagSystem.getEntity(GameEnv.playerTag);
		
		if (entity == null)
			return;
		
		FontComponent font = entity.getComponent(FontComponent.class);
		TextureComponent texture = entity.getComponent(TextureComponent.class);
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		ValueComponent value = entity.getComponent(ValueComponent.class);
		ShaderComponent shader = entity.getComponent(ShaderComponent.class);
		
		if (string.length() > 0)
			string.delete(0, string.length());
		
		string.append(value.value);
		font.font.getBounds(string, textBounds);
		
		float width = texture.region.getRegionWidth() * Env.pixelsToMetres;
		float height = texture.region.getRegionHeight() * Env.pixelsToMetres;
		float startX = transform.position.x - width * 0.5f;
		float startY = transform.position.y - height * 0.5f;
		
		if (shader != null) batch.setShader(shader.shader);
		font.font.drawWrapped(batch,
							  string,
							  startX + (width - textBounds.width) * 0.5f,
							  startY + height * 0.5f + (height - textBounds.height) * 0.5f,
							  texture.region.getRegionWidth() * Env.pixelsToMetres);
		if (shader != null) batch.setShader(null);
	}
	
	private void operationToString(Operation operation, StringBuilder string) {
		if (string.length() > 0)
			string.delete(0, string.length());
		
		switch (operation.getType()) {
		case Addition:
			string.append("+ ");
			break;
		case Division:
			string.append("/ ");
			break;
		case Mod:
			string.append("% ");
			break;
		case Product:
			string.append("x ");
			break;
		case Substraction:
			string.append("- ");
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
				string.append("= ");
				break;
			case Greater:
				string.append("> ");
				break;
			case GreaterOrEquals:
				string.append(">= ");
				break;
			case Lesser:
				string.append("< ");
				break;
			case LesserOrEquals:
				string.append("<= ");
				break;
			case NotEquals:
				string.append("!= ");
				break;
			default:
				break;
			
			}
			
			string.append(condition.getValue());
			string.append("\n");
		}
	}
}
