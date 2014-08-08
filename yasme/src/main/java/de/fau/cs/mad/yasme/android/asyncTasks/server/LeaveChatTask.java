package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

import java.lang.ref.WeakReference;

/**
 * Created by robert on 02.08.14.
 */
public class LeaveChatTask extends AsyncTask<Long, Void, Boolean> {
    private final WeakReference<Activity> activityWeakReference;
    private Chat chat;
    private boolean isOwner = false;
    private Context context = DatabaseManager.INSTANCE.getContext();

    public LeaveChatTask(Chat chat, Activity activity) {
        this.chat = chat;
        this.activityWeakReference = new WeakReference<>(activity);
    }


    @Override
    public void onPreExecute() {
        super.onPreExecute();

        if (activityWeakReference.get() != null && !activityWeakReference.get().isFinishing()) {
            AlertDialog alert = new AlertDialog.Builder(activityWeakReference.get()).create();
            alert.setTitle(context.getString(R.string.alert_leave));
            alert.setMessage(context.getString(R.string.alert_leave_message));

            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // This can fail with IllegalStateException: the task has already been executed (a task can be executed only once)
                            execute();
                            dialog.dismiss();
                        }
            });

            alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alert.show();

        } else {
            Log.w(this.getClass().getSimpleName(), "Activity has finished before message could be shown.");
        }
    }

    /**
     * @return true an success, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        if(chat.getOwner().getId() == DatabaseManager.INSTANCE.getUserId()) {
            isOwner = true;
            return false;
        }
        try {
            ChatTask.getInstance().removeOneSelfFromChat(chat.getId());
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if(!success) {
            if (isOwner) {
                new ChangeOwnerTask(chat).onPreExecute();
            }
        }
    }
}