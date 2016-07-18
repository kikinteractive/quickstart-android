package com.rounds.firelinkpoc;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by anat on 17/03/16.
 */
public class InviteManager {
	private static final String HAS_SEEN_FIRST_TIME_AFTER_SHARE_VIEW = "has_seen_first_time_after_share_view";
	private String url = null;
	private boolean appOpened;
	private FragmentManager fragmentManager;

	public void resetWithNewUrl(final String url) {
		this.appOpened = false;

		this.url = url;
	}

	public void setFragmentManager(FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
	}


	public static Intent createShareIntent(Context context, AppManager.AppInfo appInfo, String url) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");

		String intentExtraText;

		intentExtraText = String.format("%s %s %s %s",
				context.getString(R.string.chat_share_message_prefix),
				"\uD83C\uDF89\uD83C\uDFA5",
				context.getString(R.string.chat_share_message),
				url);
		intent.putExtra(Intent.EXTRA_TEXT, intentExtraText);

		intent.setPackage(appInfo.getPackageName());
		return intent;
	}


}
