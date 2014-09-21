package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.MessageKeyTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 06.07.2014.
 */
// Async-Task for sending Key to Server
public class DeleteMessageKeyTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param params [0] is keyId
     *               params [1] is DeviceId from User
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            //delete Key
            MessageKeyTask keyTask = MessageKeyTask.getInstance();
            keyTask.deleteKey(params[0]);

        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
    }
}
