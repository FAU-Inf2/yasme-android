package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.fau.cs.mad.yasme.android.EditTextWithImage;
import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetNewMessagesForChatTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMessageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.SendMessageTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 21.06.2014.
 */
public class ChatFragment extends Fragment implements NotifiableFragment<List<Message>> {
    public static final String RESTORE_LATEST_MESSAGE_ON_DISPLAY = "LATEST_MESSAGE_ON_DISPLAY";
    public static final String RESTORE_CHAT_ID = "CHAT_ID";
    private ChatAdapter mAdapter;
    private Chat chat;
    private AtomicLong latestMessageOnDisplay;
    private Context mContext;

    //UI references
    private Button imageCancel;
    private ImageView imageView;
    private EditText editMessage;
    private ListView list;

    final int RESULT_LOAD_IMAGE = 1;
    Bitmap bitmap;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrain instance variables!
        // setRetainInstance(true);

        ChatActivity activity = (ChatActivity) getActivity();
        mContext = DatabaseManager.INSTANCE.getContext();

        Intent intent = activity.getIntent();
        long chatId = intent.getLongExtra(AbstractYasmeActivity.CHAT_ID, -1);
        if (chatId <= 0) {
            throw new ExceptionInInitializerError("chatId <= 0");
        }

        //add the fragments own menu items
        setHasOptionsMenu(true);

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.INSTANCE.getChatDAO().get(chatId);
            // Assuming that the messages are sorted by id
            latestMessageOnDisplay = new AtomicLong(0);
        } catch (NullPointerException e) {
            // Occurs when new chat has been generated, but id hasn't been returned by the server yet
            chat = null;
            Log.e(this.getClass().getSimpleName(), "get chat from DB failed");
        }
        if (chat == null) {
            Toaster.getInstance().toast(R.string.unable_open_chat, Toast.LENGTH_SHORT);
            activity.finish();
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
        list = (ListView) rootView.findViewById(R.id.chat_messageList);
        imageCancel = (Button) rootView.findViewById(R.id.button_cancel_image);
        imageCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageResource(android.R.color.transparent);
                imageCancel.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                editMessage.setVisibility(View.VISIBLE);
                editMessage.requestFocus();
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layoutTextView = (LinearLayout) rootView.findViewById(R.id.text_view_layout);
        final EditTextWithImage ownEdit = new EditTextWithImage(DatabaseManager.INSTANCE.getContext());
        editMessage = ownEdit.getEditText();
        imageView = ownEdit.getImageView();
        imageView.setVisibility(View.GONE);

        editMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (editMessage.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (motionEvent.getX() > editMessage.getWidth() - editMessage.getPaddingRight()
                        - ownEdit.getIntrinsicWidth()) {
                    //button pressed
                    if (true) {
                        Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        imageCancel.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);
                        editMessage.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        layoutTextView.addView(imageView, 0, params);
        layoutTextView.addView(editMessage, 0, params);

        Button buttonSend = (Button) rootView.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(v);
            }
        });

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        mAdapter = new ChatAdapter(activity, R.layout.chat_item_other, new ArrayList<Message>());
        list.setAdapter(mAdapter);
        mAdapter.setNotifyOnChange(true);
        notifyFragment(chat.getMessages());

        ((ChatActivity) getActivity()).setActionBarTitle(chat.getName(), chat.getStatus());

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
        Log.d(getClass().getSimpleName(), "GetMessageTask started from ChatFragment");
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
        if (messages == null) {
            // Notified from GetMessageTask, new Messages are stored in the DB
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
    }

    public Chat getChat() {
        return chat;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = mContext.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap = BitmapFactory.decodeFile(picturePath);
            if (bitmap != null) {
                editMessage.setVisibility(View.GONE);
                imageCancel.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void send(View view) {
        if (editMessage.getVisibility() != View.GONE) {
            //case message
            String msgText = editMessage.getText().toString();
            if (msgText.isEmpty()) {
                Log.d(this.getClass().getSimpleName(), "Nichts eingegeben");
                return;
            }
            if (msgText.length() > 10000) {
                Toaster.getInstance().toast(R.string.text_to_long, Toast.LENGTH_LONG);
                return;
            }

            // Send trimmed message without whitespaces and get new messages afterwards
            AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
            Log.d(getClass().getSimpleName(), "GetMessageTask started from ChatFragment (send)");
            new SendMessageTask(chat, activity.getSelfUser(),
                    new GetMessageTask(this.getClass()), SendMessageTask.Mime.PLAIN).execute(msgText.trim());

            Log.d(this.getClass().getSimpleName(), "Send message in bg");
            // Empty the input field after send button was pressed
            editMessage.setText("");
        } else {
            // case picture
            AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
            imageView.setImageResource(android.R.color.transparent);
            imageView.setVisibility(View.GONE);
            imageCancel.setVisibility(View.GONE);
            editMessage.setVisibility(View.VISIBLE);
            editMessage.requestFocus();

            String imageMessage = PictureManager.INSTANCE.bitMapToString(bitmap);
            new SendMessageTask(chat, activity.getSelfUser(), new GetMessageTask(this.getClass()),
                    SendMessageTask.Mime.IMAGE).execute(imageMessage);
        }
    }

    public void updateViews(List<Message> messages) {
        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "Keine Nachrichten zum Ausgeben");
            return;
        }

        List<Message> newMessages = new ArrayList<>();

        // Even if this fragment will be notified with same messages several times, it should not display them more than once
        // Synchronize the write access on latestMessageOnDisplay in case the fragment can be notified by more than one thread
        synchronized (this) {
            long newLatestMessageOnDisplay = latestMessageOnDisplay.get();
            for (Message msg : messages) {
                // Ignore messages which were not meant for this chat
                if (msg.getChat().getId() == chat.getId() && msg.getId() > latestMessageOnDisplay.get()) {
                    newMessages.add(msg);
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
                if (i >= newMessages.size() - 1 || newMessages.get(i + 1).getErrorId() != MessageEncryption.ErrorType.DECRYPTION_FAILED) {
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
            intent.putExtra(ChatSettingsActivity.CHAT_ID, chat.getId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
