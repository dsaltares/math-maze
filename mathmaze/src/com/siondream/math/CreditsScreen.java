package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.siondream.core.Env;
import com.siondream.core.LanguageManager;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.ui.ShaderLabel;

public class CreditsScreen extends SionScreen {
	
	private final static String TAG = "CreditsScreen";
	private final static String CREDITS_FILE = "data/credits.xml";
	
	private Logger logger;
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private ImageButton btnBack;
	private Table tableCredits;
	
	private Sound sfxTap;
	
	public CreditsScreen() {
		super();
		
		logger = new Logger(TAG, Env.debugLevel);
	}
	
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
				if (GameEnv.soundEnabled) {
					sfxTap.play();
				}
				animateOut();
			}
		});
		
		WidgetGroup labelsGroup = new WidgetGroup();
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		createCreditsTable();
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(tableCredits);
		stage.addActor(imgLand);
		stage.addActor(btnBack);
		
		imgLand.setPosition(0.0f, - imgLand.getHeight());
		btnBack.setPosition((Env.virtualWidth - btnBack.getWidth()) * 0.5f, -btnBack.getHeight());
		
		sfxTap = Env.game.getAssets().get("data/sfx/tap.mp3", Sound.class);
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
				if (type == TweenCallback.COMPLETE && Env.game.getScreen().getClass() == CreditsScreen.class) {
					animateOut();
				}
			}
		};
		
		Tween.to(tableCredits, ActorTweener.Position, tableCredits.getHeight() / Env.virtualHeight * 10.0f)
			 .target(tableCredits.getX(), Env.virtualHeight)
			 .ease(TweenEquations.easeNone)
			 .setCallback(callback)
			 .start(Env.game.getTweenManager());
	}
	
	private void createCreditsTable() {
		Skin skin = GameEnv.game.getSkin();
		LanguageManager lang = Env.game.getLang();
		
		logger.info("loading file " + CREDITS_FILE);
		
		tableCredits = new Table();
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(CREDITS_FILE));

			int numElements = root.getChildCount();
			for (int i = 0; i < numElements; ++i) {
				Element node = root.getChild(i);
				
				tableCredits.row();
				
				if (node.getName().equals("image")) {
					String name = node.getAttribute("name");
					float pad = Float.parseFloat(node.getAttribute("pad"));
					Image image = new Image(skin, name);
					tableCredits.add(image).center().size(image.getWidth(), image.getHeight()).padBottom(pad);
				}
				else if (node.getName().equals("label")) {
					String text = node.getAttribute("text");
					float scale = Float.parseFloat(node.getAttribute("scale"));
					float pad = Float.parseFloat(node.getAttribute("pad"));
					ShaderLabel label = new ShaderLabel(lang.getString(text), skin, "game");
					label.setFontScale(scale);
					tableCredits.add(label).center().padBottom(pad);
				}
			}
		}
		catch (Exception e) {
			logger.error("error loading file " + CREDITS_FILE + " " + e.getMessage());
		}
		
		tableCredits.pack();
		tableCredits.setPosition((Env.virtualWidth - tableCredits.getWidth()) * 0.5f, -tableCredits.getHeight());
	}
}
