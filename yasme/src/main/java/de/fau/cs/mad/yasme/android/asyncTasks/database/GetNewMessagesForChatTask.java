package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.MessageDAO;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;

import java.util.List;

/**
 * Created by robert on 28.07.14.
 */
public class GetNewMessagesForChatTask extends AsyncTask<Void, Void, Boolean> {
    private List<Message> messages;
    private MessageDAO messageDao;
    private long chatId;
    private long latestMessageId;

    public GetNewMessagesForChatTask(long latestMessageId, long chatId) {
        messageDao = DatabaseManager.INSTANCE.getMessageDAO();
        this.chatId = chatId;
        this.latestMessageId = latestMessageId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        messages = messageDao.getNewMessagesByChat(chatId, latestMessageId);
        if(messages == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Getting new messages failed");
        }
    }
}
