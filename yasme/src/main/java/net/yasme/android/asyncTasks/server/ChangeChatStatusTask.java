package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 29.07.14.
 */
public class ChangeChatStatusTask extends AsyncTask<String, Void, Boolean> {
    private Chat chat;

    public ChangeChatStatusTask(Chat chat) {
        this.chat = chat;
    }


    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            ChatTask.getInstance().updateStatus(chat);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Invokes the fragment's method to show the chat activity
     */
    protected void onPostExecute(final Boolean success) {

    }
}
