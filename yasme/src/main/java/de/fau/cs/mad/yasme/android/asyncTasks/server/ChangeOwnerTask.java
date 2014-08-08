package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
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

    public static void preExecute(Context mContext, final ChangeOwnerTask task, final Chat chat) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getString(R.string.alert_owner));

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        TextView text = new TextView(mContext);
        text.setText(mContext.getString(R.string.alert_owner_message));

        final ListView list = new ListView(mContext);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        List<String> participantNames = new ArrayList<>();
        for(User u : chat.getParticipants()) {
            if(u.getId() == DatabaseManager.INSTANCE.getUserId()) {
                continue;
            }
            participantNames.add(u.getName());
        }
        final ArrayAdapter<List<User>> adapter = new ArrayAdapter<List<User>>(mContext,
                android.R.layout.simple_list_item_single_choice, (List)participantNames);
        list.setAdapter(adapter);

        layout.addView(text, layoutParams);
        layout.addView(list, layoutParams);
        alert.setView(layout);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int position = list.getCheckedItemPosition();
                        if(position != AdapterView.INVALID_POSITION) {
                            Long newUserId = chat.getParticipants().
                                    get(position).getId();
                            task.execute(newUserId);
                            dialog.dismiss();
                        }
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
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if(success) {
            Toaster.getInstance().toast(R.string.change_successful, Toast.LENGTH_LONG);
            LeaveChatTask task = new LeaveChatTask(chat);
            LeaveChatTask.preExecute(mContext, task);
        } else {
            Toaster.getInstance().toast(R.string.change_not_successful, Toast.LENGTH_LONG);
        }
    }
}
