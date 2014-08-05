package net.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.connection.UserTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OwnProfileFragment.OnOwnProfileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OwnProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class OwnProfileFragment extends Fragment implements View.OnClickListener, NotifiableFragment<Drawable> {

    private TextView name;
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

        name = (TextView) layout.findViewById(R.id.own_contact_header);
        email = (TextView) layout.findViewById(R.id.own_mailViewText);
        //number = (TextView) layout.findViewById(R.id.own_numberViewText);

        //imageButton = (ImageButton) layout.findViewById(R.id.own_imageButton);

        User u = activity.getSelfUser();

        name.setText(u.getName());
        email.setText(u.getEmail());
        // number.setText("");

        //imageButton.setOnClickListener(this);

        // TODO Load profile image into imageButton area as AsyncTask
        Drawable profilePicture = null;
       /*
        try {
            profilePicture = UserTask.getInstance().getProfilePicture(u.getId());
            // profilePicture will be null if no one has been uploaded yet
            if (null != profilePicture) {
                imageButton.setImageDrawable(profilePicture);
            }
        } catch (RestServiceException e) {
            Log.e("Error", e.getMessage());
        }
*/
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
        // TODO profile picture
//        switch (v.getId()){
//            case R.id.own_imageButton:
//                Intent i = new Intent(
//                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//                break;
//        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO profile picture
//        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
//            Uri selectedImage = data.getData();
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = activity.getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//
//            ImageButton imgB = (ImageButton) activity.findViewById(R.id.own_imageButton);
//
//            BitmapFactory factory = new BitmapFactory();
//            Bitmap newProfilePicture = factory.decodeFile(picturePath);
//            imgB.setImageBitmap(newProfilePicture);
//
//            // TODO Upload picture as AsyncTask
//            Drawable d = Drawable.createFromPath(picturePath);
//            try {
//                UserTask.getInstance().uploadProfilePicture(d);
//            } catch (RestServiceException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void notifyFragment(Drawable value) {

    }


    public interface OnOwnProfileFragmentInteractionListener {
        public void onOwnProfileFragmentInteraction(String s);
    }

}
