package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class UpdateTask<D extends Object, T extends DAO<D>> extends AsyncTask<Object, Void, Boolean> {

    private T specificDAO;
    private D data;
    private Class classToNotify;

    public UpdateTask(T specificDAO, D data) {
        this.specificDAO = specificDAO;
        this.data = data;
    }

    public UpdateTask(T specificDAO, D data, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.data = data;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        return null != (data = specificDAO.update(data));
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Notify
            if (null != classToNotify) {
                ObservableRegistry.getObservable(classToNotify).notifyFragments(data);
            }
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
