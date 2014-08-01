package net.yasme.android.asyncTasks.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.AuthorizationTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.PasswordEncryption;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.fragments.LoginFragment;
import net.yasme.android.ui.fragments.RegisterFragment;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous login task used to authenticate the user.
 */
public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    private SharedPreferences storage;
    private String accessToken;
    private Boolean plainPassword = false;
    private long userId;

    public UserLoginTask(Boolean plainPassword) {
        this.plainPassword = plainPassword;
    }

    /**
     *
     * @param params
     *          0 is email
     *          1 is password
     * @return
     */
    protected Boolean doInBackground(String... params) {
        String email = params[0].toLowerCase();
        String password = params[1];
        try {
            // DEBUG:
            Log.d(this.getClass().getSimpleName(),"e-Mail: " + email + " " + "Passwort: "
                    + password);

            if (plainPassword) {
                PasswordEncryption pwEnc = new PasswordEncryption(new User(email, password));
                password = pwEnc.getSecurePassword();
            }

            String loginReturn[] = AuthorizationTask.getInstance().loginUser(new User(email,
                    password));

            Log.d(this.getClass().getSimpleName(),"LoginReturn:");
            userId = Long.parseLong(loginReturn[0]);
            accessToken = loginReturn[1];

            Log.d(this.getClass().getSimpleName(), loginReturn[0]);

            // storage
            SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
            editor.putLong(AbstractYasmeActivity.USER_ID, userId);
            editor.putString(AbstractYasmeActivity.ACCESSTOKEN, accessToken);
            editor.putString(AbstractYasmeActivity.USER_MAIL, email);
            editor.commit();

        } catch (RestServiceException e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        ObservableRegistry.getObservable(LoginFragment.class).notifyFragments(
                new LoginFragment.LoginProcessParam(success, userId));
        ObservableRegistry.getObservable(RegisterFragment.class).notifyFragments(
                new RegisterFragment.RegLoginParam(success, userId));
    }
}
