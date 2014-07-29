package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.MessageDAO;
import net.yasme.android.ui.fragments.ChatFragment;

import java.util.List;

/**
 * Created by robert on 28.07.14.
 */
public class GetNewMessagesForChatTask extends AsyncTask<Void, Void, Boolean> {
    private List<Message> messages;
    private MessageDAO messageDao;
    private long chatId;
    private int numberOfMessages;

    public GetNewMessagesForChatTask(int numberOfMessages, long chatId) {
        messageDao = DatabaseManager.INSTANCE.getMessageDAO();
        this.chatId = chatId;
        this.numberOfMessages = numberOfMessages;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        messages = messageDao.getMessagesByChatAndNumberOfMessages(chatId, numberOfMessages);
        if(messages == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Notify
            ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Get new Messages not successful");
        }
    }
}