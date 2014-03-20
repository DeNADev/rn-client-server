/*
*The MIT License (MIT)
*Copyright (c) 2014 DeNA Co., Ltd.
*
*Permission is hereby granted, free of charge, to any person obtaining a copy
*of this software and associated documentation files (the "Software"), to deal
*in the Software without restriction, including without limitation the rights
*to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
*copies of the Software, and to permit persons to whom the Software is
*furnished to do so, subject to the following conditions:
*
*The above copyright notice and this permission notice shall be included in
*all copies or substantial portions of the Software.
*
*THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
*IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
*FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
*AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
*LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
*OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
*THE SOFTWARE.
*/
package com.mobage.android.sample;

import com.mobage.android.Mobage;
import com.mobage.android.Error;
import com.mobage.android.Mobage.PlatformListener;
import com.mobage.android.social.User;
import com.mobage.android.social.common.People;
import com.mobage.android.social.common.People.OnGetUserComplete;
import com.mobage.android.social.common.RemoteNotification;
import com.mobage.android.social.common.RemoteNotification.OnSetRemoteNotificationsEnabledComplete;
import com.mobage.android.social.common.RemoteNotification.RemoteNotificationListener;
import com.mobage.android.social.common.RemoteNotificationPayload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import jp.mbga.a{application_id}.R;

public class HelloMobageActivity extends Activity {
	protected static final String TAG = "HelloMobageActivity";
	private PlatformListener mPlatformListener = null;

	private static enum LOGIN_STAT {
		IDLE, REQUIRED, COMPLETE, CANCELED
	};

	private boolean started = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Mobage.initialize(Mobage.Region.JP, Mobage.ServerMode.SANDBOX,
				"{consumer_key}", "{consumer_secret}",
				"{application_id}", this);

		Mobage.onCreate(this);
		setMobagePlatformListener();
		// checkLoginStatus should be invoked only when launching app
		Mobage.checkLoginStatus();

		RemoteNotification.setRemoteNotificationsEnabled(true,
				new OnSetRemoteNotificationsEnabledComplete() {
					@Override
					public void onSuccess() {
						/* flag was set properly */
					}

					@Override
					public void onError(Error error) {
						/* An error occurred in setting this value */
					}
				});
		RemoteNotification.setListener(new RemoteNotificationListener() {
			public void handleReceive(Context context, Intent intent) {
				RemoteNotificationPayload payload = RemoteNotification
						.extractPayloadFromIntent(intent);
				Log.i("---------------", "#### " + payload.getMessage());
				RemoteNotification
						.displayStatusBarNotification(context, intent);
			}
		});
		if (!started) {
			handleIntent(getIntent());
			started = true;
		}
	}

	private void handleIntent(Intent intent) {
		if (intent != null) {
			setIntent(intent);
			Bundle extras = intent.getExtras();
			if (extras == null) {
				Log.i(TAG, "Intent extras is empty");
			} else {
				String message = extras.getString("message");
				String extrasJsonString = extras.getString("extras");
				Log.i(TAG, "message" + message);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "onNewIntent");
		handleIntent(intent);
	}

	private void setMobagePlatformListener() {
		if (mPlatformListener == null) {
			mPlatformListener = new Mobage.PlatformListener() {

				boolean mSplashCompleted = false;
				private LOGIN_STAT mLoginStat = LOGIN_STAT.IDLE;

				@Override
				public void onLoginRequired() {
					Log.i(TAG, "Login required.");
					mLoginStat = LOGIN_STAT.REQUIRED;
					checkProgress();
				}

				@Override
				public void onLoginComplete(String userId) {
					Log.i(TAG, "Login completed:" + userId);
					mLoginStat = LOGIN_STAT.COMPLETE;
					checkProgress();
				}

				@Override
				public void onSplashComplete() {
					Log.i(TAG, "Splash Completed.");
					mSplashCompleted = true;
					checkProgress();
				}

				@Override
				public void onLoginError(Error error) {
					Log.e(TAG, "Login failed. " + error.getDescription());
				}

				@Override
				public void onLoginCancel() {
					Log.d(TAG, "Login canceled.");
				}

				private void checkProgress() {
					if (mLoginStat == LOGIN_STAT.REQUIRED && mSplashCompleted) {
						Mobage.showLoginDialog();
					}
					if (mLoginStat == LOGIN_STAT.COMPLETE && mSplashCompleted) {
						Mobage.hideSplashScreen();
						// start game
						helloMobage();
					}
				}
			};
		}
		Mobage.addPlatformListener(this, mPlatformListener);
	}

	@Override
	public void onStart() {
		super.onStart();
		Mobage.onStart(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Mobage.onResume will check the session status and invoke
		// onLoginRequired if needed
		Mobage.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Mobage.onPause(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Mobage.onStop(this);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// need to be set here because the platform listeners will be removed
		// with Mobage.onStop
		Mobage.onRestart(this);
		setMobagePlatformListener();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Mobage.onDestroy(this);
	}

	public void helloMobage() {
		Log.v(TAG, "begin helloMobage");
		User.Field[] fields = { User.Field.ID, User.Field.NICKNAME,
				User.Field.HAS_APP };
		People.getCurrentUser(fields, new OnGetUserComplete() {
			@Override
			public void onSuccess(User user) {
				Log.v(TAG, "helloMobage() Success:" + user.getNickname());
			}

			@Override
			public void onError(Error error) {
				Log.v(TAG, "helloMobage() Error:" + error.getDescription());
			}
		});
	}
}
