package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.entities.MessageKey;

/**
 * Created by martin on 06.07.2014.
 */
// Async-Task for sending Key to Server
public class DeleteMessageKeyTask extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {

            /**
             * @param params [0] is keyId
             *        params [1] is DeviceId from User
             * @return Returns true if it was successful, otherwise false
             */

            try {

                //delete Key
                MessageKeyTask keytask = MessageKeyTask.getInstance();
                keytask.deleteKey(params[0]);

            } catch (Exception e) {
                Log.d(this.getClass().getSimpleName(),e.getMessage());
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {

        }
}
