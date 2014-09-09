package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.ui.fragments.OwnProfileFragment;

/**
 * Created by robert on 09.09.14.
 */
public class StoreImageTask extends AsyncTask<String, Void, Boolean> {

    private Drawable profilePicture;
    private Class classToNotify;

    public StoreImageTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    /**
     * @param params 0 is userId
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        long userId = Long.parseLong(params[0]);
        return false;
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