package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
<<<<<<< HEAD
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
=======
>>>>>>> 4f7e305ce41957f126a53ad046bc27a7b6112e04

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.contacts.QR;
import de.fau.cs.mad.yasme.android.controller.Log;
<<<<<<< HEAD
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
=======
>>>>>>> 4f7e305ce41957f126a53ad046bc27a7b6112e04
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */


<<<<<<< HEAD

public class QRCodeFragment extends Fragment implements NotifiableFragment<ArrayList<User>>{
=======
public class QRCodeFragment extends Fragment {
>>>>>>> 4f7e305ce41957f126a53ad046bc27a7b6112e04

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

<<<<<<< HEAD
        scanButton= (Button)layout.findViewById(R.id.scan_contact_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
=======
        scan = (Button) layout.findViewById(R.id.scan_contact_button);

        scan.setOnClickListener(new View.OnClickListener() {
>>>>>>> 4f7e305ce41957f126a53ad046bc27a7b6112e04

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

<<<<<<< HEAD
    @Override
    public void notifyFragment(ArrayList<User> userList) {
        Log.d(getClass().getSimpleName(), "QRCodeFragment has been notified!");
        if (userList.size() == 1 && userList.get(0) != null) {
            User user = userList.get(0);
            Log.d(getClass().getSimpleName(), "Username" + user.getName());
            mListener.onQRCodeFragmentInteraction(user);
=======
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            // if (resultCode == RESULT_OK) {

            String contents = intent.getStringExtra("SCAN_RESULT");
            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
            Log.d(getClass().getSimpleName(), "Scanned: " + contents);

            // Handle successful scan

            //} else if (resultCode == RESULT_CANCELED) {
            // Handle cancel
            //     Log.i("App","Scan unsuccessful");
            //}
>>>>>>> 4f7e305ce41957f126a53ad046bc27a7b6112e04
        }
    }

    public interface OnQRCodeFragmentInteractionListener {
        public void onQRCodeFragmentInteraction(User user);
    }

}
