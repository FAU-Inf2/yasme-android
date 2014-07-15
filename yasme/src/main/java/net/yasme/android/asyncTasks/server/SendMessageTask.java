package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.fragments.ChatFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<Message, Void, Boolean> {

    private MessageEncryption aes;
    private MessageTask messageTask = MessageTask.getInstance();
    private AsyncTask onPostExecute;
    private List<Message> messages = new ArrayList<>();

    public SendMessageTask(MessageEncryption aes) {
        this.aes = aes;
    }

    public SendMessageTask(MessageEncryption aes, AsyncTask onPostExecute) {
        this.aes = aes;
        this.onPostExecute = onPostExecute;
    }


    /**
     * @param msgs
     * @return true on success and false on error
     */
    protected Boolean doInBackground(Message... msgs) {
        for (Message msg : msgs) {
            if (null == msg) {
                Log.e(this.getClass().getSimpleName(), "Received message is null!");
            }
            this.messages.add(msg);
            try {
                Message ret = messageTask.sendMessage(msg);
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
