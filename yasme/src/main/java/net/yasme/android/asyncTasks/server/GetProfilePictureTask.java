package net.yasme.android.asyncTasks.server;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.UserTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.ChatListFragment;
import net.yasme.android.ui.ContactActivity;
import net.yasme.android.ui.OwnProfileFragment;
import net.yasme.android.ui.UserDetailsFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bene on 09.07.14.
 */
public class GetProfilePictureTask extends AsyncTask<String, Void, Boolean> {

        private DatabaseManager databaseManager = DatabaseManager.INSTANCE;
        private Drawable profilePicture;


        @Override
        protected Boolean doInBackground(String... params) {
            long userId = Long.parseLong(params[0]);

            try {
                profilePicture = UserTask.getInstance().getProfilePicture(userId);
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success && null != profilePicture) {
                // Notify registered fragments
                FragmentObservable<OwnProfileFragment, Drawable> obs = ObservableRegistry.getObservable(ChatListFragment.class);
                obs.notifyFragments(profilePicture);
            }
        }
}