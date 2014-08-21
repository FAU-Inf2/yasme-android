package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by robert on 03.08.14.
 */
public class ChangeOwnerTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;
    private Context mContext = DatabaseManager.INSTANCE.getContext();

    public ChangeOwnerTask(Chat chat) {
        this.chat = chat;
    }

    /**
     * @return Returns true if it was successful, otherwise false
     * @param params
     *              0 is id of new owner
     */
    @Override
    protected Boolean doInBackground(Long... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
        try {
            ChatTask.getInstance().changeOwnerOfChat(chat.getId(), params[0]);
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if(success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
            //LeaveChatTask task = new LeaveChatTask(chat);
            //LeaveChatTask.preExecute(mContext, task);

            AlertDialog alert = new AlertDialog.Builder(mContext).create();
            alert.setTitle(mContext.getString(R.string.alert_leave));
            alert.setMessage(mContext.getString(R.string.alert_leave_message));

            alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // This can fail with IllegalStateException: the task has already been executed (a task can be executed only once)
                            new LeaveChatTask(chat).execute();
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
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
