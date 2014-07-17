package net.yasme.android.ui.fragments;

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
import android.widget.ListView;
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
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatFragment extends Fragment implements NotifiableFragment<List<Message>> {

    private AbstractYasmeActivity activity;

    private SharedPreferences storage;
    private ChatAdapter mAdapter;

    //UI references
    private EditText editMessage;
    private TextView status;
    private LinearLayout layout;
    private ListView list;

    private Chat chat;

    //MessageEncryption aes;

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
            // Occurs when new chat has been generated, but id hasn't been returned by the server yet
            chat = null;
            Log.w(this.getClass().getSimpleName(), "get chat from DB failed");
        }
        if (chat == null) {
            chat = new Chat(chatId, activity.getSelfUser());
        }

        //DEBUG, TODO: encryption speichern und auslesen
        //aes = new MessageEncryption(chat, activity.getSelfUser());
        //chat.setEncryption(aes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        editMessage = (EditText) rootView.findViewById(R.id.text_message);
        status = (TextView) rootView.findViewById(R.id.text_status);
        status.setText("Eingeloggt: " +
                storage.getString(AbstractYasmeActivity.USER_NAME, "anonym"));
        layout = (LinearLayout) rootView.findViewById(R.id.scrollLayout);
        list = (ListView) rootView.findViewById(R.id.chat_messageList);

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

        boolean old = false;
        if(old) {
            updateViews(chat.getMessages());
        } else {
            //Get adapter and set old messages
            mAdapter = new ChatAdapter(activity, R.layout.chat_item,
                    activity.getUserId(), chat.getMessages());
            list.setAdapter(mAdapter);
            mAdapter.setNotifyOnChange(true);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatFragment, List<Message>> obs = ObservableRegistry.getObservable(ChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);

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
        status.setText("Received " + messages.size() + " messages");
    }

    public TextView getStatus() {
        return status;
    }

    public void send(View view) {
        String msgText = editMessage.getText().toString();
        if (msgText.isEmpty()) {
            status.setText("Nichts eingegeben");
            return;
        }
        //String msgEncrypted = aes.encrypt(editMessage.getText().toString());
        //String msgEncrypted = msg;
        //User user = new User(activity.getSelfUser().getName(), activity.getSelfUser().getEmail(), activity.getSelfUser().getId());
        //long aesId = aes.getKeyId();

        // Send message and get new messages afterwards
        new SendMessageTask(chat, activity.getSelfUser(), new GetMessageTask())
                .execute(msgText);
        status.setText("Send message in bg");
        editMessage.setText("");
    }

    public void asyncUpdate() {
        status.setText("GET messages");
        new GetMessageTask().execute();
        status.setText("GET messages in bg");
    }

    public void update(View view) {
        asyncUpdate();
    }

    public void updateViews(List<Message> messages) {
        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "Keine Nachrichten zum Ausgeben");
            return;
        }

        //TODO: evtl aendern
        List<Message> tmp = new ArrayList<>();
        for(Message msg : messages) {
            if(msg.getChatId() == this.chat.getId()) {
                tmp.add(msg);
            }
        }
        messages = tmp;

        boolean old = false;
        if(!old) {
            mAdapter.addAll(messages);
            editMessage.requestFocus();
        } else {
            for (Message msg : messages) {
                //msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getMessageKeyId())));
                TextView textView = new TextView(activity.getApplicationContext());

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                RelativeLayout row = new RelativeLayout(activity.getApplicationContext());
                row.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                String name;
                try {
                    name = DatabaseManager.INSTANCE.getUserDAO().get(msg.getSender().getId()).getName();
                } catch (NullPointerException e) {
                    Log.d(this.getClass().getSimpleName(), "User nicht in DB gefunden");
                    name = "anonym";
                }
                textView.setText(name + ": " + msg.getMessage());

                if (msg.getSender().getId() == activity.getSelfUser().getId()) {
                    textView.setGravity(Gravity.RIGHT);
                    row.setGravity(Gravity.RIGHT);
                    textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_self));
                    textView.setTextColor(getResources().getColor(R.color.chat_text_color_self));
                } else {
                    textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
                    textView.setTextColor(getResources().getColor(R.color.chat_text_color_other));
                }

                row.addView(textView);
                layout.addView(row, layoutParams);

                row.setFocusableInTouchMode(true);
                row.requestFocus();
                editMessage.requestFocus();
            }
        }
    }
}
