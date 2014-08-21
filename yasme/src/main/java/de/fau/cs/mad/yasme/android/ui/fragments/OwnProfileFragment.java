package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OwnProfileFragment.OnOwnProfileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the OwnProfileFragment#newInstance factory method to
 * create an instance of this fragment.
 *
 */
public class OwnProfileFragment extends Fragment implements View.OnClickListener, NotifiableFragment<Drawable> {

    private TextView name;
    private ImageView profilePicture;
    private TextView email;
    //private TextView number;

    //private ImageButton imageButton;

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
        Log.d(this.getClass().getSimpleName(),"... successful");
        obs.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_own_profile, container, false);

        name = (TextView) layout.findViewById(R.id.own_profile_header);
        email = (TextView) layout.findViewById(R.id.own_profile_email);
        profilePicture = (ImageView) layout.findViewById(R.id.own_profile_picture);

        User self = activity.getSelfUser();

        name.setText(self.getName());
        email.setText(self.getEmail());

        // Show nice profile picture
        profilePicture.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB[(int) self.getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
        TextView initial = (TextView) layout.findViewById(R.id.own_profile_picture_text);
        initial.setText(self.getName().substring(0,1).toUpperCase());

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
