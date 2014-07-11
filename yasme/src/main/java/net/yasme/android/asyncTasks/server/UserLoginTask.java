package net.yasme.android.asyncTasks.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.connection.AuthorizationTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.LoginFragment;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous login task used to authenticate the user.
 */
public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    SharedPreferences storage;
    Context context;

    public UserLoginTask(SharedPreferences storage, Context context) {
        this.storage = storage;
        this.context = context;
    }

    long lastMessageId;
    String accessToken;
    long userId;

    /**
     *
     * @param params
     *          0 is email
     *          1 is password
     * @return
     */
    protected Boolean doInBackground(String... params) {
        String email = params[0];
        String password = params[1];
        try {
            // DEBUG:
            System.out.println("e-Mail: " + email + " " + "Passwort: "
                    + password);

            String loginReturn[] = AuthorizationTask.getInstance().loginUser(new User(email,
                    password));

            System.out.println("LoginReturn:");
            userId = Long.parseLong(loginReturn[0]);
            accessToken = loginReturn[1];

            System.out.println(loginReturn[0]);

            // storage
            SharedPreferences.Editor editor = storage.edit();
            editor.putLong(AbstractYasmeActivity.USER_ID, userId);
            editor.putString(AbstractYasmeActivity.ACCESSTOKEN, accessToken);
            editor.putString(AbstractYasmeActivity.USER_MAIL, email);

            lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

            editor.commit();

        } catch (RestServiceException e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        ObservableRegistry.getObservable(LoginFragment.class).notifyFragments(new LoginFragment.LoginProcessParam(success, userId, accessToken));
    }
}
