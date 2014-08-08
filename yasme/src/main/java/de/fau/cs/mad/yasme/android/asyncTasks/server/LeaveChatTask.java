package de.fau.cs.mad.yasme.android.asyncTasks.server;

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

/**
 * Created by robert on 02.08.14.
 */
public class LeaveChatTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;
    private boolean isOwner = false;

    public LeaveChatTask(Chat chat) {
        this.chat = chat;
    }


    public static void preExecute(Context mContext, final LeaveChatTask task) {
        AlertDialog alert = new AlertDialog.Builder(mContext).create();
        alert.setTitle(mContext.getString(R.string.alert_leave));
        alert.setMessage(mContext.getString(R.string.alert_leave_message));

        alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // This can fail with IllegalStateException: the task has already been executed (a task can be executed only once)
                        task.execute();
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
    }

    /**
     * @return true an success, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        if (chat.getOwner().getId() == DatabaseManager.INSTANCE.getUserId()) {
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
        if (!success) {
            if (isOwner) {
                ChangeOwnerTask task = new ChangeOwnerTask(chat);
                ChangeOwnerTask.preExecute(DatabaseManager.INSTANCE.getContext(), task, chat);
            }
        }
    }
}