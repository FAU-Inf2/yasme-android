package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.fragments.ChatFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

    private MessageEncryption messageEncryption;
    private MessageTask messageTask = MessageTask.getInstance();
    private AsyncTask onPostExecute;
    private List<Message> messages = new ArrayList<>();
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
            }
            Message msg = new Message(sender, msgText, chat, 0);
            this.messages.add(msg);
            try {
                Message ret = messageTask.sendMessage(msg, chat, sender);
                //if (null == DatabaseManager.INSTANCE.getMessageDAO().addIfNotExists(ret)) {
                //    return false;
                //} //Nachricht wird hier ohne Datum abgespeichert -> NullPointerException
            } catch (RestServiceException rse) {
                rse.printStackTrace();
                Log.w(this.getClass().getSimpleName(), rse.getMessage());
                return false;
            }
        }
        return true;
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            Log.i(this.getClass().getSimpleName(), "Sent " + messages.size() + " messages");
            if (null != this.onPostExecute) {
                onPostExecute.execute(); // onPostExecute is a GetMessageTask
                // onPostExecute async task will call notify the registered fragments
            } else {
                ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);
            }
        } else {
            Log.w(this.getClass().getSimpleName(), "Senden fehlgeschlagen");
        }
    }
}
