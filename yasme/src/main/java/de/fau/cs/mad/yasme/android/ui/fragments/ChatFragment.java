package de.fau.cs.mad.yasme.android.ui.fragments;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetNewMessagesForChatTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMessageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.SendMessageTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatFragment extends Fragment implements NotifiableFragment<List<Message>> {
    public static final String RESTORE_LATEST_MESSAGE_ON_DISPLAY = "LATEST_MESSAGE_ON_DISPLAY";
    public static final String RESTORE_CHAT_ID = "CHAT_ID";
    private ChatAdapter mAdapter;
    //UI references
    private EditText editMessage;
    private ListView list;
    //private List<Message> localMessages = new ArrayList<>();
    private Chat chat;
    private AtomicLong latestMessageOnDisplay;
    public ChatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();

        Intent intent = activity.getIntent();
        long chatId = intent.getLongExtra(activity.CHAT_ID, 1);

        //add the fragments own menu items
        setHasOptionsMenu(true);

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.INSTANCE.getChatDAO().get(chatId);
            // Assuming that the messages are sorted by id
            latestMessageOnDisplay = new AtomicLong(0);

            //Log.d(this.getClass().getSimpleName(), "number of messages from DB: " + chat.getMessages().size());
        } catch (NullPointerException e) {
            // Occurs when new chat has been generated, but id hasn't been returned by the server yet

            // TODO Where do you get the chatId from? The chat object won't ever be updated after the server assigned an id to the chat, will it?
            chat = null;
            Log.w(this.getClass().getSimpleName(), "get chat from DB failed");
        }
        if (chat == null) {
            Toaster.getInstance().toast(R.string.unable_open_chat, Toast.LENGTH_SHORT);
            getActivity().finish();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        // Restore saved state if member variables are null
        if (null != savedInstanceState && null == latestMessageOnDisplay) {
            long latestMessageOnDisplayId = savedInstanceState.getLong(RESTORE_LATEST_MESSAGE_ON_DISPLAY);
            latestMessageOnDisplay = new AtomicLong(latestMessageOnDisplayId);
        }
        if (null != savedInstanceState && null == chat) {
            long chatId = savedInstanceState.getLong(RESTORE_CHAT_ID);
            chat = DatabaseManager.INSTANCE.getChatDAO().get(chatId);
            if (null == chat) {
                Log.e(this.getClass().getSimpleName(), "Oh no, looks like this chat has been deleted in the meantime");
                return rootView;
            }
        }

        editMessage = (EditText) rootView.findViewById(R.id.text_message);
        list = (ListView) rootView.findViewById(R.id.chat_messageList);

        Button buttonSend = (Button) rootView.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(v);
            }
        });

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        mAdapter = new ChatAdapter(activity, R.layout.chat_item_other,
                activity.getUserId(), new ArrayList<Message>());
        list.setAdapter(mAdapter);
        mAdapter.setNotifyOnChange(true);
        notifyFragment(chat.getMessages());

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
    public void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "Update Chat");
        // Ask server for new messages
        new GetMessageTask(this.getClass()).execute();
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save states
        savedInstanceState.putLong(RESTORE_LATEST_MESSAGE_ON_DISPLAY, latestMessageOnDisplay.get());
        savedInstanceState.putLong(RESTORE_CHAT_ID, chat.getId());
    }


    @Override
    public void notifyFragment(List<Message> messages) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if(messages == null) {
            //Notified from GetMessageTask, new Messages are stored in the DB
            // Note that retrieved messages will be ordered ascending by id
            new GetNewMessagesForChatTask(latestMessageOnDisplay.get(), chat.getId(), this.getClass())
                    .execute(this.getClass().getName());
            // And don't stop the progress bar
            return;
        } else {
            //Notified from GetNewMessageForChatTask
            updateViews(messages);
        }
        Log.d(this.getClass().getSimpleName(), "Received " + messages.size() + " messages");

        //progress bar off
        //getActivity().setProgressBarIndeterminateVisibility(false);
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
        //getActivity().setProgressBarIndeterminateVisibility(true);

        // Send message and get new messages afterwards
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        new SendMessageTask(chat, activity.getSelfUser(), new GetMessageTask(this.getClass())).execute(msgText);

        Log.d(this.getClass().getSimpleName(), "Send message in bg");
        // Empty the input field after send button was pressed
        editMessage.setText("");
    }

    public void updateViews(List<Message> messages) {
        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "Keine Nachrichten zum Ausgeben");
            return;
        }

        List<Message> newMessages = new ArrayList<>();

        // Even if this fragment will be notified with same messages several times, it should not display them more than once
        // Synchronize the write access on latestMessageOnDisplay in case the fragment can be notified by more than one thread
        synchronized(this) {
            long newLatestMessageOnDisplay = latestMessageOnDisplay.get();
            for (Message msg : messages) {
                // Ignore messages which were not meant for this chat
                if (msg.getChat().getId() == chat.getId() && msg.getId() > latestMessageOnDisplay.get()) {
                    newMessages.add(msg);
                    //localMessages.add(msg);
                    newLatestMessageOnDisplay = Math.max(newLatestMessageOnDisplay, msg.getId());
                }
            }
            latestMessageOnDisplay.set(newLatestMessageOnDisplay);
        }

        int count = 0;
        ArrayList<Message> newNewMessages = new ArrayList<>();
        for (int i = 0; i < newMessages.size(); i++) {
            Message msg = newMessages.get(i);
            if (msg.getErrorId() == MessageEncryption.ErrorType.DECRYPTION_FAILED) {
                count++;
                if (i >= newMessages.size() - 1 || newMessages.get(i+1).getErrorId() != MessageEncryption.ErrorType.DECRYPTION_FAILED) {
                    msg.setMessage(String.valueOf(count) + " " + getResources().getString(R.string.decryption_failed));
                    msg.setErrorId(0);
                    newNewMessages.add(msg);
                }
            } else {
                newNewMessages.add(msg);
            }
        }

        mAdapter.addAll(newNewMessages);
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