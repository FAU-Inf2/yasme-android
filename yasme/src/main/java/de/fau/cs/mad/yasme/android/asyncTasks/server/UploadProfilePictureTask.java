package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(String... params) {
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
            Log.d(this.getClass().getSimpleName(), "Successfully uploaded profile picture");
            Toaster.getInstance().toast(R.string.picture_changed, Toast.LENGTH_LONG);
        }
    }
}
