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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.LevelManager.Level;
import com.siondream.math.ui.LevelButton;
import com.siondream.math.ui.LevelButton.LevelButtonStyle;
import com.siondream.math.ui.ShaderButton.ShaderButtonStyle;
import com.siondream.math.ui.LevelPanelsBar;
import com.siondream.math.ui.ShaderButton;
import com.siondream.math.ui.ShaderLabel;

public class LevelSelectionScreen extends SionScreen implements GestureListener {

	public final static String TAG = "LevelSelectionScreen";
	
	private Logger logger;
	
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private ShaderLabel lblPick;
	private ShaderButton btnBack;
	private Texture fontTexture;
	private BitmapFont font;
	private ShaderProgram fontShader;
	private LevelPanelsBar levelsBar;
	private WidgetGroup levelsGroup;
	private GestureDetector gestureDetector;
	private int currentPanel;
	
	public LevelSelectionScreen() {
		super();
		
		logger = new Logger(TAG, Env.debugLevel);
		logger.info("initialising");
		gestureDetector = new GestureDetector(this);
	}
	
	@Override
	public void show() {
		super.show();
		init();
	}

	@Override
	public void hide() {
		super.hide();
		dispose();
	}
	
	@Override
	public void dispose() {
		GameEnv.game.getStage().clear();
		font.dispose();
		fontTexture.dispose();
		Env.game.getInputMultiplexer().removeProcessor(gestureDetector);
	}

	public void init() {
		createUI();
		animateIn();
		Env.game.getInputMultiplexer().addProcessor(gestureDetector);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Assets assets = Env.game.getAssets();
		
		fontTexture = new Texture(Gdx.files.internal("data/ui/chicken.png"), true);
		fontTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("data/ui/chicken.fnt"), new TextureRegion(fontTexture), false);
		
		fontShader = new ShaderProgram(Gdx.files.internal("data/ui/font.vert"), 
													 Gdx.files.internal("data/ui/font.frag"));
		if (!fontShader.isCompiled()) {
		    Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
		}
		
		imgBackground = new Image(assets.get("data/ui/background.png", Texture.class));
		imgLand = new Image(assets.get("data/ui/land.png", Texture.class));
		imgTitle = new Image(assets.get("data/ui/title.png", Texture.class));
		
		LabelStyle labelStyle = new LabelStyle(font, Color.BLACK);
		lblPick = new ShaderLabel("Pick a level", labelStyle, fontShader);
		lblPick.setFontScale(2.0f);
		
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
		
		btnBack = new ShaderButton("Back", buttonStyle);
		btnBack.setScale(3.25f);
		btnBack.setWidth(400.0f);
		btnBack.setHeight(125.0f);
		
		btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut();
			}
		});
		
		stage.addActor(imgBackground);
		stage.addActor(imgLand);
		stage.addActor(btnBack);
		stage.addActor(imgTitle);
		stage.addActor(lblPick);
		
		
		levelsGroup = new WidgetGroup();
		stage.addActor(levelsGroup);
		
		LevelButtonStyle levelButtonStyle = new LevelButtonStyle();
		levelButtonStyle.scale = 3.0f;
		levelButtonStyle.fontColor = Color.BLACK;
		levelButtonStyle.down = downButton;
		levelButtonStyle.up = upButton;
		levelButtonStyle.font = font;
		levelButtonStyle.shader = fontShader;
		levelButtonStyle.backGroundColor = Color.WHITE;
		levelButtonStyle.fontColor = Color.BLACK;
		
		Array<Level> levels = GameEnv.game.getLevelManager().getLevels();
		int levelsPerPanel = 6;
		currentPanel = 0;
		
		for (int i = 0; i < levels.size; ++i) {
			Level level = levels.get(i);
			LevelButton button = new LevelButton(level, levelButtonStyle);
			button.addListener(new LevelClickListener(level));
			button.setX((Env.virtualWidth * (i / levelsPerPanel)) + (Env.virtualWidth - button.getWidth()) * 0.5f);
			button.setY(850 - (i % levelsPerPanel) * (button.getHeight() + 20.0f));
			levelsGroup.addActor(button);
		}
		
		levelsBar = new LevelPanelsBar(levels.size, Env.virtualWidth, 30.0f);
		levelsBar.setY(240.0f);
		stage.addActor(levelsBar);
		
		// Initial positions
		levelsGroup.setPosition(Env.virtualWidth, 0.0f);
		lblPick.setPosition(Env.virtualWidth, 960);
		btnBack.setPosition((Env.virtualWidth - btnBack.getWidth()) * 0.5f, -btnBack.getHeight());
		levelsBar.setPosition(Env.virtualWidth, 240.0f);
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
	}
	
	private class LevelClickListener extends ClickListener {
		private Level level;
		
		public LevelClickListener(Level level) {
			super();
			this.level = level;
		}
		
		@Override
		public void clicked(InputEvent event, float x, float y) {
			GameScreen gameScreen = GameEnv.game.getScreen(GameScreen.class);
			gameScreen.setLevel(level);
			GameEnv.game.setScreen(GameScreen.class);
		}
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		float absVelocityX = Math.abs(velocityX);
		
		if (absVelocityX > 200) {
			scrollLevelPanels(currentPanel - (int)(velocityX/absVelocityX));
		}
		
		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void scrollLevelPanels(int panel) {
		if (panel >= 0 && panel < GameEnv.game.getLevelManager().getLevels().size / 6) {
			currentPanel = panel;
			
			logger.info("movint to panel " + panel);
			
			Tween.to(levelsGroup, ActorTweener.Position, 0.6f)
				 .target(-(Env.virtualWidth * panel), levelsGroup.getY())
				 .ease(TweenEquations.easeInOutQuad)
				 .start(Env.game.getTweenManager());
			
			levelsBar.scroll(panel);
		}
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
				.push(Tween.to(imgTitle, ActorTweener.Position, 0.4f)
				      	   .target((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight - imgTitle.getHeight() - 60.0f)
						   .ease(TweenEquations.easeInOutQuad))
				.beginParallel()
					.push(Tween.to(levelsGroup, ActorTweener.Position, 0.5f)
							   .target(0.0f, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(levelsBar, ActorTweener.Position, 0.25f)
							   .target(0.0f, levelsBar.getY())
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				// Animate in sequence
				.push(Tween.to(lblPick, ActorTweener.Position, 0.4f)
						   .target((Env.virtualWidth - lblPick.getWidth() * lblPick.getFontScaleX() - 40.0f), 960)
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(btnBack, ActorTweener.Position, 0.40f)
						   .target((Env.virtualWidth - btnBack.getWidth()) * 0.5f, 50.0f)
						   .ease(TweenEquations.easeInOutQuad))
				
			.end()
			.setCallback(callback)
			.start(Env.game.getTweenManager());
	}
	
	private void animateOut() {
		Timeline timeline = Timeline.createSequence();
		
		TweenCallback callback = new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				if (type == TweenCallback.COMPLETE) {
					Env.game.setScreen(MenuScreen.class);
				}
			}
		};
		
		timeline.beginSequence()
				.push(Tween.to(btnBack, ActorTweener.Position, 0.20f)
						   .target((Env.virtualWidth - btnBack.getWidth()) * 0.5f, -btnBack.getHeight())
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(lblPick, ActorTweener.Position, 0.2f)
						   .target(Env.virtualWidth, 960)
						   .ease(TweenEquations.easeInOutQuad))
				.beginParallel()
					.push(Tween.to(levelsGroup, ActorTweener.Position, 0.25f)
							   .target(Env.virtualWidth, 0.0f, 0.0f)
							   .ease(TweenEquations.easeInOutQuad))
					.push(Tween.to(levelsBar, ActorTweener.Position, 0.15f)
							   .target(Env.virtualWidth, levelsBar.getY())
							   .ease(TweenEquations.easeInOutQuad))
				.end()
				.push(Tween.to(imgLand, ActorTweener.Position, 0.25f)
						   .target(0.0f, -imgLand.getHeight(), 0.0f)
						   .ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(imgTitle, ActorTweener.Position, 0.25f)
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
}
