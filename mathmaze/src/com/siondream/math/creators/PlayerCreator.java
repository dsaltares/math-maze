package com.siondream.math.creators;

import ashley.core.Engine;
import ashley.core.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.EntityFactory.EntityCreator;
import com.siondream.core.ShaderManager;
import com.siondream.core.entity.components.ColorComponent;
import com.siondream.core.entity.components.FontComponent;
import com.siondream.core.entity.components.ShaderComponent;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.GameEnv;
import com.siondream.math.components.GridPositionComponent;
import com.siondream.math.components.ValueComponent;

public class PlayerCreator implements EntityCreator {

	public static class PlayerParams {
		public PlayerParams(Map map, MapObject object) {
			this.map = map;
			this.object = object;
		}
		
		public Map map;
		public MapObject object;
	}
	
	private static final String TAG = "PlayerCreator";
	
	private Logger logger;
	
	public PlayerCreator() {
		logger = new Logger(TAG, Env.debugLevel);
	}
	
	@Override
	public Entity createEntity(Object params) {
		if (!(params instanceof PlayerParams)) {
			logger.error("invalid params " + params);
			return null;
		}
		
		PlayerParams playerParams = (PlayerParams)params;
		
		RectangleMapObject rectangleObject = (RectangleMapObject)playerParams.object;
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		Engine engine = Env.game.getEngine();
		TagSystem tagSystem = engine.getSystem(TagSystem.class);
		ShaderManager shaderManager = Env.game.getShaderManager();
		MapProperties properties = rectangleObject.getProperties();
		TiledMapTileLayer tileLayer = (TiledMapTileLayer)playerParams.map.getLayers().get(GameEnv.backgroundLayer);
		
		Entity player = new Entity();
		TextureComponent texture = new TextureComponent();
		GridPositionComponent position = new GridPositionComponent();
		TransformComponent transform = new TransformComponent();
		ValueComponent value = new ValueComponent();
		FontComponent font = new FontComponent();
		ShaderComponent shader = new ShaderComponent();
		ColorComponent color = new ColorComponent();
		
		player.add(position);
		player.add(texture);
		player.add(transform);
		player.add(value);
		player.add(font);
		player.add(shader);
		player.add(color);
		
		texture.region = new TextureRegion(atlas.findRegion("player"));
		color.color = Color.BLACK.cpy();
		Rectangle rectangle = rectangleObject.getRectangle();
		position.x = (int)(rectangle.x / rectangle.width);
		position.y = tileLayer.getHeight() - (int)(rectangle.y / rectangle.height) - 1;
		transform.position.x = (position.x * tileLayer.getTileWidth() + texture.region.getRegionWidth() * 0.5f) * Env.pixelsToMetres;
		transform.position.y = ((tileLayer.getHeight() - position.y - 1) * tileLayer.getTileHeight() + texture.region.getRegionHeight() * 0.5f) * Env.pixelsToMetres;
		font.font = skin.getFont("gameFont");
		shader.shader = shaderManager.get("font");
		value.value = Integer.parseInt(properties.get("value", "0", String.class));
		
		engine.addEntity(player);
		
		tagSystem.setTag(player, GameEnv.playerTag);
		
		return player;
	}
}
