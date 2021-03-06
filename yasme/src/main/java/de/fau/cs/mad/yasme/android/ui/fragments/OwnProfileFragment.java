package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
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

import java.io.File;
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
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

/**
 * Created by Stefan Ettl <stefan.ettl@fau.de>
 * Modified by Tim Nisslbeck <hu78sapy@stud.cs.fau.de>
 * Modified by Robert Meissner <robert.meissner@studium.fau.de>
 */


public class OwnProfileFragment extends Fragment implements View.OnClickListener, NotifiableFragment<Drawable> {

    private EditText name;
    private ImageView profilePictureView;
    private TextView initial;
    private OnOwnProfileFragmentInteractionListener mListener;
    private User self;
    private AbstractYasmeActivity activity;

    private final static int RESULT_LOAD_IMAGE = 10;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 20;
    private final static int PIC_CROP = 30;
    private Uri imageUri;
    private Uri cropUri;

    private static final String RESTORE_IMAGE_URI = "imageUri";

    public OwnProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore the image uri if the device is rotated while capturing a profile photo
        // These lines would better fit to onRestoreView but this method requires a higher API level
        if (null != savedInstanceState) {
            if (null == imageUri && savedInstanceState.containsKey(RESTORE_IMAGE_URI)) {
                imageUri = Uri.fromFile(new File(savedInstanceState.getString(RESTORE_IMAGE_URI)));
            }
        }

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
                String newName = sanitizer.sanitize(oldName);
                if (!oldName.equals(newName)) {
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
            if (self.getName() != null && !self.getName().isEmpty()) {
                initial.setText(self.getName().substring(0, 1).toUpperCase());
            }

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != outState && null != imageUri) {
            outState.putString(RESTORE_IMAGE_URI, imageUri.getPath());
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.own_profile_picture:
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle(getString(R.string.select_image_source_title));
                alert.setMessage(getString(R.string.select_image_source_message));
                if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    alert.setNeutralButton(R.string.select_camera, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // create Intent to take a picture and return control to the calling application
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            //create temp file
                            File file = null;
                            try {
                                file = PictureManager.getOutputMediaFilePath();
                            } catch (IOException e) {
                                Log.e(this.getClass().getSimpleName(), "Error during creating the file");
                                return;
                            }
                            imageUri = Uri.fromFile(file);

                            try {
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                intent.putExtra("data", true);
                                // start the image capture Intent
                                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                            } catch (Exception e) {
                                Log.e(this.getClass().getSimpleName(), "Error during capturing an image");
                            }
                        }
                    });
                }
                alert.setPositiveButton(R.string.select_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, RESULT_LOAD_IMAGE);
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alert.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP && resultCode == Activity.RESULT_OK) {
            String picturePath = cropUri.getPath();
            Log.d(this.getClass().getSimpleName(), "retrievedResult from crop");

            User user = new User();
            user.setProfilePicture(picturePath);
            Bitmap bitmap = PictureManager.INSTANCE.getPicture(user, 256, 256);

            storeBitmap(bitmap);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = DatabaseManager.INSTANCE.getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageUri = Uri.fromFile(new File(picturePath));
            makeASquare(picturePath);
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String path = imageUri.getPath();
            makeASquare(path);
        }
    }

    private void performCrop(String path) {
        Log.d(this.getClass().getSimpleName(), "perform crop");

        try {
            //create temp file
            File tempFile = null;
            try {
                tempFile = PictureManager.getOutputMediaFilePath();
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Error during creating the file");
                makeASquare(path);
            }
            cropUri = Uri.fromFile(tempFile);

            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(imageUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            cropIntent.putExtra("data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);

        } catch (ActivityNotFoundException anfe) {
            makeASquare(path);
        }
    }

    private void makeASquare(String path) {
        User user = new User();
        user.setProfilePicture(path);
        Bitmap newProfilePicture = PictureManager.INSTANCE.getPicture(user, 256, 256);

        int size;
        if (newProfilePicture.getHeight() <= newProfilePicture.getWidth()) {
            size = newProfilePicture.getHeight();
        } else {
            size = newProfilePicture.getWidth();
        }
        Bitmap squareBitmap = ThumbnailUtils.extractThumbnail(newProfilePicture, size, size);

        storeBitmap(squareBitmap);
    }


    private void storeBitmap(Bitmap bitmap) {
        String path = "";
        try {
            path = PictureManager.INSTANCE.storePicture(self, bitmap);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return;
        }
        activity.setOwnProfilePicture(path);

        // set picture
        notifyFragment(new BitmapDrawable(getResources(), bitmap));

        // Upload picture as AsyncTask
        new UploadProfilePictureTask(bitmap).execute();
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
