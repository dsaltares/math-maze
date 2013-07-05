package com.siondream.math;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.SionScreen;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.LevelManager.Level;
import com.siondream.math.ui.LevelButton;
import com.siondream.math.ui.LevelButton.LevelButtonStyle;
import com.siondream.math.ui.LevelPanelsBar;
import com.siondream.math.ui.ShaderLabel;

public class LevelSelectionScreen extends SionScreen implements GestureListener {

	public final static String TAG = "LevelSelectionScreen";
	
	private final static int LEVELS_PER_PANEL = 6;
	
	private Logger logger;
	
	private Image imgBackground;
	private Image imgLand;
	private Image imgTitle;
	private ShaderLabel lblPick;
	private ImageButton btnBack;
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
		Env.game.getInputMultiplexer().removeProcessor(gestureDetector);
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		GameEnv.game.getLabelManager().update(delta);
	}

	public void init() {
		createUI();
		animateIn();
		Env.game.getInputMultiplexer().addProcessor(gestureDetector);
	}
	
	private void createUI() {
		Stage stage = Env.game.getStage();
		Skin skin = GameEnv.game.getSkin();
		Skin skinNearest = GameEnv.game.getSkinNearest();
		
		imgBackground = new Image(skin, "background");
		imgLand = new Image(skin, "land");
		imgTitle = new Image(skin, "title");
		
		LabelStyle labelStyle = skin.get("game", LabelStyle.class);
		lblPick = new ShaderLabel("Pick a level", labelStyle, Env.game.getShaderManager().get("font"));
		lblPick.setFontScale(2.0f);
		
		btnBack = new ImageButton(skin, "back");
		
		btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animateOut(MenuScreen.class);
			}
		});
		
		WidgetGroup labelsGroup = new WidgetGroup();
		
		stage.addActor(imgBackground);
		stage.addActor(labelsGroup);
		stage.addActor(imgLand);
		stage.addActor(btnBack);
		stage.addActor(imgTitle);
		stage.addActor(lblPick);
		
		
		levelsGroup = new WidgetGroup();
		stage.addActor(levelsGroup);
		
		LevelButtonStyle levelButtonStyle = skinNearest.get("menu", LevelButtonStyle.class);
		
		Array<Level> levels = GameEnv.game.getLevelManager().getLevels();
		currentPanel = 0;
		
		for (int i = 0; i < levels.size; ++i) {
			Level level = levels.get(i);
			LevelButton button = new LevelButton(level, levelButtonStyle);
			button.addListener(new LevelClickListener(level));
			button.setX((Env.virtualWidth * (i / LEVELS_PER_PANEL)) + (Env.virtualWidth - button.getWidth()) * 0.5f);
			button.setY(850 - (i % LEVELS_PER_PANEL) * (button.getHeight() + 20.0f));
			levelsGroup.addActor(button);
		}
		
		levelsBar = new LevelPanelsBar(levels.size, Env.virtualWidth, 30.0f);
		levelsBar.setY(240.0f);
		stage.addActor(levelsBar);
		
		GameEnv.game.getLabelManager().setGroup(labelsGroup);
		
		// Initial positions
		levelsGroup.setPosition(Env.virtualWidth, 0.0f);
		lblPick.setPosition(Env.virtualWidth, 960);
		btnBack.setPosition((Env.virtualWidth - btnBack.getWidth()) * 0.5f, -btnBack.getHeight());
		levelsBar.setPosition(Env.virtualWidth, 240.0f);
		imgTitle.setPosition((Env.virtualWidth - imgTitle.getWidth()) * 0.5f, Env.virtualHeight + imgTitle.getHeight());
		imgTitle.setOrigin(imgTitle.getWidth() * 0.5f, imgTitle.getHeight() * 0.5f);
		imgTitle.setRotation(10.0f);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK) {
			animateOut(MenuScreen.class);
			return true;
		}
		
		return false;
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
		if (panel >= 0 && panel < GameEnv.game.getLevelManager().getLevels().size / LEVELS_PER_PANEL) {
			currentPanel = panel;
			
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
					
					Array<Level> levels = GameEnv.game.getLevelManager().getLevels();
					int numLevels = levels.size;
					int index = 0;
					for (; index < numLevels; ++index) {
						if (!levels.get(index).unlocked) {
							break;
						}
					}
					
					scrollLevelPanels(Math.min(index / LEVELS_PER_PANEL, (numLevels / LEVELS_PER_PANEL) - 1));
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
	
	private class LevelClickListener extends ClickListener {
		private Level level;
		
		public LevelClickListener(Level level) {
			super();
			this.level = level;
		}
		
		@Override
		public void clicked(InputEvent event, float x, float y) {
			if (!level.unlocked) {
				return;
			}
			
			Env.game.getTweenManager().killTarget(btnBack);
			Env.game.getTweenManager().killTarget(lblPick);
			GameScreen gameScreen = GameEnv.game.getScreen(GameScreen.class);
			gameScreen.setLevel(level);
			animateOut(GameScreen.class);
		}
	}
}
