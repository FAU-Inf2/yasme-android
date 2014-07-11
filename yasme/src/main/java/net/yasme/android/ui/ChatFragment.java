package net.yasme.android.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.GetMessageTask;
import net.yasme.android.asyncTasks.server.SendMessageTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;

import java.util.List;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatFragment extends Fragment implements NotifiableFragment<List<Message>> {

    private AbstractYasmeActivity activity;

    private SharedPreferences storage;

    //UI references
    private EditText editMessage;
    private TextView status;
    private LinearLayout layout;

    private Chat chat;

    MessageEncryption aes;

    public ChatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();

           Intent intent = activity.getIntent();
           long chatId = intent.getLongExtra(activity.CHAT_ID, 1);

           storage = activity.getStorage();

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatFragment, List<Message>> obs = ObservableRegistry.getObservable(ChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.INSTANCE.getChatDAO().get(chatId);
            Log.d(this.getClass().getSimpleName(), "number of messages from DB: " + chat.getMessages().size());
        } catch (NullPointerException e) {
            chat = null;
            Log.w(this.getClass().getSimpleName(), "get chat from DB failed");
        }
        if (chat == null) {
            chat = new Chat(chatId, activity.getSelfUser(), activity);
        }

        //DEBUG, TODO: encryption speichern und auslesen
        aes = new MessageEncryption(chat, activity.getSelfUser().getId());
        chat.setEncryption(aes);

        initializeViews();
        updateViews(chat.getMessages());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container,
                false);
        editMessage = (EditText) rootView.findViewById(R.id.text_message);
        status = (TextView) rootView.findViewById(R.id.text_status);
        layout = (LinearLayout) rootView.findViewById(R.id.scrollLayout);

        Button buttonSend = (Button) rootView.findViewById(R.id.button_send);
        Button buttonUpdate = (Button) rootView.findViewById(R.id.button_update);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(v);
            }
        });
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(v);
            }
        });

        return rootView;
    }


    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<ChatFragment, List<Message>> obs = ObservableRegistry.getObservable(ChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    @Override
    public void notifyFragment(List<Message> messages) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        updateViews(messages);
    }

    public TextView getStatus() {
        return status;
    }

    private void initializeViews() {
        status.setText("Eingeloggt: " +
                storage.getString(AbstractYasmeActivity.USER_NAME, "anonym"));
    }

    public void send(View view) {
        String msg = editMessage.getText().toString();
        if (msg.isEmpty()) {
            status.setText("Nichts eingegeben");
            return;
        }
        //String msgEncrypted = aes.encrypt(editMessage.getText().toString());

        new SendMessageTask((ChatActivity) activity, this, chat.getEncryption())
                .execute(msg, activity.getSelfUser().getName(), activity.getSelfUser().getEmail(), Long.toString(activity.getSelfUser().getId()),
                        Long.toString(chat.getId()), activity.getAccessToken());
        editMessage.setText("");
        update(view);
    }

    public void asyncUpdate() {
        status.setText("GET messages");
        //TODO: folgenden Aufruf loeschen
        //new GetMessageTaskInChat(this, chat.getEncryption(), storage)
        //        .execute(Long.toString(activity.getSelfUser().getId()), activity.getAccessToken());
        new GetMessageTask(storage)
                .execute(Long.toString(activity.getSelfUser().getId()), activity.getAccessToken());
        status.setText("GET messages done");
    }

    public void update(View view) {
        asyncUpdate();
    }


    public void showMessage(Message msg) {
        TextView textView = new TextView(activity.getApplicationContext());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout row = new RelativeLayout(activity.getApplicationContext());
        row.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        //textView.setText(msg.getSender().getName() + ": "+ msg.getMessage());

        String name;
        try {
            name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
            name = "anonym";
        }
        textView.setText(name + ": " + msg.getMessage());

        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
        textView.setTextColor(getResources().getColor(R.color.chat_text_color_other));

        if (msg.getSender().getId() == activity.getSelfUser().getId()) {
            textView.setGravity(Gravity.RIGHT);
            row.setGravity(Gravity.RIGHT);
            textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_self));
            textView.setTextColor(getResources().getColor(R.color.chat_text_color_self));
        }
        row.addView(textView);
        layout.addView(row, layoutParams);

        row.setFocusableInTouchMode(true);
        row.requestFocus();
        editMessage.requestFocus();
    }


    public void updateViews(List<Message> messages) {
        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "Keine Nachrichten zum Ausgeben");
        }


        for (Message msg : messages) {
            msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getMessageKeyId())));
            TextView textView = new TextView(activity.getApplicationContext());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            RelativeLayout row = new RelativeLayout(activity.getApplicationContext());
            row.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            //textView.setText(msg.getSender().getName() + ": "+ msg.getMessage());

            String name;
            try {
                name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
            } catch (NullPointerException e) {
                Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
                name = "anonym";
            }
            textView.setText(name + ": " + msg.getMessage());

            textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
            textView.setTextColor(getResources().getColor(R.color.chat_text_color_other));

            /*if (msg.getSender().getId() == activity.getSelfUser().getId()) {
                textView.setGravity(Gravity.RIGHT);
                row.setGravity(Gravity.RIGHT);
                textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_self));
                textView.setTextColor(getResources().getColor(R.color.chat_text_color_self));
            }*/
            row.addView(textView);
            layout.addView(row, layoutParams);

            row.setFocusableInTouchMode(true);
            row.requestFocus();
            editMessage.requestFocus();
        }
    }
}
