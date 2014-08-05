package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class AddOrUpdateTask<D extends Object, T extends DAO<D>> extends AsyncTask<Object, Void, Boolean> {

    private T specificDAO;
    private D data;
    private Class classToNotify;

    public AddOrUpdateTask(T specificDAO, D data) {
        this.specificDAO = specificDAO;
        this.data = data;
    }

    public AddOrUpdateTask(T specificDAO, D data, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.data = data;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        return null != (data = specificDAO.addOrUpdate(data));
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
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
