package com.siondream.math.ui;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.siondream.core.Assets;
import com.siondream.core.Env;
import com.siondream.core.tweeners.ActorTweener;

public class LevelPanelsBar extends WidgetGroup {
	
	private Image longBar;
	private Image smallBar;
	
	public LevelPanelsBar(int numLevels, float width, float height) {
		super();
		
		Assets assets = Env.game.getAssets();
		Texture upText = assets.get("data/ui/upButton.png", Texture.class);
		upText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		Texture downText = assets.get("data/ui/downButton.png", Texture.class);
		downText.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegionDrawable upButton = new TextureRegionDrawable(new TextureRegion(upText));
		TextureRegionDrawable downButton = new TextureRegionDrawable(new TextureRegion(downText));
		
		longBar = new Image(upButton);
		smallBar = new Image(downButton);
		
		float fraction = numLevels / 6;
		
		longBar.setWidth(width);
		longBar.setHeight(height);
		smallBar.setWidth(width / fraction);
		smallBar.setHeight(height);
		
		addActor(longBar);
		addActor(smallBar);
	}
	
	public void scroll(int panel) {
		Tween.to(smallBar, ActorTweener.Position, 0.6f)
			 .target(panel * smallBar.getWidth(), smallBar.getY())
			 .ease(TweenEquations.easeInOutQuad)
			 .start(Env.game.getTweenManager());
	}
}
