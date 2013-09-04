package com.siondream.math;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.siondream.core.Env;
import com.siondream.core.LanguageManager;
import com.siondream.core.PlatformResolver;

public class AndroidResolver implements PlatformResolver {

	private Activity activity;
	
	public AndroidResolver(Activity activity) {
		this.activity = activity;
	}
	
	@Override
	public void openURL(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		activity.startActivity(browserIntent);
	}

	@Override
	public void rateApp() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName()));
		activity.startActivity(browserIntent);
	}

	@Override
	public void sendFeedback() {
		/* Create the Intent */
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		/* Fill it with Data */
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"david@siondream.com"});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Math Maze! feedback");

		/* Send it off to the Activity-Chooser */
		LanguageManager lang = Env.game.getLang();
		activity.startActivity(Intent.createChooser(emailIntent, lang.getString("Send mail...")));
	}
}
