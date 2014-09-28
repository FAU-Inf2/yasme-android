package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.IOException;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.fragments.SearchContactFragment;

/**
 * Created by robert on 27.09.14.
 */
public class GetImageWithoutSavingTask extends AsyncTask<Long, Void, Boolean> {

    private BitmapDrawable profilePicture;
    private Class classToNotify;
    private boolean isSelf;
    private User user;

    public GetImageWithoutSavingTask(Class classToNotify, User user) {
        this.classToNotify = classToNotify;
        this.user = user;
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
    protected Boolean doInBackground(Long... params) {
        long userId = user.getId();

        try {
            profilePicture = UserTask.getInstance().getProfilePicture(userId);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        if (profilePicture == null) {
            Log.d(this.getClass().getSimpleName(), "profilePicture was null");
            user.setProfilePicture(null);
            return true;
        }

        isSelf = (userId == DatabaseManager.INSTANCE.getUserId());

        String path;
        try {
            path = PictureManager.INSTANCE.storePicture(user, profilePicture.getBitmap());
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        if (path != null && !path.isEmpty()) {
            user.setProfilePicture(path);
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            if (classToNotify == null) {
                //No one to notify
                return;
            }
            // Notify registered fragments
            FragmentObservable<SearchContactFragment, SearchContactFragment.DataClass> obs =
                    ObservableRegistry.getObservable(classToNotify);
            obs.notifyFragments(new SearchContactFragment.ImageClass(profilePicture, user));
        }
    }
}
