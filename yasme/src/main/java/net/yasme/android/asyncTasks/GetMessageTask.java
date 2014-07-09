package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.connection.MessageTask;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<String, Void, Boolean> {
    SharedPreferences storage;

    public GetMessageTask(SharedPreferences storage) {
        this.storage = storage;
    }

    ArrayList<Message> messages;
    long lastMessageId;
    long userId;
    String accessToken;

    /**
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

        try {
            messages = MessageTask.getInstance().getMessage(lastMessageId);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }

        if (messages == null) {
            return false;
        }
        if (messages.isEmpty()) {
            return false;
        }
        DatabaseManager.getInstance().storeMessages(messages);
        return true;
    }


    /**
     * updates Database,
     * stores lastMessageId
     */
    @Override
    protected void onPostExecute(final Boolean success) {

        if (!success) {
            Log.w(this.getClass().getSimpleName(), "UpdateDB not successfull");
            return;
        }

        Log.i(this.getClass().getSimpleName(), "UpdateDB successfull, Messages stored");

        lastMessageId = messages.size() + lastMessageId;
        Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));

        SharedPreferences.Editor editor = storage.edit();
        editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
        editor.commit();

        //TODO: abrufen der neuen nachrichten durch den Chat triggern
        //Observer benachrichtigen
    }
}
