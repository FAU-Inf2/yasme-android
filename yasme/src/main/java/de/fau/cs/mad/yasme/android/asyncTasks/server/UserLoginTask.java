package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.connection.AuthorizationTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.LoginFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.06.14.
 */

/**
 * Represents an asynchronous login task used to authenticate the user.
 */
public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    private Boolean plainPassword = false;
    private String email;
    private String password;
    private Class classToNotify;

    public UserLoginTask(Boolean plainPassword, Class classToNotify) {
        this.plainPassword = plainPassword;
        this.classToNotify = classToNotify;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SpinnerObservable.getInstance().registerBackgroundTask(this);
    }

    /**
     * @param params 0 is email
     *               1 is password
     * @return
     */
    protected Boolean doInBackground(String... params) {
        GetInfoTask getInfoTask = new GetInfoTask(0);
        getInfoTask.execute();

        email = params[0].toLowerCase();
        password = params[1];
        try {
            // DEBUG:
            //Log.d(this.getClass().getSimpleName(), "email: " + email + " " + "password: " + password);

            if (plainPassword) {
                PasswordEncryption pwEnc = new PasswordEncryption(new User(email, password));
                password = pwEnc.getSecurePassword();
            }

            String loginReturn[] = AuthorizationTask.getInstance().loginUser(new User(email, password));
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Store email address for later use
            DatabaseManager.INSTANCE.setUserEmail(email);
        }
        ObservableRegistry.getObservable(classToNotify).notifyFragments(
                new LoginFragment.LoginProcessParam(success));
    }
}
