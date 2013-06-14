package com.siondream.math;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

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
}
