package net.yasme.android.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.UserLoginTask;
import net.yasme.android.asyncTasks.server.UserRegistrationTask;
import net.yasme.android.controller.NotifiableFragment;

/**
 * Created by robert on 06.07.14.
 */
public class RegisterFragment extends Fragment implements NotifiableFragment<RegisterFragment.RegistrationParam> {


    AbstractYasmeActivity activity;
    private UserRegistrationTask regTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void onPostRegisterExecute(Boolean success, String email, String password) {

        if (success) {
            //Login after registration was successfull
            UserLoginTask authTask = new UserLoginTask(activity.getStorage(), activity.getApplicationContext());
            authTask.execute(email, password);
            activity.getSelfUser().setEmail(email);
            Toast.makeText(
                    activity.getApplicationContext(),
                    getResources().getString(
                            R.string.registration_successful),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    activity.getApplicationContext(),
                    getResources().getString(
                            R.string.registration_not_successful),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void notifyFragment(RegistrationParam regParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        onPostRegisterExecute(regParam.getSuccess(), regParam.getEmail(), regParam.getPassword());
    }


    public static class RegistrationParam {
        private Boolean success;
        private String email;
        private String password;

        public RegistrationParam(Boolean success, String email, String password) {
            this.success = success;
            this.email = email;
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }

        public Boolean getSuccess() {
            return success;
        }
    }
}
