package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;

import java.util.List;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatSettingsAdd;
import de.fau.cs.mad.yasme.android.ui.fragments.ContactListFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.InviteToChatFragment;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 29.07.14.
 */
public class GetContactsTask extends AsyncTask<String, Void, Boolean> {
    private List<User> contacts;
    private Class classToNotify;

    public GetContactsTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        contacts = DatabaseManager.INSTANCE.getUserDAO().getContacts();
        if (contacts == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        Log.d(this.getClass().getSimpleName(), "onPostExecute");
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "Get contacts not successful");
            return;
        }

        // Notify
        if (classToNotify != InviteToChatFragment.class && classToNotify != ContactListFragment.class && classToNotify != ChatSettingsAdd.class) {
            Log.e(this.getClass().getSimpleName(), "classToNotify was not any listed known class.");
            return;
        }

        ObservableRegistry.getObservable(classToNotify).
                notifyFragments(new InviteToChatFragment.AllUsersFetchedParam(success, contacts));
    }
}
