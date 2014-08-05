package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class GetTask<D extends Object, T extends DAO<D>> extends AsyncTask<Void, Void, Boolean> {

    private T specificDAO;
    private long idToGet;
    private D data;
    private Class classToNotify;

    public GetTask(T specificDAO, long idToGet, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.idToGet = idToGet;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        return null != (data = specificDAO.get(idToGet));
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            ObservableRegistry.getObservable(classToNotify).notifyFragments(data);
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
