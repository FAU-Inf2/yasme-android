package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.YasmeChat;
import net.yasme.android.YasmeLogin;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeChat activity;
    MessageEncryption aes;

    public SendMessageTask(Context context, YasmeChat activity, MessageEncryption aes) {
        this.context = context;
        this.activity = activity;
        this.aes = aes;
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
     *              4 is accessToken
     * @return true on success and false on error
     */
    protected Boolean doInBackground(String... params) {

        msg = params[0];

        String uName = params[1];
        String uMail = params[2];
        long uId = Long.parseLong(params[3]);

        boolean result = false;

        // encrypt Message
        String msg_encrypted = aes.encrypt(msg);

        // create Message
        Message createdMessage = new Message(new User(uName, uMail,  uId),
                msg_encrypted, chatId, aes.getKeyId());
        System.out.println("AES getKeyID: " + aes.getKeyId());
        try {
            result = messageTask.sendMessage(createdMessage, params[4]);
        } catch (RestServiceException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            update();
            activity.getStatus().setText("Gesendet: " + msg);
        } else {
            activity.getStatus().setText("Senden fehlgeschlagen");
        }
    }
}
