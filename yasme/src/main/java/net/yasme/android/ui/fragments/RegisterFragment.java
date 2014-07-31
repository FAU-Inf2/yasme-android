package net.yasme.android.ui.fragments;

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
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 06.07.14.
 */
public class RegisterFragment extends Fragment implements NotifiableFragment<RegisterFragment.RegParam> {
    private AbstractYasmeActivity activity;
    private UserRegistrationTask regTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();

        SharedPreferences storage = DatabaseManager.INSTANCE.getSharedPreferences();
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

        final EditText mail = new EditText(getActivity());
        mail.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);

        final EditText password = new EditText(getActivity());
        password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint(R.string.registration_password);

        final EditText password_check = new EditText(getActivity());
        password_check.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password_check.setHint(R.string.registration_repeat_password);

        list.addView(name, layoutParams);
        list.addView(mail, layoutParams);
        list.addView(password, layoutParams);
        list.addView(password_check, layoutParams);

        alert.setView(list);

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

                        regTask.execute(inputName, inputMail, inputPassword, inputPasswordCheck);
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton(R.string.registration_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }

    public void onPostRegisterExecute(Boolean success, String email, String password) {

        if (success) {
            //Login after registration was successfull
            Toast.makeText(
                    activity.getApplicationContext(),
                    getResources().getString(
                            R.string.registration_successful),
                    Toast.LENGTH_SHORT
            ).show();
            UserLoginTask authTask = new UserLoginTask(activity.getStorage(), activity.getApplicationContext());
            authTask.execute(email, password);
            activity.getSelfUser().setEmail(email);
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
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(), "Try to get LoginObservableInstance");
        FragmentObservable<RegisterFragment, RegParam> obs =
                ObservableRegistry.getObservable(RegisterFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<RegisterFragment, RegParam> obs =
                ObservableRegistry.getObservable(LoginFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    @Override
    public void notifyFragment(RegParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (param instanceof RegLoginParam)
            notifyFragment((RegLoginParam) param);
        else if (param instanceof RegistrationParam)
            notifyFragment((RegistrationParam) param);
    }

    public void notifyFragment(RegLoginParam regParam) {
        //TODO: activity starten, wird eventuell auch schon von LoginFragment erledigt
    }

    public void notifyFragment(RegistrationParam regParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        onPostRegisterExecute(regParam.getSuccess(), regParam.getEmail(), regParam.getPassword());
    }

    public static class RegParam {
        protected Boolean success;

        public Boolean getSuccess() {
            return success;
        }
    }

    public static class RegLoginParam extends RegParam{
        private long userId;

        public long getUserId() {
            return userId;
        }

        public RegLoginParam(Boolean success, long userId) {
            this.success = success;
            this.userId = userId;

        }
    }

    public static class RegistrationParam extends RegParam{
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
    }
}
