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
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.connection.DeviceTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.Toaster;
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

    private List<Message> messages;
    private long lastMessageId;

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

            // Store MessageKey if message cotains one
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
        }

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
            //Toaster.getInstance().toast(R.string.authentication_successful, Toast.LENGTH_LONG);
        }else{
            Log.d(this.getClass().getSimpleName(), "[???] MessageKey could not be verified");
            //Toaster.getInstance().toast(R.string.authentication_failed, Toast.LENGTH_LONG);
        }

        //decrypt the key with RSA
        MessageKey messageKey = keyEncryption.decrypt(messageKeyEncrypted);
        // TODO: storeKeyToDatabase
        DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(messageKey);
        Log.d(this.getClass().getSimpleName(), "[???] Key " + messageKey.getId() + " aus den Nachrichten extrahiert und gespeichert");
    }

}
