package com.siondream.math.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.math.LevelManager.Level;

public class LevelButton extends Button {

	public static class LevelButtonStyle extends ButtonStyle {
		public ShaderProgram shader;
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
	
	public LevelButton(Level level, LevelButtonStyle style) {
		super(style);
		this.level = level;
		this.style = style;
		this.stars = new Image[3];
		setWidth(643.0f);
		setHeight(88.0f);
		
		Assets assets = Env.game.getAssets();
		regionStarOn = new TextureRegion(assets.get("data/ui/staron.png", Texture.class));
		regionStarOff = new TextureRegion(assets.get("data/ui/staroff.png", Texture.class));
		
		float initialX = getWidth() - 20.0f - (regionStarOn.getRegionWidth() + 20.0f) * stars.length;
		
		for (int i = 0; i < stars.length; ++i) {
			//this.stars[i] = new Image(new TextureRegionDrawable(((level.stars - 1) >= i) ? regionStarOn : regionStarOff));
			this.stars[i] = new Image(new TextureRegionDrawable(regionStarOn));
			this.stars[i].setX(initialX + i * (regionStarOn.getRegionWidth() + 20.0f));
			this.stars[i].setY((getHeight() - regionStarOn.getRegionHeight()) * 0.5f);
			this.addActor(this.stars[i]);
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		batch.setShader(style.shader);
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
