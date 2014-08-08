package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.connection.AuthorizationTask;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.LoginFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.RegisterFragment;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous login task used to authenticate the user.
 */
public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    private Boolean plainPassword = false;
    private String email;
    private String password;
    private String fragmentToNotify;

    public UserLoginTask(Boolean plainPassword) {
        this.plainPassword = plainPassword;
    }

    /**
     *
     * @param params
     *          0 is email
     *          1 is password
     *          2 is fragmentToNotify
     * @return
     */
    protected Boolean doInBackground(String... params) {
        fragmentToNotify = params[2];
        GetInfoTask getInfoTask = new GetInfoTask(0);
        getInfoTask.execute();
//        try {
//            getInfoTask.wait();
//        } catch (Exception e) { }


        email = params[0].toLowerCase();
        password = params[1];
        try {
            // DEBUG:
            Log.d(this.getClass().getSimpleName(),"e-Mail: " + email + " " + "Passwort: "
                    + password);

            if (plainPassword) {
                PasswordEncryption pwEnc = new PasswordEncryption(new User(email, password));
                password = pwEnc.getSecurePassword();
            }

            String loginReturn[] = AuthorizationTask.getInstance().loginUser(new User(email, password));
        } catch (RestServiceException e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            // Store email address for later use
            DatabaseManager.INSTANCE.setUserEmail(email);
        }

        if(fragmentToNotify.compareTo(LoginFragment.class.getName()) == 0) {
            ObservableRegistry.getObservable(LoginFragment.class).notifyFragments(
                    new LoginFragment.LoginProcessParam(success));
        } else if(fragmentToNotify.compareTo(RegisterFragment.class.getName()) == 0) {
            ObservableRegistry.getObservable(RegisterFragment.class).notifyFragments(
                    new RegisterFragment.RegLoginParam(success));
        }
    }
}
