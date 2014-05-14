package com.mobsource.gcm.lib;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * 
 * @author Kinnar Vasa
 *
 */

public class GcmIntentService extends IntentService {
	
	public GcmIntentService(){
		super("WeLoveFresh");
	}
	
	public GcmIntentService(String name) {
		super(name);
	}

	public static final int NOTIFICATION_ID = 1;
	NotificationCompat.Builder builder;

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			sendNotification(extras.toString(), messageType);
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg, String type) {
		GCM.getStateChangeManager().notificationReceivedChange(msg, type);
	}
}
