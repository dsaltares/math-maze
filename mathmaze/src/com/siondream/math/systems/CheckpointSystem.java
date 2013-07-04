package com.siondream.math.systems;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.siondream.core.Env;
import com.siondream.core.entity.components.TextureComponent;
import com.siondream.core.entity.systems.TagSystem;
import com.siondream.math.Condition;
import com.siondream.math.GameEnv;
import com.siondream.math.components.ConditionComponent;
import com.siondream.math.components.ValueComponent;

import ashley.core.Engine;
import ashley.core.Entity;
import ashley.core.EntitySystem;
import ashley.core.Family;
import ashley.utils.IntMap;
import ashley.utils.IntMap.Values;

public class CheckpointSystem extends EntitySystem {
	
	private IntMap<Entity> conditionEntities;
	private TextureRegion open;
	private TextureRegion closed;
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		
		conditionEntities = engine.getEntitiesFor(Family.getFamilyFor(TextureComponent.class,
																	  ConditionComponent.class));
		
		TextureAtlas atlas = GameEnv.game.getSkin().getAtlas();
		open = new TextureRegion(atlas.findRegion("checkpoint-open"));
		closed = new TextureRegion(atlas.findRegion("checkpoint"));
	}
	
	@Override
	public void update(float deltaTime) {
		TagSystem tagSystem = Env.game.getEngine().getSystem(TagSystem.class);
		Entity player = tagSystem.getEntity(GameEnv.playerTag);
		
		if (player == null)
			return;	
			
		ValueComponent value = player.getComponent(ValueComponent.class);
		
		Values<Entity> conditions = conditionEntities.values();
		while(conditions.hasNext()) {
			Entity entity = conditions.next();
			ConditionComponent condition = entity.getComponent(ConditionComponent.class);
			TextureComponent texture = entity.getComponent(TextureComponent.class);
			boolean check = true;
			
			for (Condition c : condition.conditions) {
				if (!c.check(value.value)) {
					check = false;
					break;
				}
			}
			
			texture.region = check ? open : closed;
		}
	}
}
