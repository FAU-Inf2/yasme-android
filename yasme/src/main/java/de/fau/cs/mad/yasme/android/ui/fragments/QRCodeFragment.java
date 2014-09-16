package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.contacts.QR;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.encryption.RSAEncryption;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.QRData;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

import com.google.zxing.integration.android.IntentIntegrator;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */



public class QRCodeFragment extends Fragment implements NotifiableFragment<QRData>{

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
        FragmentObservable<QRCodeFragment,QRData> obs = ObservableRegistry.getObservable(QRCodeFragment.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<QRCodeFragment, QRData> obs = ObservableRegistry.getObservable(QRCodeFragment.class);
        obs.remove(this);
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
    public void notifyFragment(QRData qrData) {
        Log.d(getClass().getSimpleName(), "QRCodeFragment has been notified!");
        if (qrData == null || qrData.getServerDevice() != null &&  qrData.getServerDevice().getUser() != null &&  qrData.getServerDevice().getUser().getName() != null) {
            Device device = qrData.getServerDevice();
            User user = device.getUser();
            Log.d(getClass().getSimpleName(), "Username" + user.getName());
            RSAEncryption rsa = new RSAEncryption();
            if (!rsa.comparePublicKeys(qrData.getPublicKey(),device.getPublicKey())) {
                Toaster.getInstance().toast(R.string.public_keys_differ, Toast.LENGTH_LONG);
                Log.i(getClass().getSimpleName(), "PublicKeys differ!");
            } else {
                Log.d(getClass().getSimpleName(), "PublicKeys are equal");
            }
            mListener.onQRCodeFragmentInteraction(user);
        } else {
            Toaster.getInstance().toast(R.string.search_no_results, Toast.LENGTH_LONG);
        }
    }

    public interface OnQRCodeFragmentInteractionListener {
        public void onQRCodeFragmentInteraction(User user);
    }

}
