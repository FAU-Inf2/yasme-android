package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 01.09.14.
 */
public class SettingsFragment extends Fragment {
    private Switch notificationSound, notificationVibration;
    private SharedPreferences.Editor settingsEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settingsEditor = DatabaseManager.INSTANCE.getSettings().edit();

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        notificationSound = (Switch) rootView.findViewById(R.id.switch_notification_sound);
        notificationVibration = (Switch) rootView.findViewById(R.id.switch_notification_vibration);

        notificationSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    settingsEditor.putBoolean(AbstractYasmeActivity.NOTIFICATION_SOUND, true);
                    settingsEditor.commit();
                } else {
                    // The toggle is disabled
                    settingsEditor.putBoolean(AbstractYasmeActivity.NOTIFICATION_SOUND, false);
                    settingsEditor.commit();
                }
            }
        });
        notificationVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    settingsEditor.putBoolean(AbstractYasmeActivity.NOTIFICATION_VIBRATE, true);
                    settingsEditor.commit();
                } else {
                    // The toggle is disabled
                    settingsEditor.putBoolean(AbstractYasmeActivity.NOTIFICATION_VIBRATE, false);
                    settingsEditor.commit();
                }
            }
        });

        rootView.findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAbout();
            }
        });
        rootView.findViewById(R.id.legal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLegal();
            }
        });



        return rootView;
    }

    public void showAbout() {
        TextView textMessage = new TextView(getActivity());
        String aboutMessage = "";
        textMessage.setText(aboutMessage);

        Pattern pattern = Pattern.compile("Terms Of Use");
        Linkify.addLinks(textMessage, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.TOU_url);
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.about));
        alert.setMessage("");
        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.show();
    }

    public void showLegal() {
        TextView textMessage = new TextView(getActivity());
        String legalMessage = "";
        textMessage.setText(legalMessage);

        Pattern pattern = Pattern.compile("Terms Of Use");
        Linkify.addLinks(textMessage, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.TOU_url);
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.legal));
        alert.setView(textMessage);
        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}