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

import java.io.IOException;

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
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.PictureManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatAdapter;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */



public class QRCodeFragment extends Fragment {

    private Button scan;

    public QRCodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_qr, container, false);

        scan= (Button)layout.findViewById(R.id.scan_contact_button);

        scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
           // if (resultCode == RESULT_OK) {

                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.d(getClass().getSimpleName(),"Scanned: " + contents);

                // Handle successful scan

            //} else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
           //     Log.i("App","Scan unsuccessful");
            //}
        }
    }
}
