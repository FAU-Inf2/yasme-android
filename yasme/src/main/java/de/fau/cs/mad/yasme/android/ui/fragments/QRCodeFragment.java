package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.StoreImageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfilePictureTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.SetProfileDataTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UploadProfilePictureTask;
import de.fau.cs.mad.yasme.android.contacts.QR;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */



public class QRCodeFragment extends Fragment implements NotifiableFragment<ArrayList<User>>{

    private Button scanButton;
    private OnQRCodeFragmentInteractionListener mListener;

    public QRCodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<QRCodeFragment, ArrayList<User>> obs = ObservableRegistry.getObservable(QRCodeFragment.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        FragmentObservable<QRCodeFragment, ArrayList<User>> obs = ObservableRegistry.getObservable(QRCodeFragment.class);
        obs.remove(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_qr, container, false);
        mListener = (OnQRCodeFragmentInteractionListener) getActivity();

        scanButton= (Button)layout.findViewById(R.id.scan_contact_button);
        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
                scanIntegrator.initiateScan();
            }
        });


        ImageView qrCode = (ImageView) layout.findViewById(R.id.qr_code);

        QR qr = new QR();
        Bitmap bitmap = qr.generateQRCode();
        if (bitmap != null) {
            qrCode.setImageBitmap(bitmap);
        }
        return layout;
    }

    @Override
    public void notifyFragment(ArrayList<User> userList) {
        Log.d(getClass().getSimpleName(), "QRCodeFragment has been notified!");
        if (userList.size() == 1 && userList.get(0) != null) {
            User user = userList.get(0);
            Log.d(getClass().getSimpleName(), "Username" + user.getName());
            mListener.onQRCodeFragmentInteraction(user);
        }
    }

    public interface OnQRCodeFragmentInteractionListener {
        public void onQRCodeFragmentInteraction(User user);
    }

}
