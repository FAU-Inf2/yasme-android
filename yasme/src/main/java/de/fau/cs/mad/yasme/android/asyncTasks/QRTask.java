package de.fau.cs.mad.yasme.android.asyncTasks;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.contacts.QR;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;


/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 26.07.2014.
 */
public class QRTask extends AsyncTask<String, Void, Boolean> {

    /**
     * Requests the user's chats from the server and updates the database.
     *
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        QR qr = new QR();
        if (qr.generateQRCode() != null) {
            return true;
        }
        return false;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        QR.finished();
    }
}
