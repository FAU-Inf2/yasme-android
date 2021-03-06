package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsInfo;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsRemove;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 30.07.14.
 * Modified by Tim Nisslbeck <hu78sapy@stud.cs.fau.de>
 */
public class ChangeUserTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;

    public ChangeUserTask(Chat chat) {
        this.chat = chat;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param params 0 is id of participant to remove
     *               1 is method
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        switch (params[1].intValue()) {
            case 0:
                try {
                    ChatTask.getInstance().removeParticipantFromChat(params[0], chat.getId());
                } catch (RestServiceException e) {
                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                    return false;
                }
                break;
            case 1:
                try {
                    ChatTask.getInstance().addParticipantToChat(params[0], chat.getId());
                } catch (RestServiceException e) {
                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                    return false;
                }
                break;
            default:
                Log.e(this.getClass().getSimpleName(), "Default option not supported. Has to be 0 or 1.");
                return false;
        }
        return true;
    }


    /**
     *
     */
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
            ObservableRegistry.getObservable(ChatSettingsInfo.class).notifyFragments(chat);
            ObservableRegistry.getObservable(ChatSettingsRemove.class).notifyFragments(chat);
//		    ObservableRegistry.getObservable(ChatSettingsAdd.class).notifyFragments(new ChatSettingsAdd.AllUsersFetchedParam(true,chat.getParticipants()));
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
