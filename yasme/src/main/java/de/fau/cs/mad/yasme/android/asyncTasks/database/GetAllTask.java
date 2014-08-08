package de.fau.cs.mad.yasme.android.asyncTasks.database;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class GetAllTask<D extends Object, T extends DAO<D>> extends AsyncTask<String, Void, Boolean> {

    private T specificDAO;
    private List<D> data;
    private Class classToNotify;
    private String fragmentToNotify;

    public GetAllTask(T specificDAO, Class classToNotify) {
        this.specificDAO = specificDAO;
        this.classToNotify = classToNotify;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        fragmentToNotify = params[0];
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        return null != (data = specificDAO.getAll());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (success) {
            // Notify
            if(fragmentToNotify.compareTo(classToNotify.getName()) == 0) {
                ObservableRegistry.getObservable(classToNotify).notifyFragments(data);
            }
        }
        else {
            Log.w(this.getClass().getSimpleName(), "Did not invoke notification as task did not finish successfully.");
        }
    }
}
