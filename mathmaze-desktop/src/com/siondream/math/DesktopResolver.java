package com.siondream.math;

import java.awt.Desktop;
import java.net.URI;

import com.badlogic.gdx.utils.Logger;
import com.siondream.core.Env;
import com.siondream.core.PlatformResolver;

public class DesktopResolver implements PlatformResolver {

	public static final String TAG = "DesktopResolver";
	
	private Logger logger;
	
	public DesktopResolver() {
		logger = new Logger(TAG, Env.debugLevel);
	}
	
	@Override
	public void openURL(String url) {
		try {
			URI uri = new URI(url);
			Desktop.getDesktop().browse(uri);
		} catch(Exception e) {
			logger.error("error opening URL " + url + " " + e);
		}
	}
}
