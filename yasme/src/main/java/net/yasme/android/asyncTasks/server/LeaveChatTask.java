package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 02.08.14.
 */
public class LeaveChatTask extends AsyncTask<Long, Void, Boolean> {

    public LeaveChatTask() {
    }


    /**
     *
     * @param params
     *              0 is chatId
     * @return
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            ChatTask.getInstance().removeOneSelfFromChat(params[0]);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
    }
}