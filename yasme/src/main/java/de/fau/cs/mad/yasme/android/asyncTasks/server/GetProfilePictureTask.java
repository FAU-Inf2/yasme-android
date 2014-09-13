package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.IOException;

import de.fau.cs.mad.yasme.android.connection.UserTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.UserAdapter;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 09.07.14.
 */
public class GetProfilePictureTask extends AsyncTask<Long, Void, Boolean> {

    private BitmapDrawable profilePicture;
    private Class classToNotify;

    public GetProfilePictureTask(Class classToNotify) {
        this.classToNotify = classToNotify;
    }

    /**
     * @param params 0 is userId
     * @return true on success, false on error
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        long userId = params[0];

        try {
            profilePicture = UserTask.getInstance().getProfilePicture(userId);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        if (profilePicture == null) {
            Log.e(this.getClass().getSimpleName(), "profilePicture was null");
            return false;
        }

        //store the picture
        if (userId == DatabaseManager.INSTANCE.getUserId()) {
            String path;
            SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
            try {
                User self = new User();
                self.setId(DatabaseManager.INSTANCE.getUserId());
                path = PictureManager.INSTANCE.storePicture(self, profilePicture.getBitmap());
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
            editor.putString(AbstractYasmeActivity.PROFILE_PICTURE, path);
            editor.commit();
        } else {
            User user = DatabaseManager.INSTANCE.getUserDAO().get(userId);
            if (user == null) {
                return false;
            }
            String path;
            try {
                path = PictureManager.INSTANCE.storePicture(user, profilePicture.getBitmap());
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
            if (path != null && !path.isEmpty()) {
                user.setProfilePicture(path);
                DatabaseManager.INSTANCE.getUserDAO().update(user);
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            if (classToNotify == UserAdapter.class) {
                //TODO Adapter benachrichtigen, dass Bild da ist
                return;
            }
            // Notify registered fragments
            //FragmentObservable<OwnProfileFragment, Drawable> obs =
            //        ObservableRegistry.getObservable(classToNotify);
            //obs.notifyFragments(profilePicture);
        }
    }
}
