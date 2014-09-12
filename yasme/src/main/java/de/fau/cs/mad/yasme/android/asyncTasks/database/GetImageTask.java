package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfilePictureTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.fragments.OwnProfileFragment;

/**
 * Created by robert on 09.09.14.
 */
public class GetImageTask extends AsyncTask<Long, Void, Boolean> {

    private Bitmap profilePicture;
    private Class classToNotify;
    long userId;

    public GetImageTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    /**
     * @param params 0 is userId
     *               1 is required height
     *               2 is required width
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        userId = params[0];
        int height = params[1].intValue();
        int width = params[2].intValue();
        User user = DatabaseManager.INSTANCE.getUserDAO().get(userId);
        if (user == null) {
            Log.e(this.getClass().getSimpleName(), "User could not be found");
            return false;
        }
        profilePicture = PictureManager.INSTANCE.getPicture(user, height, width);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            if (profilePicture == null) {
                new GetProfilePictureTask(classToNotify).execute(userId);
                return;
            }
            // Notify registered fragments
            FragmentObservable<OwnProfileFragment, Drawable> obs =
                    ObservableRegistry.getObservable(classToNotify);
            obs.notifyFragments(new BitmapDrawable(
                    DatabaseManager.INSTANCE.getContext().getResources(), profilePicture));
        }
    }
}
