package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.IOException;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;

/**
 * Created by robert on 09.09.14.
 */
public class StoreImageTask extends AsyncTask<Long, Void, Boolean> {

    private Bitmap profilePicture;

    public StoreImageTask(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * @param params 0 is userId
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        long userId = params[0];
        User user = DatabaseManager.INSTANCE.getUserDAO().get(userId);
        if (user == null) {
            return false;
        }

        String path;
        try {
            path = PictureManager.INSTANCE.storePicture(user, profilePicture);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        if (path == null || path.isEmpty()) {
            return false;
        }
        user.setProfilePicture(path);
        DatabaseManager.INSTANCE.getUserDAO().update(user);

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
        }
    }
}