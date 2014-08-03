package net.yasme.android.controller;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import net.yasme.android.R;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.activities.ChatListActivity;

import java.util.List;

/**
 * Created by robert on 02.08.14.
 */
public class NewMessageNotificationManager {

    public boolean isForeground(String PackageName){
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
        if(componentInfo.getPackageName().equals(PackageName)) return true;

        // If not then our app is not on the foreground.
        return false;
    }

    public void mNotify(int numberOfNewMessages) {
        if(!isForeground("net.yasme.android")) {
            return;
        }
        Context mContext = DatabaseManager.INSTANCE.getContext();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle("Yasme")
                        .setContentText(mContext.getString(R.string.notification_message))
                        .setContentInfo("" + numberOfNewMessages)
                        .setSmallIcon(android.R.drawable.ic_dialog_email)
                        .setPriority(1)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setAutoCancel(true)
                        .setLargeIcon(getIcon(mContext));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, ChatListActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0,
                resultIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private Bitmap getIcon(Context mContext) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        return BitmapFactory.decodeResource(mContext.getResources(), R.raw.logo, options);
    }
}
