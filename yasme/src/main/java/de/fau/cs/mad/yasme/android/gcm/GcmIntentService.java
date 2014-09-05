package de.fau.cs.mad.yasme.android.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import de.fau.cs.mad.yasme.android.controller.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMessageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetUserTask;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;


/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 19.06.14.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    protected void onHandleIntent(Intent intent) {

        Log.d(this.getClass().getSimpleName(), "GcmIntentService");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Filter messages based on message type. Since it is likely that GCM
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d(this.getClass().getSimpleName(), "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(this.getClass().getSimpleName(), "Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // If it's a regular GCM message, do some work.
                Log.d(this.getClass().getSimpleName(), "Received message with type message from GCM");
                if (extras.containsKey("type") && extras.get("type").equals("msg")) {
                    new GetMessageTask(ChatFragment.class).execute();
                } else if (extras.containsKey("type") && extras.get("type").equals("usr")) {
										String userIdS = (String) extras.get("extra");
										if(!(extras.containsKey("extra") && userIdS.matches("[0-9]+"))) {
											Log.e(this.getClass().getSimpleName(), "User id in extra message from GCM was not a numeral.");
        							GcmBroadcastReceiver.completeWakefulIntent(intent);
											return;
										}
										Long userId = Long.parseLong(userIdS);
										Log.d("OOOOOOOOOOOOOOOOOOO","GOT TO UPDATE MY USR DB for id " + userId.toString());
										new GetUserTask(ChatFragment.class,userId).execute();
								}
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
