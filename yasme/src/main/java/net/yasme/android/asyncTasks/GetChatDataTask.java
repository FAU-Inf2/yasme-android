package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.yasme.android.R;
import net.yasme.android.YasmeHome;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
public class GetChatDataTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeHome activity;

    public GetChatDataTask(Context context, YasmeHome activity) {
        this.context = context;
        this.activity = activity;
    }

    ArrayList<Chat> chatrooms = null;

    protected Boolean doInBackground(String... params) {
        chatrooms = DatabaseManager.getInstance().getAllChats();
        return chatrooms != null;
    }

    protected void onPostExecute(final Boolean success) {
            /*
            if(success) {
                //TODO: Debug
                System.out.println("Fehler bei Datenbankzugriff");
                return;
            }

            ListView list = (ListView) findViewById(R.id.chatroom_list);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            //for (Chat chat : chatrooms) {
            for (int i=0; i < 10; i++) {
                TextView name = new TextView((getApplicationContext()));
                TextView status = new TextView((getApplicationContext()));

                RelativeLayout row = new RelativeLayout(getApplicationContext());
                row.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

               // name.setText(chat.getName());
                //status.setText(chat.getStatus());
                name.setText("Name: " + String.valueOf(i));
                status.setText("Status: " + String.valueOf(i));

                row.setOnClickListener(chatClickListener);

                row.addView(name);
                //row.addView(status);
                list.addView(row, layoutParams);
            }
            */

        ArrayList<String> vals = new ArrayList<>();
        for (int i = 1; i <= 15; i++)
        {
            vals.add(String.valueOf(i));
        }
        ListAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, vals);
        final ListView list = (ListView)activity.findViewById(R.id.chatroom_list);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Long chatId = Long.parseLong(list.getAdapter().getItem(position).toString());
                activity.showChat(chatId);
            }
        });

    }
}