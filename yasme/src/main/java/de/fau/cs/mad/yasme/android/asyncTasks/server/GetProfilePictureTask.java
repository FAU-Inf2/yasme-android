package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.ui.fragments.OwnProfileFragment;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 09.07.14.
 */
public class GetProfilePictureTask extends AsyncTask<Long, Void, Boolean> {

    private Drawable profilePicture;
    private Class classToNotify;

    public GetProfilePictureTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    /**
     * @param params 0 is userId
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        long userId = params[0];

        try {
            profilePicture = UserTask.getInstance().getProfilePicture(userId);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return profilePicture != null;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify registered fragments
            FragmentObservable<OwnProfileFragment, Drawable> obs =
                    ObservableRegistry.getObservable(classToNotify);
            obs.notifyFragments(profilePicture);
        }
    }
}
