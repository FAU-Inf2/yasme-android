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
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 29.07.14.
 */
public class ChangeChatProperties extends AsyncTask<String, Void, Boolean> {
    private Chat chat;
    private Class classToNotify;

    public ChangeChatProperties(Chat chat, Class classToNotify) {
        this.chat = chat;
        this.classToNotify = classToNotify;
    }


    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        try {
            ChatTask.getInstance().updateChat(chat);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        DatabaseManager.INSTANCE.getChatDAO().update(chat);
        return true;
    }

    /**
     *
     */
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            ObservableRegistry.getObservable(classToNotify).notifyFragments(chat);
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
