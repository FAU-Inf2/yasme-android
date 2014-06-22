package net.yasme.android.asyncTasks;

/**
 * Created by robert on 19.06.14.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatListActivity;
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.ChatListFragment;


public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    ChatListFragment activity;
    SharedPreferences storage;

    public GetProfileDataTask(Context context, ChatListFragment activity, SharedPreferences storage) {
        this.context = context;
        this.activity = activity;
        this.storage = storage;
    }

    User selfProfile;
    String userMail;
    protected Boolean doInBackground(String... params) {
        long user_id = Long.parseLong(params[0]);
        String accessToken = params[1];
        userMail = params[2];
        try {
            selfProfile = UserTask.getInstance().getUserData(user_id, accessToken);
        } catch (RestServiceException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return selfProfile != null;
    }

    protected void onPostExecute(final Boolean success) {
        if(!success) {
            return;
        }
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(AbstractYasmeActivity.USER_NAME, selfProfile.getName());
        editor.commit();
    }
}
