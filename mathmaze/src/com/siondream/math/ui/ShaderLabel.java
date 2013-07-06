package com.siondream.math.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.siondream.core.Env;

public class ShaderLabel extends Label {

	public static class ShaderLabelStyle extends LabelStyle {
		public String shader;
	}
	
	private ShaderProgram shader;
	
	public ShaderLabel(CharSequence text, Skin skin, String style) {
		super(text, skin, style);
		String shaderName = skin.get(style, ShaderLabelStyle.class).shader;
		this.shader = Env.game.getShaderManager().get(shaderName);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.setShader(shader);
		super.draw(batch, parentAlpha);
		batch.setShader(null);
	}
	
	public void setShaderProgram(ShaderProgram shader) {
		this.shader = shader;
	}
}
