package net.yasme.android.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.UserRegistrationTask;

/**
 * Created by robert on 06.07.14.
 */
public class RegisterFragment extends Fragment {


    AbstractYasmeActivity activity;
    private UserRegistrationTask regTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences storage = activity.getStorage();
        regTask = new UserRegistrationTask(storage);

        registerDialog();
    }

    private void registerDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.registration_title));

        LinearLayout list = new LinearLayout(getActivity());
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText name = new EditText(getActivity());
        name.setHint(R.string.registration_name);
        list.addView(name, layoutParams);
        final EditText mail = new EditText(getActivity());
        mail.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);
        list.addView(mail, layoutParams);
        final EditText password = new EditText(getActivity());
        password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        password.setHint(R.string.registration_password);
        list.addView(password, layoutParams);
        final EditText password_check = new EditText(getActivity());
        password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        password_check.setHint(R.string.registration_repeat_password);
        list.addView(password_check, layoutParams);
        alert.setView(list);
        //TODO: Input type seems to change nothing??

        // "OK" button to save the values
        alert.setPositiveButton(R.string.registration_button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Grab the EditText's input
                        String inputName = name.getText().toString();
                        String inputMail = mail.getText().toString();
                        String inputPassword = password.getText().toString();
                        String inputPasswordCheck = password_check.getText()
                                .toString();

                        //TODO: RSA-Keys erstellen und oeffentlichen Schluessel senden
                        regTask.execute(inputName, inputMail, inputPassword, inputPasswordCheck);
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton(R.string.registration_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );
        alert.show();
    }
}
