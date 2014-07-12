package net.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.asyncTasks.database.AddIfNotExistsTask;
import net.yasme.android.asyncTasks.database.AddOrUpdateTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Message;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.MessageDAO;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatActivity;
import net.yasme.android.ui.ChatFragment;

import java.util.List;

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

    List<Message> messages;
    long lastMessageId;

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
            Log.w(this.getClass().getSimpleName(), "messages is null!");
            return false;
        }
        if (messages.isEmpty()) {
            Log.w(this.getClass().getSimpleName(), "messages is empty");
            return false;
        }
        ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);
        Log.d(this.getClass().getSimpleName(), "Number of messages to store in DB: " + messages.size());
        for(Message msg : messages) {
            new AddOrUpdateTask(DatabaseManager.INSTANCE.getMessageDAO(), msg, ChatActivity.class)
                    .execute();
            if (null != msg.getMessageKey()) {
                new AddIfNotExistsTask(DatabaseManager.INSTANCE.getMessageKeyDAO(), msg.getMessageKey(), ChatActivity.class)
                    .execute();
            }
            //DatabaseManager.INSTANCE.getMessageDAO().addIfNotExists(msg);//storeMessages(messages);
            //DatabaseManager.INSTANCE.getMessageKeyDAO().addIfNotExists(msg.getMessageKey());
        }
        return true;
    }


    /**
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

        // Fragment wird schon weiter oben benachrichtigt
    }
}
