package com.siondream.math.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.siondream.core.Env;

public class ShaderButton extends Button {

	public static class ShaderButtonStyle extends ButtonStyle {
		public String shader;
		public BitmapFont font;
		public Color backGroundColor;
		public Color fontColor;
	}
	
	private Label label;
	private ShaderProgram shader;
	private ShaderButtonStyle style;
	private float scale;
	
	public ShaderButton(String text, ShaderButtonStyle style) {
		super(style);
		this.label = new Label(text, new LabelStyle(style.font, style.fontColor));
		this.style = style;
		this.label.setAlignment(Align.center);
		this.label.setVisible(false);
		this.scale = 1.0f;
		this.shader = Env.game.getShaderManager().get(style.shader);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		super.draw(batch, parentAlpha);
		batch.setShader(shader);
		float oldScaleX = style.font.getScaleX();
		float oldScaleY = style.font.getScaleY();
		style.font.setScale(scale);
		label.validate();
		label.setPosition(getX() + (getWidth() - label.getWidth()) * 0.5f,
						  getY() + (getHeight() - label.getHeight()) * 0.5f + label.getHeight() * 0.4f);
		label.draw(batch, parentAlpha);
		label.setPosition(0.0f, 0.0f);
		style.font.setScale(oldScaleX, oldScaleY);
		batch.setShader(null);
	}
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public void setText(String text) {
		this.label.setText(text);
	}
}
