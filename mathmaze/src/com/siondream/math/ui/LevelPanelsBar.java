package com.siondream.math.ui;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.siondream.core.Env;
import com.siondream.core.tweeners.ActorTweener;
import com.siondream.math.GameEnv;

public class LevelPanelsBar extends WidgetGroup {
	
	private Image longBar;
	private Image smallBar;
	
	public LevelPanelsBar(int numLevels, float width, float height) {
		super();
		
		Skin skin = GameEnv.game.getSkinNearest();
		
		longBar = new Image(skin, "upButton");
		smallBar = new Image(skin, "downButton");
		
		float fraction = numLevels / 6;
		
		longBar.setWidth(width);
		longBar.setHeight(height);
		smallBar.setWidth(width / fraction);
		smallBar.setHeight(height);
		
		addActor(longBar);
		addActor(smallBar);
	}
	
	public void scroll(int panel) {
		Tween.to(smallBar, ActorTweener.Position, 0.3f)
			 .target(panel * smallBar.getWidth(), smallBar.getY())
			 .ease(TweenEquations.easeInOutQuad)
			 .start(Env.game.getTweenManager());
	}
}
