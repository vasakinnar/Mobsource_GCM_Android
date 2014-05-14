package com.mobsource.gcm.lib;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
/**
 * 
 * @author Kinnar Vasa
 *
 */
public class GCM {
	private static String senderId;
	private static Context context;
	private static String regid;
	private static GoogleCloudMessaging gcm;
	private static int currentVersion;
	private static StateChangeManager stateChangeManager;
	private PackageInfo pInfo;

	/**
	 * 
	 * @param context
	 *            Application context
	 * @param senderId
	 *            Substitute you own sender ID here. This is the project number
	 *            you got from the API Console, as described in
	 *            "Getting Started."
	 * @return
	 */
	public GCM(Context context, String senderId, GCMListener listener) {
		GCM.senderId = senderId;
		GCM.context = context;
		stateChangeManager = new StateChangeManager();
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			GCM.currentVersion = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			GCM.currentVersion = 1;
		}
		stateChangeManager.registerListener(listener);
	}

	public void register() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				stateChangeManager.notifyRegisterChange(RegisterEnum.FAIL, resultCode + "");
				return;
			}
		}
		gcm = GoogleCloudMessaging.getInstance(context);
		regid = getRegistrationId();

		if (regid.isEmpty()) {
			registerInBackground();
		} else {
			stateChangeManager.notifyRegisterChange(RegisterEnum.SUCCESS, regid);
		}

	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private static String getRegistrationId() {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(AppConstant.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(GCM.class.getSimpleName(), "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(AppConstant.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		if (registeredVersion != currentVersion) {
			Log.i(GCM.class.getSimpleName(), "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGcmPreferences(Context context) {
		return context.getSharedPreferences(GCM.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private static void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(senderId);
					msg = regid;
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (msg.startsWith("Error")) {
					stateChangeManager.notifyRegisterChange(RegisterEnum.FAIL, msg);
				} else {
					stateChangeManager.notifyRegisterChange(RegisterEnum.SUCCESS, msg);
				}
			}
		}.execute();
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's shared Pref
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private static void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(AppConstant.PROPERTY_REG_ID, regId);
		Log.e("Register id", "Device register id: " + regId);
		editor.putInt(AppConstant.PROPERTY_APP_VERSION, currentVersion);
		editor.commit();
	}

	public static StateChangeManager getStateChangeManager(){
		return stateChangeManager;
	}
}
