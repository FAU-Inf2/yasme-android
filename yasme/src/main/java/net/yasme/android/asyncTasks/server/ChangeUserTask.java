package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 30.07.14.
 */
public class ChangeUserTask  extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;

    public ChangeUserTask(Chat chat) {
        this.chat = chat;
    }


    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            ChatTask.getInstance().removePartipantFromChat(params[0], chat.getId());
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }


    /**
     *
     */
    protected void onPostExecute(final Boolean success) {
        if(success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
