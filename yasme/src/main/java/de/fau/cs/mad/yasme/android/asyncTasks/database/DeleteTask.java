package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class DeleteTask<D extends Object, T extends DAO<D>> extends AsyncTask<String, Void, Boolean> {

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
    protected Boolean doInBackground(String... params) {
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
