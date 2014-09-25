package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 25.09.14.
 */
public class ChangePasswordTask extends AsyncTask<String, Void, Boolean> {
    private User user;

    public ChangePasswordTask(User user) {
        this.user = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param params 0 is flag for preparation to change password
     *               1 is mailToken
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return false;
        }
        if (params[0].equals("1")) {
            try {
                UserTask.getInstance().requirePasswordToken(user);
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        } else {
            try {
                UserTask.getInstance().changePassword(user, params[1]);
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            Toaster.getInstance().toast(R.string.successful_changed_password, Toast.LENGTH_LONG);
        } else {
            Toaster.getInstance().toast(R.string.error_change_password, Toast.LENGTH_LONG);
        }
    }
}
