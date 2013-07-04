package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;

public class SplashScreen extends SionScreen {
	private Image imgSiondream;
	private Image imgLibgdx;
	
	@Override
	public void show() {
		init();
	}

	@Override
	public void hide() {
		dispose();
	}
	
	private void init() {
		createUI();
		animate();
	}
	
	@Override
	public void dispose() {
		Env.game.getStage().clear();
	}
	
	private void createUI() {
		Skin skin = GameEnv.game.getSkin();

		imgSiondream = new Image(skin, "siondream");
		imgLibgdx = new Image(skin, "libgdx");
		
		imgSiondream.setPosition((Env.virtualWidth - imgSiondream.getWidth()) * 0.5f,
								 (Env.virtualHeight - imgSiondream.getHeight()) * 0.5f);
		imgLibgdx.setPosition((Env.virtualWidth - imgLibgdx.getWidth()) * 0.5f,
				 			  (Env.virtualHeight - imgLibgdx.getHeight()) * 0.5f);
		
		imgSiondream.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		imgLibgdx.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		Stage stage = Env.game.getStage();
		stage.addActor(imgLibgdx);
		stage.addActor(imgSiondream);
	}
	
	private void animate() {
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					Env.game.setScreen(MenuScreen.class);
				}
			}
		};
		
		Timeline timeline = Timeline.createSequence();
		
		timeline.beginSequence()
					.push(Tween.to(imgSiondream, ActorTweener.Color, 0.4f)
							   .target(1.0f, 1.0f, 1.0f, 1.0f)
							   .ease(TweenEquations.easeInQuad))
					.pushPause(1.0f)
					.push(Tween.to(imgSiondream, ActorTweener.Color, 0.75f)
							   .target(0.0f, 0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInQuad))
					.push(Tween.to(imgLibgdx, ActorTweener.Color, 0.4f)
							   .target(1.0f, 1.0f, 1.0f, 1.0f)
							   .ease(TweenEquations.easeInQuad))
					.pushPause(1.0f)
					.push(Tween.to(imgLibgdx, ActorTweener.Color, 0.75f)
							   .target(0.0f, 0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
}
