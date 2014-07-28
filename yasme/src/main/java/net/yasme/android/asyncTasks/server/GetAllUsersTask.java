package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.SearchTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.fragments.InviteToChatFragment;

import java.util.List;

/**
 * Created by bene on 20.06.14.
 */
public class GetAllUsersTask extends AsyncTask<String, Void, Boolean> {

    protected SearchTask searchTask = SearchTask.getInstance();
    private List<User> allUsers;
    private Class classToNotify;

    public GetAllUsersTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            allUsers = searchTask.getAllUsers();
            if (null == allUsers) {
                return false;
            }
            return true;
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * Fills the ListView with the users,
     */
    protected void onPostExecute(final Boolean success) {
        if (success) {
            // InviteToChatFragment.ContactsFetchedParam is also used by ContactListContentFragment
            ObservableRegistry.getObservable(classToNotify).notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(true, allUsers));
        }
    }
}
