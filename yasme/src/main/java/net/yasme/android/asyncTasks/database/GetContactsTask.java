package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.UserDAO;
import net.yasme.android.ui.fragments.ContactListItemFragment;
import net.yasme.android.ui.fragments.InviteToChatFragment;

import java.util.List;

/**
 * Created by robert on 29.07.14.
 */
public class GetContactsTask extends AsyncTask<Void, Void, Boolean> {
    private List<User> contacts;
    private UserDAO userDao;

    public GetContactsTask() {
        userDao = DatabaseManager.INSTANCE.getUserDAO();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        contacts = userDao.getContacts();
        if(contacts == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Notify
            ObservableRegistry.getObservable(InviteToChatFragment.class).
                    notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            ObservableRegistry.getObservable(ContactListItemFragment.class).
                    notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
        } else {
            Log.w(this.getClass().getSimpleName(), "Get contacts not successful");
        }
    }
}
