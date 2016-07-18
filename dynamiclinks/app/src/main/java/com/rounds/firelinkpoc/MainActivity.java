/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rounds.firelinkpoc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
	Uri deepLink;
    private static final String TAG = "MainActivity";
    private static final String DEEP_LINK_URL = "http://rounds.com/dl/";

    // [START define_variables]
    private GoogleApiClient mGoogleApiClient;
    private RadioGroup mRadioGroup;
	// [END define_variables]


    // [START on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Validate that the developer has set the app code.
        validateAppCode();
		mRadioGroup = (RadioGroup)findViewById(R.id.app_radio_group);

				// Share button click listener
		findViewById(R.id.button_create_link).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create a deep link and display it in the UI
				deepLink = buildDeepLink(0, false);
				((TextView) findViewById(R.id.link_view_send)).setText(deepLink.toString());
			}
		});



		// Share button click listener
        findViewById(R.id.button_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				//extendedShareDeepLink(deepLink.toString());
				if (deepLink!=null && !deepLink.toString().isEmpty()) {
					shareDeepLink(deepLink.toString());
				}
            }
        });

		findViewById(R.id.button_share2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//extendedShareDeepLink(deepLink.toString());
				if (deepLink!=null && !deepLink.toString().isEmpty()) {
					extendedShareDeepLink(deepLink.toString());
				}
			}
		});




		// [END_EXCLUDE]

        // [START build_api_client]
        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();
        // [END build_api_client]

        // [START get_deep_link]
        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent

                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    // Handle the deep link. For example, open the linked
                                    // content, or apply promotional credit to the user's
                                    // account.

                                    // [START_EXCLUDE]
                                    // Display deep link in the UI
                                    ((TextView) findViewById(R.id.link_view_receive)).setText(deepLink);
                                    // [END_EXCLUDE]
                                } else {
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }
                            }
                        });
        // [END get_deep_link]
    }
    // [END on_create]

	private void checkAndLog(Bundle extras, String paramName){
		String value = extras.getString(paramName);
		Log.d("#bbb", "paramName "+paramName+", value:"+value);
	}

    /**
     * Build a Firebase Dynamic Link.
     * https://firebase.google.com/docs/dynamic-links/android#create-a-dynamic-link

     * @param minVersion the {@code versionCode} of the minimum version of your app that can open
     *                   the deep link. If the installed app is an older version, the user is taken
     *                   to the Play store to upgrade the app. Pass 0 if you do not
     *                   require a minimum version.
     * @param isAd true if the dynamic link is used in an advertisement, false otherwise.
     * @return a {@link Uri} representing a properly formed deep link.
     */
    private Uri buildDeepLink(int minVersion, boolean isAd) {

        // Get the unique appcode for this app.
        String appCode = getString(R.string.app_code);

        // Get this app's package name.
        String packageName = getApplicationContext().getPackageName();

		String pathToAppend = ((TextView) findViewById(R.id.link_path_to_append)).getText().toString();
		deepLink = Uri.parse(DEEP_LINK_URL);
		deepLink = deepLink.withAppendedPath(deepLink, pathToAppend);

        // Build the link with all required parameters
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(appCode + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", deepLink.toString())
                .appendQueryParameter("apn", packageName);

        // If the deep link is used in an advertisement, this value must be set to 1.
        if (isAd) {
            builder.appendQueryParameter("ad", "1");
        }

        // Minimum version is optional.
        if (minVersion > 0) {
            builder.appendQueryParameter("amv", Integer.toString(minVersion));
        }

        // Return the completed deep link.
        return builder.build();
    }

	private void shareDeepLink(String deepLink){
		int viewId = mRadioGroup.getCheckedRadioButtonId();
		AppManager.AppInfo appInfo;
		switch (viewId){
			case R.id.radio_kik:
				appInfo = AppManager.SharingApp.Kik.getInfo(this);
				break;
			case R.id.radio_line:
				appInfo = AppManager.SharingApp.Line.getInfo(this);
				break;
			case R.id.radio_messenger:
				appInfo = AppManager.SharingApp.FacebookMessenger.getInfo(this);
				break;
			case R.id.radio_twitter:
				appInfo = AppManager.SharingApp.Twitter.getInfo(this);
				break;
			case R.id.radio_whatsapp:
				appInfo = AppManager.SharingApp.Whatsapp.getInfo(this);
				break;
			default:
				appInfo = AppManager.SharingApp.Gmail.getInfo(this);
				break;

		}
		Intent intent = InviteManager.createShareIntent(this, appInfo ,deepLink);
		startActivity(intent);
	}

//    private void shareDeepLink(String deepLink) {
//       Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link");
//        intent.putExtra(Intent.EXTRA_TEXT,deepLink);
//
//		Intent chooserIntent = Intent.createChooser(intent,
//				getText(R.string.share));
//
//		// start activity with the chooser intent
//		startActivity(chooserIntent);
//    }

	private void extendedShareDeepLink(String deepLink){

		Intent intent = new AppInviteInvitation.IntentBuilder("Berry Invitation title")
				.setMessage("Berry Invitation message")
				.setDeepLink(Uri.parse(deepLink))
				.setCustomImage(Uri.parse("android.resource://com.rounds.firelinkpoc/drawable/logo512"))
				.setCallToActionText("Call to action!")
				.build();
		startActivityForResult(intent, 101);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			// Get the invitation IDs of all sent messages
			String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
			for (String id : ids) {
				Log.d(TAG, "onActivityResult: sent invitation " + id);
			}
		} else {
			// Sending failed or it was canceled, show failure message to the user
			Log.d(TAG, "onActivityResult: some error occured");
		}
	}

	private void validateAppCode() {
        String appCode = getString(R.string.app_code);
        if (appCode.contains("YOUR_APP_CODE")) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Configuration")
                    .setMessage("Please set your app code in res/values/strings.xml")
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }


}
