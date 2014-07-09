package net.yasme.android.asyncTasks;

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

    protected InviteToChatFragment fragment;

    protected SearchTask searchTask = SearchTask.getInstance();
    private List<User> allUsers;

    public GetAllUsersTask(InviteToChatFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            allUsers = searchTask.getAllUsers();
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
