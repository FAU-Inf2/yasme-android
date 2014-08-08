package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.RegisterFragment;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous task used to register the user.
 */
public class UserRegistrationTask extends AsyncTask<String, Void, Boolean> {

    private String name;
    private String email;
    private String password;
    private String fragmentToNotify;
    private long userId;


    /**
     * @params params[0] is name
     * @params params[1] is email
     * @params params[2] is password
     * @params params[3] is password_check
     * @params params[4] is fragmentToNotify
     */
    @Override
    protected Boolean doInBackground(String... params) {
        // TODO: ueberpruefen, ob user schon existiert
        name = params[0];
        email = params[1].toLowerCase();
        password = params[2];
        String password_check = params[3];
        fragmentToNotify = params[4];

        if (!password.equals(password_check)) {
            return false;
        }
        try {
            PasswordEncryption pwEnc = new PasswordEncryption(new User(email,password));
            password = pwEnc.getSecurePassword();
            userId = UserTask.getInstance().registerUser(new User(password, name, email));
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        new GetInfoTask().execute();
        if(success) {
            SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
            // Will be done by login task
            //editor.putLong(AbstractYasmeActivity.USER_ID, userId);
            editor.putString(AbstractYasmeActivity.USER_NAME, name);
            editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);
            editor.commit();
        }
        if(fragmentToNotify.compareTo(RegisterFragment.class.getSimpleName()) == 0) {
            ObservableRegistry.getObservable(RegisterFragment.class).notifyFragments(
                    new RegisterFragment.RegistrationParam(success, email, password));
        }
    }
}
