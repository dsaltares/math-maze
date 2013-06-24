package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.siondream.core.Env;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.ui.ShaderLabel;

public class FallingLabelManager {
	private WidgetGroup group;
	private LabelPool labelsPool;
	private Array<ShaderLabel> labels;
	private float timeUntilNextSpawn;
	private String[] operators = {"+", "-", "x", "/", "%"};
	private boolean fadingOut;
	
	public FallingLabelManager(LabelStyle style, ShaderProgram shader) {
		labelsPool = new LabelPool(GameEnv.mathLabelsMaxCount, style, shader);
		labels = new Array<ShaderLabel>();
		timeUntilNextSpawn = 0.0f;
		fadingOut = false;
	}
	
	public void setGroup(WidgetGroup group) {
		this.group = group;
		
		for (ShaderLabel label : labels) {
			group.addActor(label);
		}
	}
	
	public void update(float deltaTime) {
		timeUntilNextSpawn -= deltaTime;
		
		if (!fadingOut && timeUntilNextSpawn < 0.0f && labels.size < GameEnv.mathLabelsMaxCount) {
			ShaderLabel label = labelsPool.obtain();
			
			label.setFontScale(2.5f);
			label.setText(getRandomText());
			label.setColor(GameEnv.mathLabelsColor);
			label.validate();
			label.setPosition(MathUtils.random(20.0f, Env.virtualWidth - label.getWidth() - 20.0f),
							  Env.virtualHeight + label.getHeight() + 20.0f);
			
			if (group != null) group.addActor(label);
			
			Tween.to(label, ActorTweener.Position, MathUtils.random(GameEnv.mathLabelsMinTweenTime, GameEnv.mathLabelsMaxTweenTime))
				 .target(label.getX(), -label.getHeight() - 20.0f)
				 .ease(TweenEquations.easeInQuad)
				 .setCallback(new FallenLabelCallback(label))
				 .start(Env.game.getTweenManager());
			
			labels.add(label);
			timeUntilNextSpawn = GameEnv.mathLabelsSpawnTime;
		}
	}
	
	public void startFadeOut() {
		
		TweenManager tweenManager = Env.game.getTweenManager();
		
		for (ShaderLabel label : labels){
			tweenManager.killTarget(label);
			
			Tween.to(label, ActorTweener.Color, 0.1f)
				 .target(GameEnv.mathLabelsColor.r, GameEnv.mathLabelsColor.b, GameEnv.mathLabelsColor.b, 0.0f)
				 .ease(TweenEquations.easeInQuad)
				 .start(tweenManager);
		}
		
		fadingOut = true;
	}
	
	public void dispose() {
		timeUntilNextSpawn = 0.0f;
		
		for (Label label : labels) {
			label.remove();
		}
		
		labelsPool.freeAll(labels);
		labels.clear();
		fadingOut = false;
	}
	
	private String getRandomText() {
		return "" + MathUtils.random(1, 9) + operators[MathUtils.random(operators.length - 1)] + MathUtils.random(1, 9);
	}
	
	private class LabelPool extends Pool<ShaderLabel> {

		private LabelStyle style;
		private ShaderProgram shader;
		
		public LabelPool(int maxSize, LabelStyle style, ShaderProgram shader) {
			super(maxSize);
			this.style = style;
			this.shader = shader;
		}
		
		@Override
		protected ShaderLabel newObject() {
			return new ShaderLabel("", style, shader);
		}
		
	}
	
	private class FallenLabelCallback implements TweenCallback {

		private ShaderLabel label;
		
		public FallenLabelCallback(ShaderLabel label) {
			this.label = label;
		}
		
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			if (type == TweenCallback.COMPLETE) {
				label.remove();
				labels.removeValue(label, true);
				labelsPool.free(label);
			}
		}
	}
}
