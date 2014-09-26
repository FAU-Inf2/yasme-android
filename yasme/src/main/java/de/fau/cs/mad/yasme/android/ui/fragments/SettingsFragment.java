package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangePasswordTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 01.09.14.
 */
public class SettingsFragment extends Fragment {
    private Switch notificationSound, notificationVibration;
    private SharedPreferences.Editor settingsEditor;
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settings = DatabaseManager.INSTANCE.getSettings();
        settingsEditor = settings.edit();

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        notificationSound = (Switch) rootView.findViewById(R.id.switch_notification_sound);
        notificationVibration = (Switch) rootView.findViewById(R.id.switch_notification_vibration);

        notificationSound.setChecked(settings.getBoolean(AbstractYasmeActivity.NOTIFICATION_SOUND, false));
        notificationVibration.setChecked(settings.getBoolean(AbstractYasmeActivity.NOTIFICATION_VIBRATE, false));

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
        rootView.findViewById(R.id.button_reset_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetPassword();
            }
        });

        return rootView;
    }

    public void showAbout() {
        TextView textMessage = new TextView(getActivity());
        TextView contactView = new TextView(getActivity());
        LinearLayout content = new LinearLayout(getActivity());
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        textMessage.setText(R.string.link_text_about);
        contactView.setText(R.string.contact);

        Pattern pattern = Pattern.compile("YASME");
        Linkify.addLinks(textMessage, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.link_readme);
            }
        });

        content.addView(textMessage, layoutParams);
        content.addView(contactView, layoutParams);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.about));
        alert.setView(content);
        alert.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.show();
    }

    public void showLegal() {
        TextView textMessage = new TextView(getActivity());
        String legalMessage = getString(R.string.link_text_legal);
        textMessage.setText(legalMessage);

        Pattern pattern = Pattern.compile(getString(R.string.TOS));
        Linkify.addLinks(textMessage, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.TOS_url);
            }
        });
        pattern = Pattern.compile(getString(R.string.privacy_policy));
        Linkify.addLinks(textMessage, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.privacy_policy_url);
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.legal));
        alert.setView(textMessage);
        alert.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {

            }
        });
        alert.show();
    }

    public void showResetPassword() {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(getString(R.string.request_email_title));

        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        final TextView requestEmailText = new TextView(activity);
        requestEmailText.setText(R.string.request_email_body);

        final EditText mail = new EditText(activity);
        mail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);

        list.addView(mail);
        list.addView(requestEmailText, layoutParams);

        alert.setView(list);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        String emailTmp = mail.getText().toString();
                        Log.d(this.getClass().getSimpleName(), "Mail to send token at: " + emailTmp);
                        User user = new User();
                        user.setEmail(emailTmp);
                        new ChangePasswordTask(user).execute("1");
                        forgotPasswordDialog(emailTmp);
                    }
                }
        );

        // Skip, email was already sent
        alert.setNeutralButton(R.string.skip,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        forgotPasswordDialog(null);
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }

    public void forgotPasswordDialog(final String inputMail) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(getString(R.string.password_title));

        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        final EditText token = new EditText(activity);
        token.setInputType(InputType.TYPE_CLASS_TEXT);
        token.setHint(R.string.hint_mail_token);

        final TextView tokenExplanation = new TextView(activity);
        tokenExplanation.setText(R.string.explanation_mail_token);

        final EditText password = new EditText(activity);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint(R.string.hint_new_password);

        final EditText passwordCheck = new EditText(activity);
        passwordCheck.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordCheck.setHint(R.string.hint_repeat_new_password);

        final EditText mail = new EditText(activity);
        mail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);

        if (inputMail == null || inputMail.isEmpty()) {
            mail.setText(inputMail);
            list.addView(mail);
        }
        list.addView(token, layoutParams);
        list.addView(tokenExplanation, layoutParams);
        list.addView(password, layoutParams);
        list.addView(passwordCheck, layoutParams);

        alert.setView(list);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Grab the EditText's input
                        String email;
                        if (inputMail == null || inputMail.isEmpty()) {
                            email = mail.getText().toString();
                        } else {
                            email = inputMail;
                        }
                        String mailToken = token.getText().toString();
                        String inputPassword = password.getText().toString();
                        String inputPasswordCheck = passwordCheck.getText()
                                .toString();

                        if (password.getText().length() < 8) {
                            Toaster.getInstance().toast(R.string.password_too_short,
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        if (!inputPassword.equals(inputPasswordCheck)) {
                            Toaster.getInstance().toast(R.string.passwords_do_not_match,
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        User user = new User(email, inputPassword);
                        PasswordEncryption pwEnc = new PasswordEncryption(user);
                        User secured = pwEnc.securePassword();
                        new ChangePasswordTask(secured).execute("0", mailToken);
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
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