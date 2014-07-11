package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.encryption.AESEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;

import java.util.ArrayList;

/**
 * Created by martin on 06.07.2014.
 */
// Async-Task for sending Key to Server
public class SendMessageKeyTask extends AsyncTask<String, Void, MessageKey> {

    private AESEncryption aes;
    private MessageKeyTask messageKeyTask;
    private ArrayList<User> recipients;
    private Chat chat;

    public SendMessageKeyTask(AESEncryption aes, ArrayList<User> recipients, Chat chat) {
        this.aes = aes;
        this.recipients = recipients;
        this.chat = chat;
    }

    protected MessageKey doInBackground(String... params) {

        try {

            String keyBase64 = aes.getKeyinBase64();
            String iv = aes.getIVinBase64();
            String sign = "test";
            //TODO: encTyoe je nach Verschluesselung anpassen
            byte encType = 0;

            // send Key to all Recipients
            MessageKeyTask messageKeyTask = MessageKeyTask.getInstance();
            MessageKey messageKey = messageKeyTask.saveKey(recipients, chat,
                    keyBase64, iv, encType, sign);

            return messageKey;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),"Fail to send key: "+e.getMessage());
        }
        return null;
    }

    protected void onPostExecute(Boolean result) {

    }
}
