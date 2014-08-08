package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsAdd;
import de.fau.cs.mad.yasme.android.ui.fragments.ContactListFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.InviteToChatFragment;

/**
 * Created by robert on 29.07.14.
 */
public class GetContactsTask extends AsyncTask<String, Void, Boolean> {
    private List<User> contacts;
    private UserDAO userDao = DatabaseManager.INSTANCE.getUserDAO();
    private String fragmentToNotify;

    @Override
    protected Boolean doInBackground(String... params) {
        fragmentToNotify = params[0];
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        contacts = userDao.getContacts();
        if(contacts == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            if(fragmentToNotify.compareTo(InviteToChatFragment.class.getName()) == 0) {
                ObservableRegistry.getObservable(InviteToChatFragment.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            } else if(fragmentToNotify.compareTo(ContactListFragment.class.getName()) == 0) {
                ObservableRegistry.getObservable(ContactListFragment.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            } else if(fragmentToNotify.compareTo(ChatSettingsAdd.class.getName()) == 0) {
                ObservableRegistry.getObservable(ChatSettingsAdd.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            }
        } else {
            Log.w(this.getClass().getSimpleName(), "Get contacts not successful");
        }
    }
}
