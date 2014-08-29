package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;

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
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 29.07.14.
 */
public class GetContactsTask extends AsyncTask<String, Void, Boolean> {
    private List<User> contacts;
    private UserDAO userDao = DatabaseManager.INSTANCE.getUserDAO();
    private Class classToNotify;

    public GetContactsTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(String... params) {
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
            if(classToNotify == InviteToChatFragment.class) {
                ObservableRegistry.getObservable(InviteToChatFragment.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            } else if(classToNotify == ContactListFragment.class) {
                ObservableRegistry.getObservable(ContactListFragment.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            } else if(classToNotify == ChatSettingsAdd.class) {
                ObservableRegistry.getObservable(ChatSettingsAdd.class).
                        notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
            }
        } else {
            Log.w(this.getClass().getSimpleName(), "Get contacts not successful");
        }
    }
}
