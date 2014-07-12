package net.yasme.android.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.CreateChatTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDetailsFragment.OnDetailsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment
        extends DialogFragment
        implements View.OnClickListener,
        NotifiableFragment<UserDetailsFragment.UserDetailsFragmentParam> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER = "userparam";
    private static final String ARG_USERNAME = "param1";
    private static final String ARG_USERMAIL = "param2";
    private static final String ARG_USERID = "param3";
    private static final String ARG_CONTACTBUTTON = "param4";

    // TODO: Rename and change types of parameters

    private User contact;
    private User selfUser;
    private AbstractYasmeActivity activity;

    private TextView contactName;
    private TextView email;
    private TextView number;
    private Button startChat;
    private Button addContact;
    private ImageButton mailButton;
    private ImageButton numberButton;
    private UserDAO userDAO;

    private OnDetailsFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserDetailsFragment newInstance(User theContact, Boolean addContactButton) {
        UserDetailsFragment fragment = new UserDetailsFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, theContact);
        args.putString(ARG_USERNAME, theContact.getName());
        args.putString(ARG_USERMAIL, theContact.getEmail());
        args.putString(ARG_USERID, String.valueOf(theContact.getId()));
        args.putBoolean(ARG_CONTACTBUTTON, addContactButton);
        fragment.setArguments(args);

        return fragment;
    }

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            contact = (User) getArguments().getSerializable(ARG_USER);
            //contact.setName(getArguments().getString(ARG_USERNAME));
            //contact.setEmail(getArguments().getString(ARG_USERMAIL));
            //contact.setId(Long.valueOf(getArguments().getString(ARG_USERID)));
        }

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<UserDetailsFragment, UserDetailsFragmentParam> obs =
                ObservableRegistry.getObservable(UserDetailsFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        if (!obs.isRegistered(this)) {
            obs.register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_user_details, container, false);
        contactName = (TextView) layout.findViewById(R.id.contact_header);
        email = (TextView) layout.findViewById(R.id.mailViewText);
        number = (TextView) layout.findViewById(R.id.numberViewText);
        startChat = (Button) layout.findViewById(R.id.contact_detail_newchat);
        addContact = (Button) layout.findViewById(R.id.contact_detail_addcontact);
        mailButton = (ImageButton) layout.findViewById(R.id.mail_image_button);
        numberButton = (ImageButton) layout.findViewById(R.id.number_image_button);

        contactName.setText(contact.getName());
        email.setText(contact.getEmail());
        number.setText("");

        startChat.setOnClickListener(this);
        addContact.setOnClickListener(this);
        mailButton.setOnClickListener(this);
        numberButton.setOnClickListener(this);

        activity = (AbstractYasmeActivity) getActivity();
        selfUser = activity.getSelfUser();

        userDAO = DatabaseManager.INSTANCE.getUserDAO();

        if (!getArguments().getBoolean(ARG_CONTACTBUTTON)) {
            addContact.setVisibility(View.GONE);
        }

        return layout;
    }

    public void onButtonPressed(String s) {
        if (mListener != null) {
            mListener.onDetailsFragmentInteraction(contact, 0);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDetailsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.contact_detail_newchat:
                Log.d(this.getClass().getSimpleName(), "------------------- Create New Chat ---------------------------");
                List<User> selectedUsers = new ArrayList<User>();
                selectedUsers.add(contact);
                new CreateChatTask(selfUser, selectedUsers).execute();
                break;

            case R.id.contact_detail_addcontact:
                contact.addToContacts();
                //db.createUserIfNotExists(user);
                userDAO.addOrUpdate(contact);
                Log.d(this.getClass().getSimpleName(), "------------------- Contact Added ---------------------------" + userDAO.getContacts());
                break;

            case R.id.mail_image_button:
                this.sendMail(contact.getEmail());
                break;
            case R.id.number_image_button:
                break;
        }

    }

    private void sendMail(String email) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT, "Message powered by YASME");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public void startChat(long chatId) {
        //Log.d(this.getClass().getSimpleName(), "Start chat: " + chatId);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
    }


    public interface OnDetailsFragmentInteractionListener {
        public void onDetailsFragmentInteraction(User user, Integer buttonId);
    }


    @Override
    public void notifyFragment(UserDetailsFragmentParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (param instanceof NewChatParam) {
            notifyFragment((NewChatParam) param);
        }
        if (param instanceof UserDetailsParam) {
            notifyFragment((UserDetailsParam) param);
        }
    }

    public void notifyFragment(NewChatParam newChatParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified with newChatParam");
        startChat(newChatParam.getChatId());
    }

    public void notifyFragment(UserDetailsParam userDetailsParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified with userDetailsParam");

    }

    //superclass of notifyParameters
    public static class UserDetailsFragmentParam {

    }

    public static class NewChatParam extends UserDetailsFragmentParam {
        private Long chatId;

        public NewChatParam(Long chatId) {
            this.chatId = chatId;
        }

        public Long getChatId() {
            return chatId;
        }
    }

    public static class UserDetailsParam extends UserDetailsFragmentParam {
        private Boolean success;
        private Long userId;
        private String accessToken;

        public UserDetailsParam(Boolean success, Long userId, String accessToken) {
            this.success = success;
            this.userId = userId;
            this.accessToken = accessToken;
        }

        public Long getUserId() {
            return userId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public Boolean getSuccess() {
            return success;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<UserDetailsFragment, UserDetailsFragmentParam> obs =
                ObservableRegistry.getObservable(UserDetailsFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        if (!obs.isRegistered(this)) {
            obs.register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<UserDetailsFragment, UserDetailsFragmentParam> obs =
                ObservableRegistry.getObservable(UserDetailsFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }
}
