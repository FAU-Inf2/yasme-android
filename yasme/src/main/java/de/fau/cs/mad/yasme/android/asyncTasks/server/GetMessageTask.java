package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.List;

import de.fau.cs.mad.yasme.android.connection.DeviceTask;
import de.fau.cs.mad.yasme.android.connection.MessageKeyTask;
import de.fau.cs.mad.yasme.android.connection.MessageTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NewMessageNotificationManager;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.KeyEncryption;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.DebugManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.06.14.
 */

/* TODO: extend Method so keys can be fetched and deleted */
public class GetMessageTask extends AsyncTask<Object, Void, Boolean> {

    private List<Message> messages;
    private long lastMessageId;
    private NewMessageNotificationManager notifier;
    private Class classToNotify;

    public GetMessageTask(Class classToNotify) {
        notifier = DatabaseManager.INSTANCE.getNotifier();
        this.classToNotify = classToNotify;
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(Object... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        lastMessageId = DatabaseManager.INSTANCE.getSharedPreferences()
                .getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

        try {
            messages = MessageTask.getInstance().getMessages(lastMessageId);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
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
        //Log.d(this.getClass().getSimpleName(), "Number of messages to store in DB: " + messages.size());
        for (Message message : messages) {
            // Log.d(this.getClass().getSimpleName(), message.getMessage() + " " + message.getId());

            // Refresh Sender-User-Data
            User sender = message.getSender();
            User dbUser = DatabaseManager.INSTANCE.getUserDAO().get(sender.getId());
            //Log.d(getClass().getSimpleName(), "Sender lastMod: " + sender.getLastModified());
            if (dbUser == null || dbUser.getLastModified() == null || sender.getLastModified() == null || sender.getLastModified().compareTo(dbUser.getLastModified()) > 0) {
                //Log.d(getClass().getSimpleName(), "Sender has to be refreshed");
                RefreshTask refreshTask = new RefreshTask(RefreshTask.RefreshType.USER, sender.getId(), true);
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
                //Log.d(getClass().getSimpleName(), "Message successfully stored");
                if (message.getChat() != null) {
                    //Log.d(getClass().getSimpleName(), "Get Chat from DB");
                    ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
                    Chat chat = chatDAO.get(message.getChat().getId());
                    if (chat != null) {
                        chat.setLastMessage(message);
                        chatDAO.addOrUpdate(chat);
                        //Log.d(getClass().getSimpleName(), "Set lastMessage for chat " + chat.getId() +": " + message.getId());
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
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            Log.e(this.getClass().getSimpleName(), "No success");
            return;
        } else {
            //Log.i(this.getClass().getSimpleName(), "UpdateDB successfull, Messages stored");

            int size = messages.size();
            if (size > 0) {
                // Store lastMessageId
                //Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId) + " this message id: " + messages.get(size-1).getId());

                SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
                editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
                editor.commit();

                
                if (!(size == 1 && messages.get(0).getSender().getId() == DatabaseManager.INSTANCE.getUserId())) {
                    notifier.mNotify(size, messages.get(size - 1).getChatId());
                }
            }

            if (classToNotify == ChatFragment.class) {
                ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(null);
            } else if (classToNotify == ChatListFragment.class) {
                ObservableRegistry.getObservable(ChatListFragment.class).notifyFragments(null);
            }
        }
    }

    private Message decrypt(Message message) {
        Chat chat = DatabaseManager.INSTANCE.getChatDAO().get(message.getChat().getId());
        User sender = DatabaseManager.INSTANCE.getUserDAO().get(message.getSender().getId());
        message.setChat(chat);
        message.setSender(sender);

        // Decrypt
        MessageEncryption messageEncryption = new MessageEncryption(chat, sender);
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
            Log.e(getClass().getSimpleName(), "CreatorDevice is null");
            creator = null;
        }
        messageKeyEncrypted.setCreatorDevice(creator);
        if (creator != null) {
            //Log.d(getClass().getSimpleName(), "Creator publicKey: " + messageKeyEncrypted.getCreatorDevice().getPublicKey());
        }

        KeyEncryption keyEncryption = new KeyEncryption();

        //verify the signature of the key and save authenticity-status in messageKeyEncrypted
        if (messageKeyEncrypted.setAuthenticity(keyEncryption.verify(messageKeyEncrypted))) {
            Log.d(this.getClass().getSimpleName(), "MessageKey has successfully been verified");
        } else {
            Log.d(this.getClass().getSimpleName(), "MessageKey could not be verified");
        }

        //decrypt the key with RSA
        MessageKey messageKey = keyEncryption.decrypt(messageKeyEncrypted);
        if (messageKey != null && DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(messageKey) != null) {
            try {
                MessageKeyTask.getInstance().deleteKey(messageKey.getId());
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }

            // For Developer-Devices only
            if (DebugManager.INSTANCE.isDebugMode()) {
                //Log.d(getClass().getSimpleName(), "Store messageKey to external storage");
                DebugManager.INSTANCE.storeMessageKeyToExternalStorage(messageKey);
            }
        }
        Log.d(this.getClass().getSimpleName(), "Key " + messageKey.getId() + " extracted from Message and saved");
    }
}
