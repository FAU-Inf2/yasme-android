package net.yasme.android.asyncTasks;

/**
 * Created by robert on 19.06.14.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.YasmeChats;
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;


public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeChats activity;

    public GetProfileDataTask(Context context, YasmeChats activity) {
        this.context = context;
        this.activity = activity;
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
        activity.getSelf().setName(selfProfile.getName());
        TextView profileInfo = (TextView) activity.findViewById(R.id.profileInfo);
        profileInfo.setText(selfProfile.getName() + ": " + userMail);
    }
}
