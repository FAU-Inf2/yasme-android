package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 09.09.14.
 */
public class UploadProfilePictureTask extends AsyncTask<String, Void, Boolean> {

    private Bitmap profilePicture;

    public UploadProfilePictureTask(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        try {
            UserTask.getInstance().uploadProfilePicture(profilePicture);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            Log.d(this.getClass().getSimpleName(), "Successful uploaded profile picture");
            Toaster.getInstance().toast("Successful uploaded profile picture", Toast.LENGTH_LONG);
        }
    }
}