package net.yasme.android.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.yasme.android.asyncTasks.server.GetMessageTask;


/**
 * Created by florianwinklmeier on 19.06.14.
 */
public class GcmIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() { super("GcmIntentService"); }

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.d(this.getClass().getSimpleName(),"GcmIntentService");
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			// Filter messages based on message type. Since it is likely that GCM
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Log.d(this.getClass().getSimpleName(),"Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
					Log.d(this.getClass().getSimpleName(),"Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// If it's a regular GCM message, do some work.
				Log.d(this.getClass().getSimpleName(), "Received message with type message from GCM");
				if (extras.containsKey("type") && extras.get("type").equals("msg")) {
					new GetMessageTask().execute();
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}
