package net.yasme.android.asyncTasks.server;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.connection.InfoTask;
import net.yasme.android.connection.UserTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.ServerInfo;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.fragments.ChatListFragment;
import net.yasme.android.ui.fragments.OwnProfileFragment;

/**
 * Created by martin on 08.08.2014.
 */
public class GetInfoTask extends AsyncTask<Void, Void, Boolean> {

    public GetInfoTask(long interval) {
        this.interval = interval;
    }

    public GetInfoTask() {
        interval = INTERVAL;
    }

    //private static final long INTERVAL = 86400 * 1000;
    private static final long INTERVAL = 600 * 1000;
    private long interval;
    private ServerInfo serverInfo = null;

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(getClass().getSimpleName(),"ServerInfo will be fetched");
       long updateTime = DatabaseManager.INSTANCE.getServerInfoUpdateTime();
       if ((updateTime + interval) > System.currentTimeMillis()) {
           Log.d(getClass().getSimpleName(),"... up-to-date");
           return false;
       }
        DatabaseManager.INSTANCE.setServerInfoUpdateTime();

        InfoTask infoTask = InfoTask.getInstance();
        try {
            serverInfo = infoTask.getInfo();
            return true;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(),"...failed");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (!success) {
            return;
        }
        Log.d(getClass().getSimpleName(),"... finished");
        DatabaseManager.INSTANCE.setServerInfo(serverInfo);

        if (serverInfo != null && serverInfo.hasMessage()) {
            Log.d(getClass().getSimpleName(),"has Message:" + serverInfo.getMessage());
            Toaster.getInstance().toast(serverInfo.getMessage(), Toast.LENGTH_LONG);
        }
    }
}