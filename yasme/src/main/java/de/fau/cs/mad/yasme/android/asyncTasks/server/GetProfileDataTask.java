package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.06.14.
 */
public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {

    User selfProfile;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    protected Boolean doInBackground(String... params) {
        try {
            selfProfile = UserTask.getInstance().getUserData();
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
            return false;
        }
        return selfProfile != null;
    }

    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if(!success) {
            return;
        }
        SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
        editor.putLong(AbstractYasmeActivity.USER_ID, selfProfile.getId());
        editor.putString(AbstractYasmeActivity.USER_NAME, selfProfile.getName());
        editor.commit();
    }
}
