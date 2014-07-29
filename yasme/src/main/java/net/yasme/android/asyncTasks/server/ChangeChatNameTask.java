package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import net.yasme.android.entities.Chat;

/**
 * Created by robert on 29.07.14.
 */
public class ChangeChatNameTask extends AsyncTask<String, Void, Boolean> {
    private Chat chat;

    public ChangeChatNameTask(Chat chat) {
        this.chat = chat;
    }


    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        /*try {
            //ChatTask.getInstance().updateName(chat);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }*/
        return true;
    }


    /**
     * Invokes the fragment's method to show the chat activity
     */
    protected void onPostExecute(final Boolean success) {

    }
}
