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

	@Override
	public void rateApp() {
		String url = "https://play.google.com/store/apps/details?id=com.siondream.math";
		
		try {
			URI uri = new URI(url);
			Desktop.getDesktop().browse(uri);
		} catch(Exception e) {
			logger.error("error opening URL " + url + " " + e);
		}
	}
	
	@Override
	public void sendFeedback() {
		String url = "mailto:david@siondream.com";
		
		try {
			URI uri = new URI(url);
			Desktop.getDesktop().browse(uri);
		} catch(Exception e) {
			logger.error("error opening URL " + url + " " + e);
		}
	}
}
