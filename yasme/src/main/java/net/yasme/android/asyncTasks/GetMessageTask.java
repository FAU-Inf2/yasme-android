package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.Constants;
import net.yasme.android.YasmeChat;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeChat activity;
    SharedPreferences storage;
    MessageEncryption aes;

    public GetMessageTask(Context context, YasmeChat activity, SharedPreferences storage, MessageEncryption aes) {
        this.context = context;
        this.activity = activity;
        this.storage = storage;
        this.aes = aes;
    }

    ArrayList<Message> messages;
    MessageTask messageTask = MessageTask.getInstance(activity);
    long lastMessageId;

    /**
     * @param params 1 is user_id
     *               2 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        lastMessageId = storage.getLong(Constants.LAST_MESSAGE_ID, 0L);
        try {
            messages = messageTask.getMessage(lastMessageId, Long.parseLong(params[0]), params[1]);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }

        if (messages == null) {
            return false;
        }
        if (messages.isEmpty()) {
            return false;
        }

        // decrypt Messages
        for (Message msg : messages) {
            msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getMessageKeyId())));
        }

        return true;
    }

    /**
     * Fills the TextViews with the messages
     * - maybe this should be done also in doInBackground
     */
    protected void onPostExecute(final Boolean success) {
        if (success) {
            activity.updateViews(messages);
            lastMessageId = messages.size() + lastMessageId;
        } else {
            activity.getStatus().setText("Keine neuen Nachrichten");
        }
    }
}
