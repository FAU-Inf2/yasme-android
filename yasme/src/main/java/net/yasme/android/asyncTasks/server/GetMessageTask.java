package net.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.DeviceTask;
import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.NewMessageNotificationManager;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.encryption.KeyEncryption;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.DebugManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.fragments.ChatFragment;
import net.yasme.android.ui.fragments.ChatListFragment;

import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<Object, Void, Boolean> {

    private List<Message> messages;
    private long lastMessageId;
    private NewMessageNotificationManager notifier;

    public GetMessageTask() {
        notifier = DatabaseManager.INSTANCE.getNotifier();
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(Object... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        lastMessageId = DatabaseManager.INSTANCE.getSharedPreferences().getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

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
            } else {
                Log.d(getClass().getSimpleName(), "Message successfully stored");
                if (message.getChat() != null) {
                    Log.d(getClass().getSimpleName(), "Get Chat from DB");
                    ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
                    Chat chat = chatDAO.get(message.getChat().getId());
                    if (chat != null) {
                        chat.setLastMessage(message);
                        chatDAO.addOrUpdate(chat);
                        Log.d(getClass().getSimpleName(), "Set lastMessage for chat " + chat.getId() +": " + message.getId());
                    } else {
                        Log.e(getClass().getSimpleName(), "Chat not found in DB");
                    }
                } else {
                    Log.e(getClass().getSimpleName(), "Chat not found in Message");
                }
            }
            lastMessageId = Math.max(lastMessageId, message.getId());
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "No success");
            SpinnerObservable.getInstance().removeBackgroundTask(this);
            return;
        }
        Log.i(this.getClass().getSimpleName(), "UpdateDB successfull, Messages stored");

        if(messages.size() > 0) {
            // Store lastMessageId
            Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));

            SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
            editor.commit();

            if(!(messages.size() == 1 && messages.get(0).getSender().getId() == DatabaseManager.INSTANCE.getUserId())) {
                notifier.mNotify(messages.size());
            }
        }
        //For notification testing:
        //notifier.mNotify(messages.size());


        ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(null);
        ObservableRegistry.getObservable(ChatListFragment.class).notifyFragments(null);
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
        SpinnerObservable.getInstance().removeBackgroundTask(this);
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
        if (messageKey != null && DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(messageKey) != null) {
            try {
                MessageKeyTask.getInstance().deleteKey(messageKey.getId());
            } catch(Exception e) {

            }

            // For Developer-Devices only
            if (DebugManager.INSTANCE.isDebugMode()) {
                Log.d(getClass().getSimpleName(), "Store messageKey to external storage");
                DebugManager.INSTANCE.storeMessageKeyToExternalStorage(messageKey);
            }
        }
        Log.d(this.getClass().getSimpleName(), "[???] Key " + messageKey.getId() + " aus den Nachrichten extrahiert und gespeichert");
    }
}
