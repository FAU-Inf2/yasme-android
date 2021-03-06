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
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 03.08.14.
 */
public class ChangeOwnerAndLeaveTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;

    public ChangeOwnerAndLeaveTask(Chat chat) {
        this.chat = chat;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param params 0 is id of new owner
     *               1 is additional leave
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            ChatTask.getInstance().changeOwnerOfChat(chat.getId(), params[0]);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        chat.setOwner(DatabaseManager.INSTANCE.getUserDAO().get(params[0]));
        if (params[1] == 1L) {
            try {
                ChatTask.getInstance().removeOneSelfFromChat(chat.getId());
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
            chat.removeParticipant(DatabaseManager.INSTANCE.getUserDAO()
                    .get(DatabaseManager.INSTANCE.getUserId()));
        }
        DatabaseManager.INSTANCE.getChatDAO().update(chat);
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
            new GetMyChatsTask(ChatListFragment.class).startIfNecessary();
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
