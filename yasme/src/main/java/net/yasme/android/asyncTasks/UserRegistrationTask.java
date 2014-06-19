package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import net.yasme.android.Constants;
import net.yasme.android.R;
import net.yasme.android.YasmeLogin;
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

/**
 * Created by robert on 19.06.14.
 */
/**
 * Represents an asynchronous task used to register the user.
 */
public class UserRegistrationTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    SharedPreferences storage;
    YasmeLogin activity;

    public UserRegistrationTask(Context context, SharedPreferences storage, YasmeLogin activity) {
        this.context = context;
        this.storage = storage;
        this.activity = activity;
    }

    String name;
    String email;
    String password;

    /**
     * @params params[0] is name
     * @params params[1] is email
     * @params params[2] is password
     * @params params[3] is password_check
     */
    @Override
    protected Boolean doInBackground(String... params) {
        // TODO: ueberpruefen, ob user schon existiert
        name = params[0];
        email = params[1];
        password = params[2];
        String password_check = params[3];

        if (!password.equals(password_check)) {
            return false;
        }
        try {
            long userId = UserTask.getInstance().registerUser(new User(password, name,
                    email));
        } catch (RestServiceException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success) {
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(Constants.USER_NAME, name);
            editor.putLong(Constants.LAST_MESSAGE_ID, 0L);
            editor.commit();

            //Login after registration was successfull
            UserLoginTask authTask = new UserLoginTask(context, storage, activity);
            authTask.execute(email, password);
        }
        activity.onPostRegisterExecute(success, email, password);
    }

    @Override
    protected void onCancelled() {
        activity.showProgress(false);
    }
}