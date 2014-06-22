package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import net.yasme.android.ui.InviteToChatActivity;
import net.yasme.android.connection.SearchTask;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.InviteToChatFragment;

import java.util.List;

/**
 * Created by bene on 20.06.14.
 */
public class GetAllUsersTask extends AsyncTask<String, Void, Boolean> {

    protected Context context;
    protected InviteToChatFragment fragment;

    protected SearchTask searchTask = SearchTask.getInstance();
    private long userId;
    private String accessToken;
    private List<User> allUsers;

    public GetAllUsersTask(Context context, InviteToChatFragment fragment) {
        this.context = context;
        this.fragment = fragment;
    }

    /**
     * @param params
     *              0 is userId
     *              1 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        userId = Long.parseLong(params[0]);
        accessToken = params[1];
        try {
            allUsers = searchTask.getAllUsers(userId, accessToken);
        } catch (RestServiceException e) {
            // TODO
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Fills the ListView with the users,
     */
    protected void onPostExecute(final Boolean success) {
        if (success) {
            fragment.updateChatPartnersList(allUsers);
        }
    }
}
