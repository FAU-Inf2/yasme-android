package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * @Author Tim Nisslbeck (hu78sapy@stud.cs.fau.de), 29.08.14
 */
public class SetProfileDataTask extends AsyncTask<Void, Void, Boolean> {

    private User selfProfile;

    public SetProfileDataTask(User selfUser) {
        selfProfile = selfUser;
    }

    protected Boolean doInBackground(Void... nothing) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        // Sanitize input
        try {
            UserTask.getInstance().changeUserData(selfProfile);
        } catch (RestServiceException rse) {
            Log.e(this.getClass().getSimpleName(), rse.getMessage());
            return false;
        }
        return selfProfile != null;
    }

    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            return;
        }
        SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
        editor.putString(AbstractYasmeActivity.USER_NAME, selfProfile.getName());
        editor.commit();
    }
}
