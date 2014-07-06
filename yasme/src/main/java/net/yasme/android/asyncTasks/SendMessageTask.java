package net.yasme.android.asyncTasks;

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

    ChatActivity activity;
    MessageEncryption aes;
    ChatFragment fragment;

    public SendMessageTask(ChatActivity activity, ChatFragment fragment, MessageEncryption aes) {

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

        // encrypt Message
        //String msgEncrypted = aes.encrypt(msg); //TODO
        String msgEncrypted = msg;
        Log.d(this.getClass().getSimpleName(), "Zu sendende Nachricht: " + msgEncrypted);

        // create Message
        User user = new User(uName, uMail,  uId);
        long aesId = aes.getKeyId();
        Message createdMessage = new Message(user, msgEncrypted, Long.parseLong(params[4]), aesId);
        Log.d(this.getClass().getSimpleName(), "AES getKeyID: " + aes.getKeyId());
        /*Log.d(this.getClass().getSimpleName(), "[Debug] " + createdMessage.getMessage() +
                ", " + createdMessage.getId() + ", " + createdMessage.getChatId());
        */
        if(createdMessage == null) {
            Log.d(this.getClass().getSimpleName(), "1 createdMessage is null!");
        }
        if(user == null) {
            Log.d(this.getClass().getSimpleName(), "2 createdMessage is null!");
        }
        if(msgEncrypted == null) {
            Log.d(this.getClass().getSimpleName(), "3 createdMessage is null!");
        }
        if(Long.parseLong(params[4]) == 0) {
            Log.d(this.getClass().getSimpleName(), "4 createdMessage is null!");
        }
        if(aesId == 0) {
            Log.d(this.getClass().getSimpleName(), "5 createdMessage is null!");
        }
        Log.e(this.getClass().getSimpleName(), msgEncrypted + " " + user.getId() + " " +
                Long.parseLong(params[4]) + " " + aesId);
        try {
            if(createdMessage == null) {
                Log.e(this.getClass().getSimpleName(), "createdMessage is null!");
            }
            messageTask.sendMessage(createdMessage);
            return true;
        } catch (RestServiceException e) {
            e.printStackTrace();
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }
        return false;
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            //activity.asyncUpdate();
            fragment.getStatus().setText("Gesendet: " + msg);
            Log.i(this.getClass().getSimpleName(), "Gesendet: " + msg);
        } else {
            fragment.getStatus().setText("Senden fehlgeschlagen");
            Log.w(this.getClass().getSimpleName(), "Senden fehlgeschlagen");
        }
    }
}
