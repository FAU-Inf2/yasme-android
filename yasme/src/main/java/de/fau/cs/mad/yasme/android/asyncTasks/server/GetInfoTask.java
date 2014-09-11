package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.connection.InfoTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.ServerInfo;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Martin Sturm <***REMOVED***> on 08.08.2014.
 */
public class GetInfoTask extends AsyncTask<Void, Void, Boolean> {

    public GetInfoTask(long interval) {
        this.interval = interval;
    }

    public GetInfoTask() {
        interval = INTERVAL;
    }

    private static final long INTERVAL = 86400 * 1000;
    //private static final long INTERVAL = 600 * 1000;
    private long interval;
    private ServerInfo serverInfo = null;

    @Override
    protected Boolean doInBackground(Void... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        Log.d(getClass().getSimpleName(), "ServerInfo will be fetched");
        long updateTime = DatabaseManager.INSTANCE.getServerInfoUpdateTime();
        if ((updateTime + interval) > System.currentTimeMillis()) {
            Log.d(getClass().getSimpleName(), "... up-to-date");
            return false;
        }
        DatabaseManager.INSTANCE.setServerInfoUpdateTime();

        InfoTask infoTask = InfoTask.getInstance();
        try {
            serverInfo = infoTask.getInfo();
            return true;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "...failed");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            return;
        }
        Log.d(getClass().getSimpleName(), "... finished");
        DatabaseManager.INSTANCE.setServerInfo(serverInfo);

        if (serverInfo != null && serverInfo.hasMessage()) {
            Log.d(getClass().getSimpleName(), "has Message:" + serverInfo.getMessage());
            Log.d(getClass().getSimpleName(), "Login:" + serverInfo.getLoginAllowed());
            Toaster.getInstance().toast(serverInfo.getMessage(), Toast.LENGTH_LONG);
        }
    }
}