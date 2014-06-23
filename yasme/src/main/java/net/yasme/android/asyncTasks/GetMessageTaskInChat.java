package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatActivity;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */

public class GetMessageTaskInChat extends AsyncTask<String, Void, Boolean> {
    Context context;
    ChatActivity activity;
    SharedPreferences storage;
    MessageEncryption aes;

    public GetMessageTaskInChat(Context context, ChatActivity activity, MessageEncryption aes, SharedPreferences storage) {
        this.context = context;
        this.activity = activity;
        this.storage = storage;
        this.aes = aes;
    }

    ArrayList<Message> messages;
    MessageTask messageTask = MessageTask.getInstance(context);
    long lastMessageId;
    long userId;
    String accessToken;

    /**
     * @param params
     *              0 is userId
     *              1 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);
        userId = Long.parseLong(params[0]);
        accessToken = params[1];

        try {
            messages = messageTask.getMessage(lastMessageId, userId, accessToken);
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
        //for (Message msg : messages) {
        //    msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getMessageKeyId())));
        //}

        return true;
    }

    /**
     * Fills the TextViews with the messages,
     * updates Database,
     * stores lastMessageId
     */
    protected void onPostExecute(final Boolean success) {
        if (success) {
            activity.updateViews(messages);
            new UpdateDBTask(context, messages).execute(Long.toString(lastMessageId),
                    Long.toString(userId), accessToken);

            Log.d(this.getClass().getSimpleName(), "After update the Database");
            lastMessageId = messages.size() + lastMessageId;
            Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));
            SharedPreferences.Editor editor = storage.edit();
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
            editor.commit();
        } else {
            Log.i(this.getClass().getSimpleName(), "Keine neuen Nachrichten");
            activity.getStatus().setText("Keine neuen Nachrichten");
        }
    }
}
