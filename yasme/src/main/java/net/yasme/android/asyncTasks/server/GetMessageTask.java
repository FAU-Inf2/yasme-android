package net.yasme.android.asyncTasks.server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.yasme.android.R;
import net.yasme.android.connection.DeviceTask;
import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.KeyEncryption;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.activities.ChatListActivity;
import net.yasme.android.ui.fragments.ChatFragment;

import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<Object, Void, Boolean> {
    SharedPreferences storage;
    private List<Message> messages;
    private long lastMessageId;
    public GetMessageTask() {
        this.storage = DatabaseManager.INSTANCE.getSharedPreferences();
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(Object... params) {
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

        try {
            messages = MessageTask.getInstance().getMessages(lastMessageId);
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
        for(Message message : messages) {
            Log.d(this.getClass().getSimpleName(), message.getMessage() + " " + message.getId());

            // Refresh Sender-User-Data
            User sender = message.getSender();
            User dbUser = DatabaseManager.INSTANCE.getUserDAO().get(sender.getId());
            Log.d(getClass().getSimpleName(), "Sender lastMod: " + sender.getLastModified());
            if (dbUser == null || dbUser.getLastModified() == null || sender.getLastModified() == null || sender.getLastModified().compareTo(dbUser.getLastModified()) > 0) {
                Log.d(getClass().getSimpleName(), "Sender has to be refreshed");
                RefreshTask refreshTask = new RefreshTask(RefreshTask.RefreshType.USER,sender.getId(),true);
                refreshTask.execute();
            }

            // Store MessageKey if message contains one
            storeMessageKey(message);
            // Decrypt message
            message = decrypt(message);

            if (null == DatabaseManager.INSTANCE.getMessageDAO().addIfNotExists(message)) {//changed from addOrUpdate
                Log.e(this.getClass().getSimpleName(), "Storing a message in database failed");
                return false;
            }
            lastMessageId = Math.max(lastMessageId, message.getId());
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "No success");
            return;
        }
        Log.i(this.getClass().getSimpleName(), "UpdateDB successfull, Messages stored");

        if(messages.size() > 0) {
            // Store lastMessageId
            Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));

            SharedPreferences.Editor editor = storage.edit();
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
            editor.commit();

            mNotify(messages.size());
        }
        //For notification testing:
        //mNotify(messages.size());


        ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(null);
        //ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);

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

    private void mNotify(int numberOfNewMessages) {


        Context mContext = DatabaseManager.INSTANCE.getContext();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle("Yasme")
                        .setContentText("Received new messages")
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

    private Message decrypt(Message message) {
        Chat chat = DatabaseManager.INSTANCE.getChatDAO().get(message.getChat().getId());
        User sender = DatabaseManager.INSTANCE.getUserDAO().get(message.getSender().getId());
        message.setChat(chat);
        message.setSender(sender);

        // Decrypt
        MessageEncryption messageEncryption = new MessageEncryption(chat,sender);
        message = messageEncryption.decrypt(message);
        return message;

    }

    private void storeMessageKey(Message message) {
        MessageKey messageKeyEncrypted = message.getMessageKey();
        if (messageKeyEncrypted == null) {
            return;
        }
        Device creator;
        try {
            creator = DeviceTask.getInstance().getDevice(messageKeyEncrypted.getCreatorDevice().getId());
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "CreatorDevice is null");
            creator = null;
        }
        messageKeyEncrypted.setCreatorDevice(creator);
        if (creator != null) {
            Log.d(getClass().getSimpleName(), "Creator publicKey: " + messageKeyEncrypted.getCreatorDevice().getPublicKey());
        }

        KeyEncryption keyEncryption = new KeyEncryption();

        //verify the signature of the key and save authenticity-status in messageKeyEncrypted
        if(messageKeyEncrypted.setAuthenticity(keyEncryption.verify(messageKeyEncrypted))){
            Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been verified");
        }else{
            Log.d(this.getClass().getSimpleName(), "[???] MessageKey could not be verified");
        }

        //decrypt the key with RSA
        MessageKey messageKey = keyEncryption.decrypt(messageKeyEncrypted);
        // TODO: storeKeyToDatabase
        if (DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(messageKey) != null) {
            try {
                MessageKeyTask.getInstance().deleteKey(messageKey.getId());
            } catch(Exception e) {

            }
        }
        Log.d(this.getClass().getSimpleName(), "[???] Key " + messageKey.getId() + " aus den Nachrichten extrahiert und gespeichert");
    }
}
