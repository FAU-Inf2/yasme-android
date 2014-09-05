package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.RegisterFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.06.14.
 */

/**
 * Represents an asynchronous task used to register the user.
 */
public class UserRegistrationTask extends AsyncTask<String, Void, Boolean> {

    private String name;
    private String email;
    private String password;
    private Class classToNotify;
    private long userId;
    private int message = R.string.registration_successful;

    public UserRegistrationTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }


    /**
     * @params params[0] is name
     * @params params[1] is email
     * @params params[2] is password
     * @params params[3] is password_check
     */
    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);

        name = params[0];
        email = params[1].toLowerCase();
        password = params[2];
        String password_check = params[3];

        if (!password.equals(password_check)) {
            return false;
        }
        try {
            PasswordEncryption pwEnc = new PasswordEncryption(new User(email, password));
            password = pwEnc.getSecurePassword();
            userId = UserTask.getInstance().registerUser(new User(password, name, email));
        } catch (RestServiceException e) {
            message = R.string.registration_not_successful;
            if (e.getCode() == Error.BAD_EMAIL.getNumber()) {
                message = R.string.email_invalid;
            }
            if (e.getCode() == Error.FORBIDDEN.getNumber()) {
                message = R.string.email_exists;
            }
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        // Ask server for news, i.e. new version of app
        new GetInfoTask().execute();

        if (success) {
            SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
            editor.putString(AbstractYasmeActivity.USER_NAME, name);
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);
            editor.putLong(AbstractYasmeActivity.USER_ID, 0L);
            editor.commit();
        }

        ObservableRegistry.getObservable(classToNotify).notifyFragments(
                new RegisterFragment.RegistrationParam(success, email, password, message));

        SpinnerObservable.getInstance().removeBackgroundTask(this);
    }
}
