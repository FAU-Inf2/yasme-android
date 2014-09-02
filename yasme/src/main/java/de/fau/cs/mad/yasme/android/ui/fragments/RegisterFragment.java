package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UserLoginTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UserRegistrationTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 06.07.14.
 */
public class RegisterFragment extends Fragment implements NotifiableFragment<RegisterFragment.RegistrationParam> {

    public static String inputName = "";
    public static String inputMail = "";
    public static String inputPass1 = "";
    public static String inputPass2 = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerDialog(false);
    }

    private void agreeDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.agree_title));

        LinearLayout list = new LinearLayout(getActivity());
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        TextView notice = new TextView(getActivity());
        TextView tou = new TextView(getActivity());
        TextView privacyPolicy = new TextView(getActivity());

        notice.setText(getString(R.string.no_TOU_toast));

        list.addView(notice, layoutParams);

        alert.setView(list);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.agree,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        registerDialog(true);
                    }
                }
        );
        alert.show();
    }

    private void registerDialog(final boolean acceptedTos) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.registration_title));

        LinearLayout list = new LinearLayout(getActivity());
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText name = new EditText(getActivity());
        name.setHint(R.string.registration_name);
        name.setText(inputName);

        final EditText mail = new EditText(getActivity());
        mail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);
        mail.setText(inputMail);

        final EditText password = new EditText(getActivity());
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint(R.string.registration_password);
        password.setText(inputPass1);

        final EditText passwordCheck = new EditText(getActivity());
        passwordCheck.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordCheck.setHint(R.string.registration_repeat_password);
        passwordCheck.setText(inputPass2);

        String checkBoxText = getString(R.string.read_TOU_start) + " " +
                getString(R.string.TOU_link) + " " +
                getString(R.string.read_TOU_end);

        final CheckBox checkBox = new CheckBox(getActivity());
        //checkBox.setMovementMethod(LinkMovementMethod.getInstance());
        //checkBox.setClickable(true);
        //checkBox.setText(Html.fromHtml(checkBoxText));
        checkBox.setText(checkBoxText);

        Pattern pattern = Pattern.compile("Terms Of Use");
        Linkify.addLinks(checkBox, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return getString(R.string.TOU_url);
            }
        });


        if(acceptedTos) {
            checkBox.setError(getString(R.string.no_TOU_toast) + " " + getString(R.string.TOU_link));
        }

        list.addView(name, layoutParams);
        list.addView(mail, layoutParams);
        list.addView(password, layoutParams);
        list.addView(passwordCheck, layoutParams);
        list.addView(checkBox, layoutParams);

        alert.setView(list);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.registration_button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Grab the EditText's input
                        String inputName = name.getText().toString();
                        String inputMail = mail.getText().toString();
                        String inputPassword = password.getText().toString();
                        String inputPasswordCheck = passwordCheck.getText()
                                .toString();

                        RegisterFragment.inputName = inputName;
                        RegisterFragment.inputMail = inputMail;
                        RegisterFragment.inputPass1 = inputPassword;
                        RegisterFragment.inputPass2 = inputPasswordCheck;

                        if (password.getText().length() < 8) {
                            Toaster.getInstance().toast(R.string.password_too_short, Toast.LENGTH_LONG);
                            return;
                        }
                        if (!inputPassword.equals(inputPasswordCheck)) {
                            Log.d(getClass().getSimpleName(), "Password1##" + inputPassword + "##");
                            Log.d(getClass().getSimpleName(), "Password2##" + inputPasswordCheck + "##");
                            Toaster.getInstance().toast(R.string.passwords_do_not_match, Toast.LENGTH_LONG);
                            return;
                        }

                        if(checkBox.isChecked()) {
														AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
														Log.e("OOOOOOOO",name+" "+mail+" "+password+" "+passwordCheck+" "+checkBox);
														IBinder ib = null;
														InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
														boolean flag=false;
														View focus = activity.getCurrentFocus();
														if(null!=focus) ib=focus.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","focus: "+flag);
														if(null!=checkBox) ib=checkBox.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","checkBox: "+flag);
														if(null!=passwordCheck) ib=passwordCheck.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","passwordCheck: "+flag);
														if(null!=password) ib=password.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","password: "+flag);
														if(null!=mail) ib=mail.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","mail: "+flag);
														if(null!=name) ib=name.getWindowToken();
														if(null!=imm && null!=ib) flag=imm.hideSoftInputFromWindow(ib, 0);
														Log.e("O","name: "+flag);
														Log.e("OOOOOOOO",ib+"");
                            new UserRegistrationTask(RegisterFragment.class)
                                    .execute(inputName, inputMail, inputPassword, inputPasswordCheck,
                                            this.getClass().getName());
                        } else {
                            Toaster.getInstance().toast(R.string.no_TOU_toast, Toast.LENGTH_LONG);
                            registerDialog(true);
                        }
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

    public void onPostRegisterExecute(Boolean success, String email, String password, int message) {

        if (success) {
            //Login after registration was successfull
            Toaster.getInstance().toast(getResources().getString(
                    message), Toast.LENGTH_SHORT);
            UserLoginTask authTask = new UserLoginTask(false, LoginFragment.class);
            authTask.execute(email, password, this.getClass().getName());
            ((AbstractYasmeActivity) getActivity()).getSelfUser().setEmail(email);
        } else {
            Toaster.getInstance().toast(getResources().getString(message), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(), "Try to get LoginObservableInstance");
        FragmentObservable<RegisterFragment, RegistrationParam> obs =
                ObservableRegistry.getObservable(RegisterFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<RegisterFragment, RegistrationParam> obs =
                ObservableRegistry.getObservable(RegisterFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    @Override
    public void notifyFragment(RegistrationParam regParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        onPostRegisterExecute(regParam.getSuccess(), regParam.getEmail(), regParam.getPassword(), regParam.getMessage());
    }


    public static class RegistrationParam {
        protected Boolean success;
        private String email;
        private String password;
        private int message;

        public RegistrationParam(boolean success, String email, String password, int message) {
            this.success = success;
            this.email = email;
            this.password = password;
            this.message = message;
        }

        public Boolean getSuccess() {
            return success;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public int getMessage() {
            return message;
        }
    }
}
