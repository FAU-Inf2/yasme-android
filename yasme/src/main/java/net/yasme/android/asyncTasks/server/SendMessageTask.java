package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.*;
import net.yasme.android.ui.fragments.ChatFragment;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

    private MessageEncryption messageEncryption;
    private MessageTask messageTask = MessageTask.getInstance();
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
        if (success) {
            if (null != this.onPostExecute) {
                onPostExecute.execute(); // onPostExecute is a GetMessageTask
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

        // Send
        return  MessageTask.getInstance().sendMessage(message);
    }
}