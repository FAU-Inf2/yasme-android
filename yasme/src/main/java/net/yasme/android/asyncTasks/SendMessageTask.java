package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.ui.ChatActivity;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.ChatFragment;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    ChatActivity activity;
    MessageEncryption aes;
    ChatFragment fragment;

    public SendMessageTask(Context context, ChatActivity activity, ChatFragment fragment, MessageEncryption aes) {
        this.context = context;
        this.activity = activity;
        this.aes = aes;
        this.fragment = fragment;
    }

    MessageTask messageTask = MessageTask.getInstance(activity);

    String msg;

    /**
     *
     * @param params
     *              0 is message
     *              1 is userName
     *              2 is userMail
     *              3 is userId
     *              4 is chatId
     *              5 is accessToken
     * @return true on success and false on error
     */
    protected Boolean doInBackground(String... params) {

        msg = params[0];

        String uName = params[1];
        String uMail = params[2];
        long uId = Long.parseLong(params[3]);

        boolean result = false;

        // encrypt Message
        //String msg_encrypted = aes.encrypt(msg); //TODO: evtl. loeschen
        String msg_encrypted = msg;

        // create Message
        User user = new User(uName, uMail,  uId);
        long aesId = aes.getKeyId();
        Message createdMessage = new Message(user, msg_encrypted, Long.parseLong(params[4]), aesId);
        System.out.println("AES getKeyID: " + aes.getKeyId());
        try {
            result = messageTask.sendMessage(createdMessage, params[5]);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }
        return result;
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            //activity.asyncUpdate();
            fragment.getStatus().setText("Gesendet: " + msg);
        } else {
            fragment.getStatus().setText("Senden fehlgeschlagen");
        }
    }
}
