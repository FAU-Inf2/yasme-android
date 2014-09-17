package de.fau.cs.mad.yasme.android.controller;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 02.08.14.
 */
public class NewMessageNotificationManager {
    private int numberOfMessages;
    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private SharedPreferences mSettings;
    // mId allows you to update the notification later on.
    private int mId;

    public NewMessageNotificationManager() {
        mContext = DatabaseManager.INSTANCE.getContext();
        numberOfMessages = 0;
        mSettings = DatabaseManager.INSTANCE.getSettings();
        try {
            mNotificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        } catch (NullPointerException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return;
        }
        mBuilder = new NotificationCompat.Builder(mContext)
                .setContentTitle("Yasme")
                .setContentText(mContext.getString(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_notify_y)
                .setPriority(1)
                .setAutoCancel(true)
                .setLargeIcon(getIcon(mContext));
        mId = 1;
    }

    public void clearMessages() {
        this.numberOfMessages = 0;
    }

    private boolean isForeground() {
        //get Context
        Context mContext = DatabaseManager.INSTANCE.getContext();

        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) mContext.
                getSystemService(mContext.ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if (componentInfo.getPackageName().equals(mContext.getPackageName())) {
            return true;
        }

        // If not then our app is not on the foreground.
        return false;
    }

    public void mNotify(final int numberOfNewMessages, final long chatId) {
        if (isForeground()) {
            Log.i(this.getClass().getSimpleName(), "App in foreground");
            return;
        }
        mNotificationManager.cancel(mId);
        numberOfMessages += numberOfNewMessages;

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, ChatActivity.class);
        resultIntent.putExtra(AbstractYasmeActivity.CHAT_ID, chatId);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0,
                resultIntent, 0);
        if (mSettings.getBoolean(AbstractYasmeActivity.NOTIFICATION_VIBRATE, false)) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            Log.d(this.getClass().getSimpleName(), "Vibration should be activated");
        }
        if (mSettings.getBoolean(AbstractYasmeActivity.NOTIFICATION_SOUND, false)) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            Log.d(this.getClass().getSimpleName(), "Sound should be activated");
        }
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setContentInfo("" + numberOfMessages);

        mNotificationManager.notify(mId, mBuilder.build());
    }

    private Bitmap getIcon(Context mContext) {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
    }
}
