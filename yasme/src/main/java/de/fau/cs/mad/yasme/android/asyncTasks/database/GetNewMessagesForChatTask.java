package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;

import java.util.List;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.MessageDAO;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 28.07.14.
 */
public class GetNewMessagesForChatTask extends AsyncTask<String, Void, Boolean> {
    private List<Message> messages;
    private MessageDAO messageDao;
    private long chatId;
    private long latestMessageId;
    private Class classToNotify;

    public GetNewMessagesForChatTask(long latestMessageId, long chatId, Class classToNotify) {
        messageDao = DatabaseManager.INSTANCE.getMessageDAO();
        this.chatId = chatId;
        this.latestMessageId = latestMessageId;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        messages = messageDao.getNewMessagesByChat(chatId, latestMessageId);
        if (messages == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            if (classToNotify == ChatFragment.class) {
                ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(messages);
            }
        } else {
            Log.w(this.getClass().getSimpleName(), "Getting new messages failed");
        }
    }
}
