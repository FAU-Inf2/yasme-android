package net.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 19.06.14.
 */
public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {
    SharedPreferences storage;

    public GetProfileDataTask(SharedPreferences storage) {
        this.storage = storage;
    }

    User selfProfile;
    protected Boolean doInBackground(String... params) {
        try {
            selfProfile = UserTask.getInstance().getUserData();
        } catch (RestServiceException e) {
            Log.d(this.getClass().getSimpleName(),e.getMessage());
            return false;
        }
        return selfProfile != null;
    }

    protected void onPostExecute(final Boolean success) {
        if(!success) {
            return;
        }
        SharedPreferences.Editor editor = storage.edit();
        editor.putLong(AbstractYasmeActivity.USER_ID, selfProfile.getId());
        editor.putString(AbstractYasmeActivity.USER_MAIL, selfProfile.getEmail());
        editor.putString(AbstractYasmeActivity.USER_NAME, selfProfile.getName());
        editor.commit();
    }
}
