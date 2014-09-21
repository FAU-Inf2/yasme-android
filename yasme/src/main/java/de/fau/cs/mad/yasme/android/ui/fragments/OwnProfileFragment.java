package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfilePictureTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.SetProfileDataTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UploadProfilePictureTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Sanitizer;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

/**
 * Created by Stefan Ettl <stefan.ettl@fau.de>
 * Modified by Tim Nisslbeck <hu78sapy@stud.cs.fau.de>
 */


public class OwnProfileFragment extends Fragment implements View.OnClickListener, NotifiableFragment<Drawable> {

    private EditText name;
    private ImageView profilePictureView;
    private TextView initial;
    private OnOwnProfileFragmentInteractionListener mListener;
    User self;
    AbstractYasmeActivity activity;

    private static int RESULT_LOAD_IMAGE = 1;

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
        activity = (AbstractYasmeActivity) getActivity();
        self = activity.getSelfUser();

        View layout = inflater.inflate(R.layout.fragment_own_profile, container, false);
        TextView email = (TextView) layout.findViewById(R.id.own_profile_email);
        TextView id = (TextView) layout.findViewById(R.id.own_profile_id);
        initial = (TextView) layout.findViewById(R.id.own_profile_picture_text);
        profilePictureView = (ImageView) layout.findViewById(R.id.own_profile_picture);
        profilePictureView.setOnClickListener(this);


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
                Sanitizer sanitizer = new Sanitizer();
                String oldName = name.getText().toString();
                String newName=sanitizer.sanitize(oldName);
                if(!oldName.equals(newName)) {
                    Toast.makeText(getActivity(), getString(R.string.illegal_characters) + ": " + sanitizer.getRegex(), Toast.LENGTH_LONG).show();
                    name.setText(newName);
                }
                new SetProfileDataTask(new User(newName, activity.getUserMail(), -1)).execute();
                return true;
            }
        });

        name.setText(self.getName());
        email.setText(self.getEmail());
        id.setText("" + self.getId());
        self.setProfilePicture(activity.getOwnProfilePicture());

        BitmapDrawable pic = null;
        if (self.getProfilePicture() != null) {
            int width = 300;
            int height = 300;
            Log.e(this.getClass().getSimpleName(), "Width: " + width + " Height: " + height);
            pic = new BitmapDrawable(getResources(), PictureManager.INSTANCE
                    .getPicture(self, height, width));
            Log.d(this.getClass().getSimpleName(), "Try to load Picture from: " + self.getProfilePicture());
        }
        if (pic == null) {
            // Show nice profile picture
            Log.d(this.getClass().getSimpleName(), "using standard picture");
            profilePictureView.setBackgroundColor(ChatAdapter.CONTACT_DUMMY_COLORS_ARGB
                    [(int) self.getId() % ChatAdapter.CONTACT_DUMMY_COLORS_ARGB.length]);
            initial.setText(self.getName().substring(0, 1).toUpperCase());

            // Load profile image into profilePictureView from server as AsyncTask if available
            new GetProfilePictureTask(getClass()).execute(self.getId());
        } else {
            notifyFragment(pic);
            Log.d(this.getClass().getSimpleName(), "successful loaded picture");
        }
        return layout;
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
        switch (v.getId()) {
            case R.id.own_profile_picture:
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            //store own image on device
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);

            // Calculate inSampleSize
            options.inSampleSize = PictureManager.INSTANCE.calculateInSampleSize(options, 500, 500);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newProfilePicture = BitmapFactory.decodeFile(picturePath, options);

            String path = "";
            try {
                path = PictureManager.INSTANCE.storePicture(self, newProfilePicture);
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return;
            }
            activity.setOwnProfilePicture(path);

            // set picture
            notifyFragment(new BitmapDrawable(getResources(), newProfilePicture));

            // Upload picture as AsyncTask
            new UploadProfilePictureTask(newProfilePicture).execute();
        }
    }

    @Override
    public void notifyFragment(Drawable value) {
        initial.setVisibility(View.GONE);
        profilePictureView.setBackgroundColor(Color.TRANSPARENT);
        profilePictureView.setImageDrawable(value);
    }

    public interface OnOwnProfileFragmentInteractionListener {
        public void onOwnProfileFragmentInteraction(String s);
    }
}
