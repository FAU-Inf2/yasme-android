package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.YasmeChat;
import net.yasme.android.YasmeLogin;
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
    MessageEncryption aes;

    public GetMessageTask(Context context, YasmeChat activity, MessageEncryption aes) {
        this.context = context;
        this.activity = activity;
        this.aes = aes;
    }

    ArrayList<Message> messages;
    MessageTask messageTask = MessageTask.getInstance(activity);

    /**
     * @param params
     *              0 is lastMessageID
     *              1 is user_id
     *              2 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {

        try {
            messages = messageTask.getMessage(Long.parseLong(params[0]), Long.parseLong(params[1]), params[2]);
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
            lastMessageID = messages.size() + lastMessageID;
        } else {
            activity.getStatus().setText("Keine neuen Nachrichten");
        }
    }
}
