package com.siondream.math.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.siondream.core.Env;
import com.siondream.math.GameEnv;
import com.siondream.math.LevelManager.Level;

public class LevelButton extends Button {

	public static class LevelButtonStyle extends ButtonStyle {
		public String shader;
		public BitmapFont font;
		public Color backGroundColor;
		public Color fontColor;
		public float scale;
	}
	
	private LevelButtonStyle style;
	private Level level;
	private TextureRegion regionStarOn;
	private TextureRegion regionStarOff;
	private Image[] stars;
	private Image lock;
	private ShaderProgram shader;
	
	public LevelButton(Level level, LevelButtonStyle style) {
		super(style);
		this.level = level;
		this.style = style;
		this.stars = new Image[3];
		setWidth(643.0f);
		setHeight(88.0f);
		
		Skin skin = GameEnv.game.getSkin();
		TextureAtlas atlas = skin.getAtlas();
		
		if (level.unlocked) {
			regionStarOn = new TextureRegion(atlas.findRegion("staron"));
			regionStarOff = new TextureRegion(atlas.findRegion("staroff"));
			
			float initialX = getWidth() - 20.0f - (regionStarOn.getRegionWidth() + 20.0f) * stars.length;
			
			for (int i = 0; i < stars.length; ++i) {
				this.stars[i] = new Image(new TextureRegionDrawable(((level.stars - 1) >= i) ? regionStarOn : regionStarOff));
				this.stars[i].setX(initialX + i * (regionStarOn.getRegionWidth() + 20.0f));
				this.stars[i].setY((getHeight() - regionStarOn.getRegionHeight()) * 0.5f);
				this.addActor(this.stars[i]);
			}
		}
		else {
			this.setDisabled(true);
			
			lock = new Image(skin, "lock");
			lock.setPosition(438.0f, (getHeight() - lock.getHeight()) * 0.5f);
			this.addActor(lock);
		}
		
		this.shader = Env.game.getShaderManager().get(style.shader);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		batch.setShader(shader);
		float oldScaleX = style.font.getScaleX();
		float oldScaleY = style.font.getScaleY();
		Color oldColor = style.fontColor;
		style.font.setScale(style.scale);
		TextBounds bounds = style.font.getBounds(level.name);
		style.font.setColor(style.fontColor);
		style.font.draw(batch, level.name, getX() + 20.0f, getY() + bounds.height * 1.1f);
		style.font.setScale(oldScaleX, oldScaleY);
		style.font.setColor(oldColor);
		batch.setShader(null);
	}

	public void updateStars() {
		for (int i = 0; i < stars.length; ++i) {
			this.stars[i].setDrawable(new TextureRegionDrawable((i >= level.stars - 1) ? regionStarOn : regionStarOff));
		}
	}
}
