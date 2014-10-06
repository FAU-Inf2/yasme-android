package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.MessageTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.KeyOutdatedException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

    private AsyncTask<Object,Void,Boolean> onPostExecute; // could also be a GetMessageTask
    private Chat chat;
    private User sender;
    private Mime type;

    public SendMessageTask(Chat chat, User sender, AsyncTask<Object,Void,Boolean> onPostExecute, Mime type) {
        this.sender = sender;
        this.chat = chat;
        this.onPostExecute = onPostExecute;
        this.type = type;
    }


    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param msgs
     * @return true on success and false on error
     */
    protected Boolean doInBackground(String... msgs) {
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
                onPostExecute.execute(); // onPostExecute is a GetMessageTask
                // onPostExecute async task will call notify the registered fragments
            }
        } else {
            Log.e(this.getClass().getSimpleName(), "Send failed");
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
                Log.e(this.getClass().getSimpleName(),e.getMessage());
                return null;
            }
        }
    }

    private Message sendMessage(String text, Boolean forceKeyGeneration) throws KeyOutdatedException {
        // Create message
        Message message;
        if (type == Mime.PLAIN) {
            message = new Message(sender, text, chat, 0,
                    DatabaseManager.INSTANCE.getContext().getResources().getString(R.string.mime_text));
        } else if (type == Mime.IMAGE) {
            message = new Message(sender, text, chat, 0,
                    DatabaseManager.INSTANCE.getContext().getResources().getString(R.string.mime_image));
        } else {
            Log.e(this.getClass().getSimpleName(), "Error during creating a new message");
            return null;
        }

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

    public enum Mime {
        PLAIN, //"text/plain"
        IMAGE; //"media/image"
    }
}
