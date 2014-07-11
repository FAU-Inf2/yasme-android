package net.yasme.android.asyncTasks.database;

import android.os.AsyncTask;

import net.yasme.android.storage.dao.DAO;

/**
 * Created by bene on 11.07.14.
 */
public class AddTask<D extends Object, T extends DAO<D>> extends AsyncTask<Void, Void, Boolean> {

    private T specificDAO;
    private D data;

    public AddTask(T specificDAO, D data) {
        this.specificDAO = specificDAO;
        this.data = data;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return null != specificDAO.add(data);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
