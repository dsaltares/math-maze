package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.ui.ShaderLabel;

public class CreditsScreen extends SionScreen {
	
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private ImageButton btnBack;
	private Table tableCredits;
	
	@Override
	public void show() {
		init();
	}

	@Override
	public void hide() {
		dispose();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		GameEnv.game.getLabelManager().update(delta);
	}
	
	private void init() {
		createUI();
		animateIn();
	}
	
	@Override
	public void dispose() {
		Env.game.getStage().clear();
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Skin skin = GameEnv.game.getSkin();
		
		imgBackground = new Image(skin, "background");
		imgLand = new Image(skin, "land");
		imgTitle = new Image(skin, "title");
		btnBack = new ImageButton(skin, "back");
		
		btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut();
			}
		});
		
		WidgetGroup labelsGroup = new WidgetGroup();
		
		Image imgSiondream = new Image(skin, "siondream");
		ShaderLabel lblRoleA = new ShaderLabel("Design & art & programming", skin, "game");
		lblRoleA.setFontScale(2.0f);
		ShaderLabel lblDavid = new ShaderLabel("David Saltares Marquez", skin, "game");
		lblDavid.setColor(0.75f, 0.75f, 0.75f, 1.0f);
		lblDavid.setFontScale(1.5f);
		ShaderLabel lblRoleB = new ShaderLabel("Powered by", skin, "game");
		lblRoleB.setFontScale(2.0f);
		ShaderLabel lbllibgdx = new ShaderLabel("libgdx", skin, "game");
		lbllibgdx.setColor(0.75f, 0.75f, 0.75f, 1.0f);
		lbllibgdx.setFontScale(1.5f);
		
		tableCredits = new Table();
		tableCredits.setSize(Env.virtualWidth, Env.virtualHeight);
		tableCredits.row();
		tableCredits.add(imgTitle).center().size(imgTitle.getWidth(), imgTitle.getHeight()).padBottom(75.0f);
		tableCredits.row();
		tableCredits.add(imgSiondream).center().size(imgSiondream.getWidth() * 0.5f, imgSiondream.getHeight() * 0.5f).padBottom(40.0f);
		tableCredits.row();
		tableCredits.add(lblRoleA).center().padBottom(20.0f);
		tableCredits.row();
		tableCredits.add(lblDavid).center().padBottom(30.0f);
		tableCredits.row();
		tableCredits.add(lblRoleB).center().padBottom(20.0f);
		tableCredits.row();
		tableCredits.add(lbllibgdx).center().padBottom(20.0f);
		tableCredits.invalidate();
		tableCredits.layout();
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(tableCredits);
		stage.addActor(imgLand);
		stage.addActor(btnBack);
		
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		btnBack.setPosition((Env.virtualWidth - btnBack.getWidth()) * 0.5f, -btnBack.getHeight());
		tableCredits.setY(-tableCredits.getHeight());
	}
	
	private void animateIn() {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					animateCredits();
				}
			}
		};
		
		timeline.beginSequence()
					// Animate in sequence
					.push(Tween.to(imgLand, ActorTweener.Position, 0.25f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(btnBack, ActorTweener.Position, 0.30f)
							   .target(btnBack.getX(), 20.0f)
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private void animateOut() {
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
				.push(Tween.to(tableCredits, ActorTweener.Color, 0.25f)
							   .target(1.0f, 1.0f, 1.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(btnBack, ActorTweener.Position, 0.20f)
						   .target(btnBack.getX(), -btnBack.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgLand, ActorTweener.Position, 0.12f)
					   .target(0.0f, -imgLand.getHeight())
					   .ease(TweenEquations.easeInOutQuad))
		.end()
		.setCallback(callback)
		.start(Env.game.getTweenManager());
	}
	
	private void animateCredits() {
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					animateOut();
				}
			}
		};
		
		Tween.to(tableCredits, ActorTweener.Position, 12.0f)
			 .target(tableCredits.getX(), Env.virtualHeight)
			 .ease(TweenEquations.easeNone)
			 .setCallback(callback)
			 .start(Env.game.getTweenManager());
	}
}
