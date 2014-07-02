package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
// geloescht werden
public class GetMessageTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    SharedPreferences storage;

    public GetMessageTask(Context context, SharedPreferences storage) {
        this.context = context;
        this.storage = storage;
    }

    ArrayList<Message> messages;

    long lastMessageId;
    long userId;
    String accessToken;

    /**
     * @param params
     *              0 is userId
     *              1 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);
        userId = Long.parseLong(params[0]);
        accessToken = params[1];
        MessageTask messageTask = MessageTask.getInstance(context);

        try {
            messages = messageTask.getMessage(lastMessageId);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }

        if (messages == null) {
            return false;
        }
        if (messages.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * updates Database,
     * stores lastMessageId
     */
    protected void onPostExecute(final Boolean success) {
        if (success) {
            new InsertNewMessagesTask(messages).execute(Long.toString(lastMessageId),
                    Long.toString(userId), accessToken);
            Log.d(this.getClass().getSimpleName(), "After update the Database");

            lastMessageId = messages.size() + lastMessageId;
            Log.d(this.getClass().getSimpleName(), "LastMessageId: " + Long.toString(lastMessageId));

            SharedPreferences.Editor editor = storage.edit();
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
            editor.commit();

        } else {
            Log.i(this.getClass().getSimpleName(), "Keine neuen Nachrichten");
            Toast.makeText(context, "Keine neuen Nachrichten", Toast.LENGTH_SHORT).show();
        }
    }
}
