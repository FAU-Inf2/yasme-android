package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.storage.dao.DAO;

import java.util.List;

/**
 * Created by bene on 11.07.14.
 */
public class GetAllTask<D extends Object, T extends DAO<D>> extends AsyncTask<Void, Void, Boolean> {

    private T specificDAO;
    private List<D> data;
    private Class classToNotify;

    public GetAllTask(T specificDAO, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return null != (data = specificDAO.getAll());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Notify
            ObservableRegistry.getObservable(classToNotify).notifyFragments(data);
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
