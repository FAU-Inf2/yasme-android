package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatFragment;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

//import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by Tim on 03.09.14.
 */

public class GetUserTask extends AsyncTask<Object, Void, Boolean> {

    private Class classToNotify = null;
    private Long id = null;

    public GetUserTask(Class classToNotify, Long id) {
        this.classToNotify = classToNotify;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
//		lastMessageId = DatabaseManager.INSTANCE.getSharedPreferences().getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);

        User dbUser = DatabaseManager.INSTANCE.getUserDAO().get(id);
        Log.d("OOOOOOOOOOOOO", "User id in doInBackground: " + dbUser.getId() + " with name " + dbUser.getName());
//		DatabaseManager.INSTANCE.getUserDAO().update(user);
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (!success) {
            Log.e(this.getClass().getSimpleName(), "No success");
            SpinnerObservable.getInstance().removeBackgroundTask(this);
            return;
        }

        Log.i(this.getClass().getSimpleName(), "UpdateDB successful, User stored");
/*				SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
                editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
				editor.commit();
*/
        if (classToNotify == ChatFragment.class) {
            ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(null);
        } else if (classToNotify == ChatListFragment.class) {
            ObservableRegistry.getObservable(ChatListFragment.class).notifyFragments(null);
        }
        SpinnerObservable.getInstance().removeBackgroundTask(this);
    }
}
