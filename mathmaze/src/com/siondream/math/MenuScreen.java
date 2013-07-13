package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;

public class MenuScreen extends SionScreen {
	
	private Image imgTwitter;
	private Image imgFacebook;
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private Table socialTable;
	private Table buttonsTable;
	private ImageButton btnSound;
	private ImageButton btnInfo;
	private ImageButton btnPlay;
	
	private Sound sfxTap;
	
	public MenuScreen() {
		super();
	}
		
	@Override
	public void show() {
		init();
	}

	@Override
	public void hide() {
		dispose();
	}
	
	private void init() {
		Music song = GameEnv.game.getMusic();
		song.setVolume(GameEnv.soundEnabled ? GameEnv.musicVolume : 0.0f);
		song.setLooping(true);
		song.play();
		
		createUI();
		animateIn();
	}
	
	@Override
	public void dispose() {
		Env.game.getStage().clear();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		GameEnv.game.getLabelManager().update(delta);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Skin skin = GameEnv.game.getSkin();
		
		imgBackground = new Image(skin, "background");
		imgLand = new Image(skin, "land");
		imgFacebook = new Image(skin, "facebook");
		imgTwitter = new Image(skin, "twitter");
		
		imgTitle = new Image(skin, "title");
		
		btnPlay = new ImageButton(skin, "play");
		btnInfo = new ImageButton(skin, "info");
		btnSound = new ImageButton(skin, "music");
		
		socialTable = new Table();
		socialTable.row();
		socialTable.add(imgFacebook).width(imgFacebook.getWidth()).height(imgFacebook.getHeight()).padRight(20.0f);
		socialTable.add(imgTwitter).width(imgFacebook.getWidth()).height(imgFacebook.getHeight());
		socialTable.layout();
		socialTable.setWidth(imgFacebook.getWidth() + 20.0f + imgTwitter.getWidth());
		socialTable.setHeight(imgFacebook.getHeight());
		
		buttonsTable = new Table();
		buttonsTable.row();
		buttonsTable.add(btnInfo).padBottom(20.0f);
		buttonsTable.row();
		buttonsTable.add(btnSound);
		buttonsTable.validate();
		buttonsTable.setSize(btnInfo.getWidth(), btnInfo.getHeight() + btnSound.getHeight() + 20.0f);
		
		btnPlay.setPosition((Env.virtualWidth - btnPlay.getWidth()) * 0.5f, -btnPlay.getHeight());
		buttonsTable.setPosition(Env.virtualWidth + buttonsTable.getWidth() * 0.5f,  20.0f + buttonsTable.getHeight() * 0.5f);
		
		Preferences preferences = GameEnv.game.getPreferences();
		btnSound.setDisabled(!preferences.getBoolean("soundEnabled", true));
		
		WidgetGroup labelsGroup = new WidgetGroup();
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(imgTitle);
		stage.addActor(btnPlay);
		stage.addActor(imgLand);
		stage.addActor(buttonsTable);
		stage.addActor(socialTable);
		
		
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		imgFacebook.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				Env.platform.openURL("http://facebook.com/siondream");
			}
		});
		
		imgTwitter.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				Env.platform.openURL("http://twitter.com/siondream");
			}
		});
		
		btnPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		btnSound.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				sfxTap.play();
				
				boolean wasDisabled = btnSound.isDisabled();
				btnSound.setDisabled(!wasDisabled);
				Preferences preferences = GameEnv.game.getPreferences();
				GameEnv.soundEnabled = !btnSound.isDisabled();
				preferences.putBoolean("soundEnabled", GameEnv.soundEnabled);
				preferences.flush();
				GameEnv.game.getMusic().setVolume(GameEnv.soundEnabled ? GameEnv.musicVolume : 0.0f);
			}
		});
		
		btnInfo.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				
				animateOut(CreditsScreen.class);
			}
		});
		
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		socialTable.setPosition(-socialTable.getWidth(), 20.0f);
		
		Assets assets = Env.game.getAssets();
		sfxTap = assets.get("data/sfx/tap.mp3", Sound.class);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK) {
			Gdx.app.exit();
			return true;
		}
		
		return false;
	}
	
	private void animateIn() {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					animateTitle();
					animatePlay();
				}
			}
		};
		
		timeline.beginSequence()
					// Animate in sequence
					.push(Tween.to(imgLand, ActorTweener.Position, 0.25f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.2f)
							   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight - imgTitle.getHeight() - 60.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(btnPlay, ActorTweener.Position, 0.30f)
							   .target(btnPlay.getX(), (Env.virtualHeight - btnPlay.getHeight()) * 0.5f)
							   .ease(TweenEquations.easeInOutQuad))
					.beginParallel()
					.push(Tween.to(socialTable, ActorTweener.Position, 0.12f)
							   .target(20.0f, 20.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(buttonsTable, ActorTweener.Position, 0.12f)
							   .target(Env.virtualWidth - buttonsTable.getWidth() * 0.5f - 20.0f, buttonsTable.getY())
							   .ease(TweenEquations.easeInOutQuad))
					.end()
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private void animateOut(final Class<? extends SionScreen> screenType) {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					Env.game.setScreen(screenType);
				}
			}
		};
		
		timeline.beginSequence()
					.beginParallel()
						.push(Tween.to(socialTable, ActorTweener.Position, 0.05f)
								   .target(-socialTable.getWidth(), 20.0f)
								   .ease(TweenEquations.easeInOutQuad))
						.push(Tween.to(buttonsTable, ActorTweener.Position, 0.25f)
								   .target(Env.virtualWidth + buttonsTable.getWidth() * 0.5f, buttonsTable.getY())
								   .ease(TweenEquations.easeInOutQuad))
					.end()
					.push(Tween.to(btnPlay, ActorTweener.Position, 0.20f)
							   .target(btnPlay.getX(), -btnPlay.getHeight())
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgLand, ActorTweener.Position, 0.12f)
						   .target(0.0f, -imgLand.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.12f)
						   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.end()
				.setCallback(callback)
				.start(Env.game.getTweenManager());
	}
	
	private void animateTitle() {
		Timeline timeline = Timeline.createSequence();

		timeline.beginSequence()
				.push(Tween.to(imgTitle, ActorTweener.Scale, 0.5f)
						   .target(1.1f, 1.1f)
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgTitle, ActorTweener.Scale, 0.5f)
						   .target(1.0f, 1.0f)
						   .ease(TweenEquations.easeInOutQuad))
				.end()
				.repeat(Tween.INFINITY, 0.3f)
				.start(Env.game.getTweenManager());
	}

	private void animatePlay() {
		Timeline timeline = Timeline.createSequence();

		timeline.beginSequence()
				.push(Tween.to(btnPlay, ActorTweener.Scale, 0.5f)
						   .target(1.5f, 1.5f)
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(btnPlay, ActorTweener.Scale, 0.5f)
						   .target(0.8f, 0.8f)
						   .ease(TweenEquations.easeInOutQuad))
				.end()
				.repeat(Tween.INFINITY, 0.1f)
				.start(Env.game.getTweenManager());
	}
}
