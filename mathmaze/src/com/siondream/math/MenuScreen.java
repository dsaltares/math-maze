package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.ui.ShaderButton;
import com.siondream.math.ui.ShaderButton.ShaderButtonStyle;

public class MenuScreen extends SionScreen {
	
	private Image imgTwitter;
	private Image imgFacebook;
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private WidgetGroup buttonsGroup;
	private Table socialTable;
	private ShaderButton btnPlay;
	private ShaderButton btnAbout;
	private Texture fontTexture;
	private BitmapFont font;
	private ShaderProgram fontShader;
	
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
		createUI();
		animateIn();
	}
	
	@Override
	public void dispose() {
		Env.game.getStage().clear();
		font.dispose();
		fontTexture.dispose();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		GameEnv.game.getLabelManager().update(delta);
	}
	
	private void createUI() {
		fontTexture = new Texture(Gdx.files.internal("data/ui/chicken.png"), true);
		fontTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("data/ui/chicken.fnt"), new TextureRegion(fontTexture), false);
		
		fontShader = new ShaderProgram(Gdx.files.internal("data/ui/font.vert"), 
													 Gdx.files.internal("data/ui/font.frag"));
		if (!fontShader.isCompiled()) {
		    Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
		}
		
		Assets assets = Env.game.getAssets();
		Stage stage = Env.game.getStage();
		
		imgBackground = new Image(assets.get("data/ui/background.png", Texture.class));
		imgLand = new Image(assets.get("data/ui/land.png", Texture.class));
		imgFacebook = new Image(assets.get("data/ui/facebook.png", Texture.class));
		imgTwitter = new Image(assets.get("data/ui/twitter.png", Texture.class));
		
		imgTitle = new Image(assets.get("data/ui/title.png", Texture.class));
		
		Texture upText = assets.get("data/ui/upButton.png", Texture.class);
		upText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		Texture downText = assets.get("data/ui/downButton.png", Texture.class);
		downText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegionDrawable upButton = new TextureRegionDrawable(new TextureRegion(upText));
		TextureRegionDrawable downButton = new TextureRegionDrawable(new TextureRegion(downText));
		
		ShaderButtonStyle buttonStyle = new ShaderButtonStyle();
		buttonStyle.down = downButton;
		buttonStyle.up = upButton;
		buttonStyle.font = font;
		buttonStyle.shader = fontShader;
		buttonStyle.backGroundColor = Color.WHITE;
		buttonStyle.fontColor = Color.BLACK;
		
		btnPlay = new ShaderButton("Play", buttonStyle);
		btnAbout = new ShaderButton("About", buttonStyle);
		
		btnPlay.setScale(4.25f);
		btnPlay.setSize(650.0f, 150.0f);
		btnAbout.setScale(3.25f);
		btnAbout.setSize(400.0f, 125.0f);
		
		socialTable = new Table();
		socialTable.row();
		socialTable.add(imgFacebook).width(imgFacebook.getWidth()).height(imgFacebook.getHeight()).padRight(20.0f);
		socialTable.add(imgTwitter).width(imgFacebook.getWidth()).height(imgFacebook.getHeight());
		socialTable.layout();
		socialTable.setWidth(imgFacebook.getWidth() + 20.0f + imgTwitter.getWidth());
		socialTable.setHeight(imgFacebook.getHeight());
		
		buttonsGroup = new WidgetGroup();
		buttonsGroup.setSize(Env.virtualWidth, Env.virtualHeight);
		buttonsGroup.addActor(btnPlay);
		buttonsGroup.addActor(btnAbout);
		
		btnPlay.setPosition((buttonsGroup.getWidth() - btnPlay.getWidth()) * 0.5f, 600.0f);
		btnAbout.setPosition((buttonsGroup.getWidth() - btnAbout.getWidth()) * 0.5f, btnPlay.getY() - btnPlay.getHeight() - 30.0f);
		
		WidgetGroup labelsGroup = new WidgetGroup();
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(imgTitle);
		stage.addActor(buttonsGroup);
		stage.addActor(imgLand);
		stage.addActor(socialTable);
		
		
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		imgFacebook.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Env.platform.openURL("http://siondream.com");
			}
		});
		
		imgTwitter.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Env.platform.openURL("http://twitter.com/siondream");
			}
		});
		
		btnPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(LevelSelectionScreen.class);
			}
		});
		
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		buttonsGroup.setPosition(0.0f, -Env.virtualHeight);
		socialTable.setPosition(-socialTable.getWidth(), 20.0f);
	}
	
	private void animateIn() {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					animateTitle();
				}
			}
		};
		
		timeline.beginSequence()
					// Animate in sequence
					.push(Tween.to(imgLand, ActorTweener.Position, 0.5f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(imgTitle, ActorTweener.Position, 0.4f)
							   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight - imgTitle.getHeight() - 60.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(buttonsGroup, ActorTweener.Position, 0.40f)
							   .target(0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(socialTable, ActorTweener.Position, 0.25f)
							   .target(20.0f, 20.0f)
							   .ease(TweenEquations.easeInOutQuad))
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
					.push(Tween.to(socialTable, ActorTweener.Position, 0.25f)
							   .target(-socialTable.getWidth(), 20.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.beginParallel()
						.push(Tween.to(buttonsGroup, ActorTweener.Position, 0.40f)
								   .target(0.0f, -Env.virtualHeight)
								   .ease(TweenEquations.easeInOutQuad))
						.push(Tween.to(imgTitle, ActorTweener.Position, 0.25f)
								   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight())
								   .ease(TweenEquations.easeInOutQuad))
					.end()
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

}
