package net.yasme.android.asyncTasks.server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.yasme.android.R;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.activities.ChatActivity;
import net.yasme.android.ui.fragments.ChatFragment;

import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<Object, Void, Boolean> {
    SharedPreferences storage;

    public GetMessageTask() {
        this.storage = DatabaseManager.INSTANCE.getSharedPreferences();
    }

    List<Message> messages;
    long lastMessageId;

    /**
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(Object... params) {
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

        try {
            messages = MessageTask.getInstance().getMessage(lastMessageId);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }

        if (messages == null) {
            Log.w(this.getClass().getSimpleName(), "messages is null!");
            return false;
        }
        if (messages.isEmpty()) {
            Log.i(this.getClass().getSimpleName(), "messages is empty");
            return true;
        }

        //add new messages to DB
        Log.d(this.getClass().getSimpleName(), "Number of messages to store in DB: " + messages.size());
        for(Message msg : messages) {
            Log.d(this.getClass().getSimpleName(), msg.getMessage() + " " + msg.getId());
            //DatabaseManager.INSTANCE.getMessageDAO().add(msg);//storeMessages(messages);
            //DatabaseManager.INSTANCE.getMessageKeyDAO().add(msg.getMessageKey());

            if (null == DatabaseManager.INSTANCE.getMessageDAO().addOrUpdate(msg)) {
                Log.e(this.getClass().getSimpleName(), "Storing a message in database failed");
            }

            if (null != msg.getMessageKey()) {
                if (null == DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(msg.getMessageKey())) {
                    Log.e(this.getClass().getSimpleName(), "Storing a message key in database failed");
                }
            }
        }

        //increase and store lastMessageId
        lastMessageId = messages.get(messages.size()-1).getId();
        Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));

        SharedPreferences.Editor editor = storage.edit();
        editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
        editor.commit();

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "No success");
            return;
        }
        Log.i(this.getClass().getSimpleName(), "UpdateDB successfull, Messages stored");
        ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);

        // Vibrate
//        if (messages.size() > 0) {
//            Context context = DatabaseManager.INSTANCE.getContext();
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//            Intent targetIntent = new Intent(context, ChatActivity.class);
//            targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    targetIntent, 0);
//
//            String content = "Received " + messages.size() + " new message";
//            content += (messages.size() > 1) ? "s" : "";
//            NotificationCompat.Builder mBuilder =
//                    new NotificationCompat.Builder(context)
//                            .setSmallIcon(R.drawable.ic_launcher)
//                            .setContentTitle("YASME")
//                            .setStyle(new NotificationCompat.BigTextStyle()
//                                    .bigText(content))
//                            .setContentText(content)
//                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.FLAG_AUTO_CANCEL);
//
//            mBuilder.setContentIntent(contentIntent);
//            notificationManager.notify(1, mBuilder.build());
//        }

        //Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
}
