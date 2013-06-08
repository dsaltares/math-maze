package com.siondream.core;

import java.util.Locale;

import ashley.core.PooledEngine;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.core.entity.components.TransformComponent;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.core.tweeners.CameraTweener;
import com.siondream.core.tweeners.TransformTweener;

public class SionGame extends Game implements InputProcessor {

	private final String TAG = "SionGame";
	
	private Logger logger;
	private ObjectMap<Class<? extends SionScreen>, SionScreen> screens;
	private SionScreen nextScreen;
	private SionScreen currentScreen;
	private InputMultiplexer multiplexer;
	private Rectangle viewport;
	
	private Assets assets;
	private OrthographicCamera camera;
	private OrthographicCamera uiCamera;
	private PooledEngine engine;
	private Stage stage;
	private Skin skin;
	private TweenManager tweenManager;
	private LanguageManager languageManager;
	private World world;
	private float accumulator;
	
	@Override
	public void create() {
		Env.init(this);
		
		logger = new Logger(TAG, Env.debugLevel);
		
		logger.info("initialising");
		
		assets = new Assets("data/config/assets.xml");
		assets.loadGroup("base");
		assets.finishLoading();
		
		screens = new ObjectMap<Class<? extends SionScreen>, SionScreen>();
		nextScreen = null;
		currentScreen = null;
		viewport = new Rectangle(0.0f, 0.0f, 0.0f, 0.0f);
		camera = new OrthographicCamera(Env.virtualWidth * Env.pixelsToMetres,
										Env.virtualHeight * Env.pixelsToMetres);
		uiCamera = new OrthographicCamera(Env.virtualWidth, Env.virtualHeight);
		uiCamera.position.x = Env.virtualWidth * 0.5f;
		uiCamera.position.y = Env.virtualHeight * 0.5f;
		
		tweenManager = new TweenManager();
		Tween.registerAccessor(OrthographicCamera.class, new CameraTweener());
		Tween.registerAccessor(Actor.class, new ActorTweener());
		Tween.registerAccessor(TransformComponent.class, new TransformTweener());
		Tween.setCombinedAttributesLimit(4);
		
		world = new World(Env.gravity, Env.doSleep);
		accumulator = 0.0f;
		engine = new PooledEngine();
		skin = assets.get("data/ui/uiskin.json", Skin.class);
		stage = new Stage(Env.virtualWidth, Env.virtualHeight, true);
		
		multiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(multiplexer);
		Gdx.input.setCatchBackKey(Env.catchBack);
		Gdx.input.setCatchMenuKey(Env.catchMenu);
		multiplexer.addProcessor(this);
		
		languageManager = new LanguageManager("data/lang", Locale.getDefault().getLanguage());
	}

	@Override
	public void dispose() {
		logger.info("shutting down");
		
		assets.dispose();
		world.dispose();
		stage.dispose();
		skin.dispose();
		
		if (currentScreen != null) {
			currentScreen.dispose();
		}
	}
	
	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		
		Gdx.gl.glViewport((int) viewport.x,
						  (int) viewport.y,
						  (int) viewport.width,
						  (int) viewport.height);
		
		camera.update();
		uiCamera.update();
		engine.update(deltaTime);
		stage.act(deltaTime);
		super.render();
		
		accumulator += deltaTime;
		
		while (accumulator >= Env.physicsDeltaTime) {
			world.step(Env.physicsDeltaTime, Env.velocityIterations, Env.positionIterations);
			accumulator -= deltaTime;
		}
		
		tweenManager.update(deltaTime);
		performScreenChange();
	}

	@Override
	public void resize(int width, int height) {
		// Calculate new aspect ratio
        float aspectRatio = (float)width / (float)height;
        
        float scale = 1.0f;
        
        // Calculate the scale we need to apply and the possible crop
        if(aspectRatio > Env.aspectRatio)
        {
            scale = (float)height / (float)Env.virtualHeight;
            viewport.x = (width - Env.virtualWidth * scale) * 0.5f;
        }
        else if(aspectRatio < Env.aspectRatio)
        {
            scale = (float)width / (float)Env.virtualWidth;
            viewport.y = (height - Env.virtualHeight * scale) * 0.5f;
        }
        else
        {
            scale = (float)width/(float)Env.virtualWidth;
        }
        
        // New witdh and  height
        viewport.width = (float)Env.virtualWidth * scale;
        viewport.height = (float)Env.virtualHeight * scale;
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	protected void addScreen(SionScreen screen) {
		screens.put(screen.getClass(), screen);
	}
	
	public <T extends SionScreen> T getScreen(Class<T> type) {
		return getScreen(type);
	}
	
	@Override
	public void setScreen(Screen screen) {
		logger.error("method not supported");
	}
	
	public void setScreen(Class<? extends SionScreen> type) {
		SionScreen screen = screens.get(type);
		
		if (screen != null) {
			nextScreen = screen;
		}
		else {
			logger.error("invalid screen " + type.getName());
		}
	}
	
	public Assets getAssets() {
		return assets;
	}
	
	public OrthographicCamera getCamera() {
		return camera;
	}
	
	public OrthographicCamera getUICamera() {
		return uiCamera;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public TweenManager getTweenManager() {
		return tweenManager;
	}
	
	public PooledEngine getEngine() {
		return engine;
	}
	
	public LanguageManager getLang() {
		return languageManager;
	}
	
	public InputMultiplexer getInputMultiplexer() {
		return multiplexer;
	}
	
	private void performScreenChange() {
		if (nextScreen != null) {
			logger.info("switching to screen " + screens.findKey(nextScreen, false));
			multiplexer.removeProcessor(currentScreen);
			setScreenInternal(nextScreen);
			multiplexer.addProcessor(currentScreen);
			//Gdx.input.setInputProcessor(nextScreen);
			nextScreen = null;
		}
	}
	
	private void setScreenInternal(SionScreen screen) {
		super.setScreen(screen);
		currentScreen = screen;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (Env.debug) {
			if (keycode == Keys.F1) {
				Env.init(this);
			}
		}
		
		return stage.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return stage.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return stage.keyTyped(character);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return stage.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return stage.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return stage.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return stage.mouseMoved(screenX, screenY);
	}

	@Override
	public boolean scrolled(int amount) {
		return stage.scrolled(amount);
	}
}
