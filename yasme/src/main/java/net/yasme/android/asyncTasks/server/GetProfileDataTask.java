package net.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.UserTask;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 19.06.14.
 */
public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {

    User selfProfile;
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        try {
            selfProfile = UserTask.getInstance().getUserData();
        } catch (RestServiceException e) {
            Log.d(this.getClass().getSimpleName(),e.getMessage());
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
        // Don't store the mail. It will be null since the server won't deliver any email addresses any more
        //editor.putString(AbstractYasmeActivity.USER_MAIL, selfProfile.getEmail());
        editor.putString(AbstractYasmeActivity.USER_NAME, selfProfile.getName());
        editor.commit();
    }
}
