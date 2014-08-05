package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class DeleteTask<D extends Object, T extends DAO<D>> extends AsyncTask<Void, Void, Boolean> {

    private T specificDAO;
    private D objectToDelete;
    private Class classToNotify;

    public DeleteTask(T specificDAO, D objectToDelete) {
        this.specificDAO = specificDAO;
        this.objectToDelete = objectToDelete;
    }

    public DeleteTask(T specificDAO, D objectToDelete, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.objectToDelete = objectToDelete;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        return specificDAO.delete(objectToDelete);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            if (null != classToNotify) {
                ObservableRegistry.getObservable(classToNotify).notifyFragments(success);
            }
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
