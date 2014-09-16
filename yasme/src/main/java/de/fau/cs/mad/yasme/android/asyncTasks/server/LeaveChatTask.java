package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 02.08.14.
 */
public class LeaveChatTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;

    public LeaveChatTask(Chat chat) {
        this.chat = chat;
    }

    /**
     * @return true an success, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        try {
            ChatTask.getInstance().removeOneSelfFromChat(chat.getId());
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_SHORT);
            new GetMyChatsTask(ChatListFragment.class).startIfNecessary();
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_SHORT);
        }
    }
}