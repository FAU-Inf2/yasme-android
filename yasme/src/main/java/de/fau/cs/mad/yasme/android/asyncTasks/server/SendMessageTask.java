package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.connection.MessageTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.KeyOutdatedException;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

    private AsyncTask onPostExecute;
    private Chat chat;
    private User sender;


    //public SendMessageTask(MessageEncryption aes) {
    //    this.aes = aes;
    //}

    public SendMessageTask(Chat chat, User sender, AsyncTask onPostExecute) {
        this.sender = sender;
        this.chat = chat;
        this.onPostExecute = onPostExecute;
    }


    /**
     * @param msgs
     * @return true on success and false on error
     */
    protected Boolean doInBackground(String... msgs) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        for (String msgText : msgs) {
            if (null == msgText) {
                Log.e(this.getClass().getSimpleName(), "Received message is null!");
                continue;
            }

            if (sendMessage(msgText) == null) {
                return false;
            }
        }
        return true;
    }

    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            if (null != this.onPostExecute) {
                onPostExecute.execute(this.getClass().getName()); // onPostExecute is a GetMessageTask
                // onPostExecute async task will call notify the registered fragments
            }
        } else {
            Log.w(this.getClass().getSimpleName(), "Senden fehlgeschlagen");
        }
    }

    private Message sendMessage(String text) {
        try {
            // At first, try with an old key
            return sendMessage(text, false);
        } catch (KeyOutdatedException koe) {
            try {
                // If key is outdated, retry with a generated key
                return sendMessage(text, true);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private Message sendMessage(String text, Boolean forceKeyGeneration) throws KeyOutdatedException {
        // Create message
        Message message = new Message(sender, text, chat, 0);

        // Encrypt
        MessageEncryption messageEncryption = new MessageEncryption(chat, sender);
        if (forceKeyGeneration) {
            message = messageEncryption.encryptGenerated(message);
        } else {
            message = messageEncryption.encrypt(message);
        }

        if (message == null) {
            return null;
        }

        // Send
        return  MessageTask.getInstance().sendMessage(message);
    }
}