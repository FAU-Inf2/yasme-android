package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetContactsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.CreateChatTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;

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

    private TextView contactName;
    private ImageView profilePicture;
    // private TextView email;
    // private TextView number;
    private Button startChat;
    private Button addContact;
    // private ImageButton mailButton;
    // private ImageButton numberButton;
    private UserDAO userDAO = DatabaseManager.INSTANCE.getUserDAO();
    private Context context = DatabaseManager.INSTANCE.getContext();

    private OnDetailsFragmentInteractionListener mListener;

    public UserDetailsFragment() {
        // Required empty public constructor
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            contact = (User) getArguments().getSerializable(ARG_USER);
            //contact.setName(getArguments().getString(ARG_USERNAME));
            //contact.setEmail(getArguments().getString(ARG_USERMAIL));
            //contact.setId(Long.valueOf(getArguments().getString(ARG_USERID)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        selfUser = activity.getSelfUser();

        View layout = inflater.inflate(R.layout.fragment_user_details, container, false);
        contactName = (TextView) layout.findViewById(R.id.contact_header);
        profilePicture = (ImageView) layout.findViewById(R.id.contact_details_profile_picture);
        // email = (TextView) layout.findViewById(R.id.mailViewText);
        //number = (TextView) layout.findViewById(R.id.numberViewText);
        startChat = (Button) layout.findViewById(R.id.contact_detail_newchat);
        addContact = (Button) layout.findViewById(R.id.contact_detail_addcontact);
        // mailButton = (ImageButton) layout.findViewById(R.id.mail_image_button);
        // numberButton = (ImageButton) layout.findViewById(R.id.number_image_button);

        contactName.setText(contact.getName());
        // email.setText(contact.getEmail());
        // number.setText("");

        // Show first letter of contact name as profile image
        if (profilePicture != null) {
            profilePicture.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB[(int) contact.getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
            TextView initial = (TextView) layout.findViewById(R.id.contact_details_profile_picture_text);
            initial.setText(contact.getName().substring(0,1).toUpperCase());
        }


        // Don't show button to add self to contacts
        if (selfUser.getId() == contact.getId()) {
            startChat.setVisibility(View.GONE);
            addContact.setVisibility(View.GONE);
        } else {
            startChat.setOnClickListener(this);
            addContact.setOnClickListener(this);
        }

        // mailButton.setOnClickListener(this);
        // numberButton.setOnClickListener(this);

        if (!getArguments().getBoolean(ARG_CONTACTBUTTON)) {
            addContact.setVisibility(View.GONE);
        }

        return layout;
    }

//    public void onButtonPressed(String s) {
//        if (mListener != null) {
//            mListener.onDetailsFragmentInteraction(contact, 0);
//        }
//    }


    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<UserDetailsFragment, UserDetailsFragmentParam> obs = ObservableRegistry.getObservable(UserDetailsFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<UserDetailsFragment, UserDetailsFragmentParam> obs =
                ObservableRegistry.getObservable(UserDetailsFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
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

                // Add to contacts, but don't show toasts
                addToContacts(contact, false);
                Set<User> selectedUsers = new HashSet<>();
                selectedUsers.add(contact);
                // Don't dismiss. Wait until you're notified and than go to the chat view
                new CreateChatTask(selfUser, selectedUsers).execute(this.getClass().getName());
                break;

            case R.id.contact_detail_addcontact:

                addToContacts(contact, true);
                this.dismiss();
                break;

            //case R.id.mail_image_button:
            //    this.sendMail(contact.getEmail());
            //    break;
            //case R.id.number_image_button:
            //    break;
        }

    }


    private void addToContacts(User tappedUser, boolean showToast) {
        User userFromDb = userDAO.get(tappedUser.getId());
        if (null != userFromDb) {
            contact = userFromDb;
        }

        // Contact flag will be zero if the tapped user was not found in the database
        if (contact.isContact()) {
            if (showToast) {
                String toast = contact.getName() + " " + context.getText(R.string.contact_already_added) + ".";
                Toaster.getInstance().toast(toast, Toast.LENGTH_LONG);
            }
            return;
        }

        contact.addToContacts();
        userDAO.addOrUpdate(contact);
        Log.d(this.getClass().getSimpleName(), "contact added");

        if (showToast) {
            String toast = contact.getName() + " " + context.getText(R.string.contact_added_success) + ".";
            Toaster.getInstance().toast(toast, Toast.LENGTH_LONG);
        }

        // Refresh contact list in first tab
        new GetContactsTask().execute(this.getClass().getName());
    }


    private void sendMail(String email) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.contact_details_email_default_subject));
        i.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.contact_details_email_default_body));
        try {
            startActivity(Intent.createChooser(i, context.getString(R.string.contact_details_compose_email_action)));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(this.getClass().getSimpleName(), ex.getMessage());
            ex.printStackTrace();
        }

    }

    public void startChat(long chatId) {
        // TODO
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        //Log.d(this.getClass().getSimpleName(), "Start chat: " + chatId);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
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

    public interface OnDetailsFragmentInteractionListener {
        public void onDetailsFragmentInteraction(User user, Integer buttonId);
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

        public UserDetailsParam(Boolean success, Long userId) {
            this.success = success;
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }


        public Boolean getSuccess() {
            return success;
        }
    }
}
