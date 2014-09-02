package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

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

        return rootView;
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