package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.OwnProfileFragment;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 09.07.14.
 */
public class GetProfilePictureTask extends AsyncTask<String, Void, Boolean> {

    private Drawable profilePicture;
    private Class classToNotify;


    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        long userId = Long.parseLong(params[0]);

        try {
            profilePicture = UserTask.getInstance().getProfilePicture(userId);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success && null != profilePicture) {
            if (classToNotify == ChatListFragment.class) {
                // Notify registered fragments
                FragmentObservable<OwnProfileFragment, Drawable> obs =
                        ObservableRegistry.getObservable(ChatListFragment.class);
                obs.notifyFragments(profilePicture);
            }
        }
    }
}
