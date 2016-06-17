package com.rounds.firelinktest;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Telephony;

/**
 * Created by nitzan on 21/03/2016.
 */
public class AppManager {
	public enum SharingApp {
		DefaultMessaging(null, false),
		FacebookMessenger("com.facebook.orca", true),
		Kakao("com.kakao.talk", false),
		Kik("kik.android", true),
		Line("jp.naver.line.android", true),
		Telegram("org.telegram.messenger", true),
		Twitter("com.twitter.android", false),
		Whatsapp("com.whatsapp", true),
		Viber("com.viber.voip", false),
		Hike("com.bsb.hike", true),
		Gmail("com.google.android.gm", false),
		Vkontakte("com.vkontakte.android", false);

		private final String packageName;
		private final boolean overlaySupported;

		SharingApp(final String packageName, final boolean overlaySupported) {
			this.packageName = packageName;
			this.overlaySupported = overlaySupported;
		}

		public AppInfo getInfo(final Context context) {
			if (this == DefaultMessaging) {
				return AppManager.getDefaultMessagingApp(context);
			}

			return AppManager.getInfo(context, this.packageName, this);
		}

		public String getPackageName() {
			return this.packageName;
		}

		public static SharingApp fromId(final int id) {
			return SharingApp.values()[id];
		}

		public boolean supportAppOverlay(){
			return this.overlaySupported;
		}

	}

	public static abstract class AppInfo {
		private final int id;
		private final String packageName;
		private final String label;

		protected AppInfo(final SharingApp sharingApp, final String label) {
			this(sharingApp, sharingApp.packageName, label);
		}

		protected AppInfo(final SharingApp sharingApp, final String packageName, final String label) {
			this.id = sharingApp.ordinal();
			this.packageName = packageName;
			this.label = label;
		}

		public int getId() {
			return this.id;
		}

		public String getPackageName() {
			return this.packageName;
		}

		public String getLabel() {
			return this.label;
		}

		public SharingApp getSharingApp() {
			return SharingApp.fromId(this.id);
		}

		public void showAppOverlay(){

		}

		public abstract boolean isInstalled();

		public abstract Bitmap getIconBitmap(final Context context);


		public abstract boolean isOverlaySupported();
	}

	static class UnexistingAppInfo extends AppInfo {
		private UnexistingAppInfo(final SharingApp sharingApp) {
			super(sharingApp, null);
		}

		@Override
		public boolean isInstalled() {
			return false;
		}

		@Override
		public Bitmap getIconBitmap(final Context context) {
			return null;
		}

		@Override
		public boolean isOverlaySupported() {
			return false;
		}

	}

	static class ExistingAppInfo extends AppInfo {
		private final SharingApp sharingApp;
		private final int iconResourceId;

		private Bitmap iconBitmap;

		private ExistingAppInfo(final SharingApp sharingApp, final String label, final int icon) {
			this(sharingApp, sharingApp.packageName, label, icon);
		}

		private ExistingAppInfo(final SharingApp sharingApp, final String packageName, final String label, final int icon) {
			super(sharingApp, packageName, label);
			this.sharingApp = sharingApp;
			this.iconResourceId = icon;
		}

		@Override
		public SharingApp getSharingApp() {
			return this.sharingApp;
		}

		@Override
		public boolean isInstalled() {
			return true;
		}

		@Override
		public Bitmap getIconBitmap(final Context context) {
			if (this.iconBitmap == null) {
				final Resources resources;

				try {
					resources = context.getPackageManager().getResourcesForApplication(this.getPackageName());
				} catch (PackageManager.NameNotFoundException e) {
					return null;
				}

				this.iconBitmap = BitmapFactory.decodeResource(resources, this.iconResourceId);
			}

			return this.iconBitmap;
		}

		@Override
		public boolean isOverlaySupported() {
			return this.sharingApp.supportAppOverlay();
		}
	}

	private static AppInfo getInfo(final Context context, final String packageName, final SharingApp app) {
		if (!AppManager.isAppInstalled(context, packageName)) {
			return new UnexistingAppInfo(app);
		}

		final ApplicationInfo applicationInfo;
		final String appLabel;
		final PackageManager packageManager = context.getPackageManager();

		try {
			applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			appLabel = applicationInfo.loadLabel(packageManager).toString();
		} catch (PackageManager.NameNotFoundException e) {
			return new UnexistingAppInfo(app);
		}

		return new ExistingAppInfo(app, packageName, appLabel, applicationInfo.icon);
	}

	private static AppInfo getDefaultMessagingApp(final Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return AppManager.getInfo(context, Telephony.Sms.getDefaultSmsPackage(context), SharingApp.DefaultMessaging);
		}

		final PackageManager packageManager = context.getPackageManager();
		final Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		List<ResolveInfo> resolved = packageManager.queryIntentActivities(smsIntent, 0);
		return resolved.size() == 0 ?
				new UnexistingAppInfo(SharingApp.DefaultMessaging) :
				new ExistingAppInfo(SharingApp.DefaultMessaging, resolved.get(0).activityInfo.loadLabel(packageManager).toString(), resolved.get(0).getIconResource());
	}

	private static boolean isAppInstalled(Context context, final String packageName) {
		final PackageManager pm = context.getPackageManager();

		try {
			pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}

		return true;
	}
}
