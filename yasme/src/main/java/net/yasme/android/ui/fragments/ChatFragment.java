package net.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetNewMessagesForChatTask;
import net.yasme.android.asyncTasks.server.GetMessageTask;
import net.yasme.android.asyncTasks.server.SendMessageTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatAdapter;
import net.yasme.android.ui.activities.ChatSettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatFragment extends Fragment implements NotifiableFragment<List<Message>> {

    private AbstractYasmeActivity activity;

    private ChatAdapter mAdapter;

    //UI references
    private EditText editMessage;
    private ListView list;

    private Chat chat;
    private int numberOfMessages;
    private List<Message> localMessages;

    public ChatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();

        Intent intent = activity.getIntent();
        long chatId = intent.getLongExtra(activity.CHAT_ID, 1);

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatFragment, List<Message>> obs = ObservableRegistry.getObservable(ChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.INSTANCE.getChatDAO().get(chatId);
            localMessages = chat.getMessages();
            numberOfMessages = localMessages.size();
            Log.d(this.getClass().getSimpleName(), "number of messages from DB: " + chat.getMessages().size());
        } catch (NullPointerException e) {
            // Occurs when new chat has been generated, but id hasn't been returned by the server yet
            chat = null;
            Log.w(this.getClass().getSimpleName(), "get chat from DB failed");
        }
        if (chat == null) {
            chat = new Chat(chatId, activity.getSelfUser());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        editMessage = (EditText) rootView.findViewById(R.id.text_message);
        list = (ListView) rootView.findViewById(R.id.chat_messageList);

        Button buttonSend = (Button) rootView.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(v);
            }
        });

        mAdapter = new ChatAdapter(activity, R.layout.chat_item_other,
                activity.getUserId(), chat.getMessages());
        list.setAdapter(mAdapter);
        mAdapter.setNotifyOnChange(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<ChatFragment, List<Message>> obs = ObservableRegistry.
                getObservable(ChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    @Override
    public void notifyFragment(List<Message> messages) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if(messages == null) {
            //Notified from GetMessageTask, new Messages are stored in the DB
            new GetNewMessagesForChatTask(numberOfMessages, chat.getId()).execute();
        } else {
            //Notified from GetNewMessageForChatTask
            updateViews(messages);
        }
        Log.d(this.getClass().getSimpleName(), "Received " + messages.size() + " messages");

        //progress bar off
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    public Chat getChat() {
        return chat;
    }

    public void send(View view) {
        String msgText = editMessage.getText().toString();
        if (msgText.isEmpty()) {
            Log.d(this.getClass().getSimpleName(), "Nichts eingegeben");
            return;
        }
        //progress bar on
        getActivity().setProgressBarIndeterminateVisibility(true);

        // Send message and get new messages afterwards
        new SendMessageTask(chat, activity.getSelfUser(), new GetMessageTask()).execute(msgText);

        Log.d(this.getClass().getSimpleName(), "Send message in bg");
        editMessage.setText("");
    }

    public void updateViews(List<Message> messages) {
        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "Keine Nachrichten zum Ausgeben");
            return;
        }

        //TODO: evtl aendern
        List<Message> tmp = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getChatId() == this.chat.getId()) {
                if (!localMessages.contains(msg)) {
                    tmp.add(msg);
                    localMessages.add(msg);
                }
            }
        }

        messages = tmp;
        numberOfMessages = numberOfMessages + tmp.size();

        mAdapter.addAll(messages);
        editMessage.requestFocus();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();
        if (itemId == R.id.action_chat_settings) {
            Intent intent = new Intent(getActivity(), ChatSettingsActivity.class);
            intent.putExtra("chat", chat);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}