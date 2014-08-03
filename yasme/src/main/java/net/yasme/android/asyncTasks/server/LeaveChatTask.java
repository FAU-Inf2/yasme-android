package net.yasme.android.asyncTasks.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

/**
 * Created by robert on 02.08.14.
 */
public class LeaveChatTask extends AsyncTask<Long, Void, Boolean> {
    private Chat chat;
    private boolean isOwner = false;
    private Context context = DatabaseManager.INSTANCE.getContext();

    public LeaveChatTask(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.alert_leave));

        TextView text = new TextView(context);
        text.setText(context.getString(R.string.alert_leave_message));

        alert.setView(text);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        execute();
                    }
                }
        );
        // "Cancel" button
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }

    /**
     * @return true an success, otherwise false
     */
    @Override
    protected Boolean doInBackground(Long... params) {
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
        if(!success) {
            if (isOwner) {
                new ChangeOwnerTask(chat).onPreExecute();
            }
        }
    }
}