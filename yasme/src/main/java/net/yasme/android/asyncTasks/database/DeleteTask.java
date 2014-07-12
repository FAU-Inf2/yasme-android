package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class DeleteTask<D extends Object, T extends DAO<D>> extends AsyncTask<Void, Void, Boolean> {

    private T specificDAO;
    private D objectToDelete;
    private Class classToNotify;

    public DeleteTask(T specificDAO, D objectToDelete, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.objectToDelete = objectToDelete;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return specificDAO.delete(objectToDelete);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Notify
            ObservableRegistry.getObservable(classToNotify).notifyFragments(success);
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
