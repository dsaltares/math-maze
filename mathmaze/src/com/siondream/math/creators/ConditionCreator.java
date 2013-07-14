package com.siondream.math.creators;

import ashley.core.Engine;
import ashley.core.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.entity.components.ColorComponent;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.ShaderComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.GroupSystem;
import com.siondream.math.Condition;
import com.siondream.math.GameEnv;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.GridPositionComponent;

public class ConditionCreator implements EntityCreator {

	public static class ConditionParams {
		public ConditionParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public Map map;
		public MapObject object;
	}
	
	private static final String TAG = "ConditionCreator";
	
	private Logger logger;
	
	public ConditionCreator() {
		logger = new Logger(TAG, Env.debugLevel);
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof ConditionParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		ConditionParams conditionParams = (ConditionParams)params;
		Engine engine = Env.game.getEngine();
		GroupSystem groupSystem = engine.getSystem(GroupSystem.class);
		RectangleMapObject rectangleObject = (RectangleMapObject)conditionParams.object;
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)conditionParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		String name = conditionParams.object.getName();
		String[] parts = name.split(":");
		
		if (parts.length < 2 || parts.length % 2 != 0) {
			logger.error("invalid condition entity " + name);
			return null;
		}
		
		Entity condition = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		FontComponent font = new FontComponent();
		ShaderComponent shader = new ShaderComponent();
		ColorComponent color = new ColorComponent();
		ConditionComponent conditionComponent = new ConditionComponent();
		
		Array<Condition> conditions = new Array<Condition>();
		
		for (int j = 0; j < parts.length; j = j + 2) {
			conditions.add(new Condition(parts[j], Integer.parseInt(parts[j + 1])));
		}
		
		conditionComponent.conditions = conditions;
		condition.add(texture);
		condition.add(position);
		condition.add(transform);
		condition.add(conditionComponent);
		condition.add(font);
		condition.add(shader);
		condition.add(color);
		
		texture.region = new TextureRegion(atlas.findRegion("checkpoint"));
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		transform.position.z = 100.0f;
		font.font = skin.getFont("gameFont");
		shader.shader = Env.game.getShaderManager().get("font");
		color.color = Color.BLACK.cpy();
		
		engine.addEntity(condition);
		
		groupSystem.register(condition, GameEnv.conditionsGroup);
		
		return condition;
	}

}
