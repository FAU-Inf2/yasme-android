package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.SetProfileDataTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

/**
 * Created by Stefan Ettl <stefan.ettl@fau.de>
 */


public class OwnProfileFragment extends Fragment implements View.OnClickListener, NotifiableFragment<Drawable> {

    private EditText name;
    private ImageView profilePicture;
    private TextView email, id;
    private OnOwnProfileFragmentInteractionListener mListener;

    public OwnProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get OwnProfileObservable");
        FragmentObservable<OwnProfileFragment, Drawable> obs = ObservableRegistry.getObservable(OwnProfileFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_own_profile, container, false);

        email = (TextView) layout.findViewById(R.id.own_profile_email);
        id = (TextView) layout.findViewById(R.id.own_profile_id);
        profilePicture = (ImageView) layout.findViewById(R.id.own_profile_picture);
        name = (EditText) layout.findViewById(R.id.own_profile_header);
        name.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event isn't a key-down event on the "enter" button, skip this.
                if (!((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)))
                    return false;
                AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
                // Hide virtual keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                // Set Focus away from edittext
                name.setFocusable(false);
                name.setFocusableInTouchMode(true);

                // Save name in android device
                User u = activity.getSelfUser();
                u.setName(name.getText().toString());
                long t = -1;
								User newUser = new User(u.getName(),u.getEmail(),t);
                new SetProfileDataTask(newUser).execute();
                return true;
            }
        });

        User self = activity.getSelfUser();
        name.setText(self.getName());
        email.setText(self.getEmail());
        id.setText("" + self.getId());

        // Show nice profile picture
				if(self.getId()>0) {
	        profilePicture.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB[(int) self.getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
	        TextView initial = (TextView) layout.findViewById(R.id.own_profile_picture_text);
	        initial.setText(self.getName().substring(0, 1).toUpperCase());
				}

        return layout;
    }

    public void onButtonPressed(String s) {
        if (mListener != null) {
            mListener.onOwnProfileFragmentInteraction(s);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOwnProfileFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyFragment(Drawable value) {
    }

    public interface OnOwnProfileFragmentInteractionListener {
        public void onOwnProfileFragmentInteraction(String s);
    }
}
