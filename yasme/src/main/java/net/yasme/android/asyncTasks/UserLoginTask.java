package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.LoginActivity;
import net.yasme.android.connection.AuthorizationTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous login task used to authenticate the user.
 */
public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    SharedPreferences storage;
    LoginActivity activity;

    public UserLoginTask(Context context, SharedPreferences storage, LoginActivity activity) {
        this.context = context;
        this.storage = storage;
        this.activity = activity;
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

            //initConnection Session
            ConnectionTask.initSession(userId,accessToken);

            System.out.println(loginReturn[0]);

            // storage
            SharedPreferences.Editor editor = storage.edit();
            editor.putLong(AbstractYasmeActivity.USER_ID, userId);
            editor.putString(AbstractYasmeActivity.ACCESSTOKEN, accessToken);
            editor.putString(AbstractYasmeActivity.USER_MAIL, email);

            lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

            //editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

            editor.commit();

            //Initialize database (once in application)
            if (!DatabaseManager.isInitialized()) {
                DatabaseManager.init(context, userId, accessToken);
            }

        } catch (RestServiceException e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success) {
            new UpdateDBTask(context, null)
                    .execute(Long.toString(lastMessageId), Long.toString(userId), accessToken);
        }
        activity.onPostLoginExecute(success, userId, accessToken);
    }

    @Override
    protected void onCancelled() {
        activity.showProgress(false);
    }
}
