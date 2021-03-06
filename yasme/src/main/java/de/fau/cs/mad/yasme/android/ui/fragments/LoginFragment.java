package de.fau.cs.mad.yasme.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import de.fau.cs.mad.yasme.android.BuildConfig;
import de.fau.cs.mad.yasme.android.EditTextWithX;
import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangePasswordTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.DeviceRegistrationTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UserLoginTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.encryption.PasswordEncryption;
import de.fau.cs.mad.yasme.android.entities.ServerInfo;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.DebugManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatListActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de>
 */

public class LoginFragment extends Fragment implements NotifiableFragment<LoginFragment.LoginParam> {

    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask authTask = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private TextView loginStatusMessageView;
    private View mProgressView;
    private View mLoginFormView;
    // values for devices yasme server
    private String deviceProduct;
    // Values for name, email and password at the time of the login attempt.
    private String emailTmp;
    private String passwordTmp;
    // focusView for validate()
    private View focusView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();

        // In debug app, show @yasme.net
        String defaultEmail = (BuildConfig.DEBUG) ? "@yasme.net" : "";

        // Restore preferences
        emailTmp = DatabaseManager.INSTANCE.getSharedPreferences().getString(AbstractYasmeActivity.USER_MAIL, defaultEmail);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container,
                false);

        // Set up the login form.
        emailView = (EditText) rootView.findViewById(R.id.email);
        emailView.setText(emailTmp);

        passwordView = (EditText) rootView.findViewById(R.id.password);
        if (BuildConfig.DEBUG) {
            passwordView.setText(R.string.default_password);
        }
        passwordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        loginStatusMessageView = (TextView) rootView.findViewById(R.id.login_status_message);

        rootView.findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "SignIn-Button pushed");
                        attemptLogin();
                    }
                }
        );

        rootView.findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "Register-Button pushed");
                        registerDialog();
                    }
                }
        );

        TextView tv = (TextView) rootView.findViewById(R.id.forgot_password);

        Pattern pattern = Pattern.compile(getString(R.string.forgot_password));
        /*
        Linkify.addLinks(tv, pattern, "", null, new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher matcher, String s) {
                return "";
            }
        });
        */
        tv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "Forgot Password-Button pushed");
                        requestMail();
                    }
                }
        );

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(), "Try to get LoginObservableInstance");
        FragmentObservable<LoginFragment, LoginParam> obs = ObservableRegistry.getObservable(LoginFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);
        mProgressView = getActivity().findViewById(R.id.login_status);
        mLoginFormView = getActivity().findViewById(R.id.login);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<LoginFragment, LoginParam> obs =
                ObservableRegistry.getObservable(LoginFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    private void registerDialog() {
        getFragmentManager().beginTransaction()
                .add(R.id.singleFragmentContainer, new RegisterFragment()).commit();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (missing fields, etc.), the errors are presented
     * and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (authTask == null) {
            authTask = new UserLoginTask(true, this.getClass());
            Log.d(this.getClass().getSimpleName(), "AuthTask is null");
        }

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        emailTmp = emailView.getText().toString();
        passwordTmp = passwordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password.
        if (TextUtils.isEmpty(passwordTmp)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (passwordTmp.length() < 8) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid mail.
        if (TextUtils.isEmpty(emailTmp)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            // Hide the virtual keyboard
            AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
            View focus = activity.getCurrentFocus();
            if (null == focus) {
                focus = focusView;
            }
            if (null == focus) {
                focus = passwordView;
            }
            if (null == focus) {
                focus = emailView;
            }
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (null != imm && null != focus)
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);

            // Start the asynctask
            authTask.execute(emailTmp, passwordTmp);
            authTask = null;
        }
    }

    public void onPostLoginExecute(Boolean success, long userId) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();

        activity.getSelfUser().setId(userId);

        if (success) {
            //Initialize database (once in application)
            if (!DatabaseManager.INSTANCE.isInitialized()) {
                Log.e(getClass().getSimpleName(), "DB-Manger hasn't been initialized");
            }
            DatabaseManager.INSTANCE.setUserId(userId);

            // check if there is a device in the Database
            if (yasmeDeviceCheck()) {
                Log.d(this.getClass().getSimpleName(), "Device exists in Database");

                long deviceId = DatabaseManager.INSTANCE.getSharedPreferences().getLong(AbstractYasmeActivity.DEVICE_ID, -1);
                if (deviceId < 0) {
                    // Error ocurred
                    Log.e(this.getClass().getSimpleName(), "Could not load registered device's id from shared prefs");
                    showProgress(false);
                    return;
                }

                DatabaseManager.INSTANCE.setDeviceId(deviceId);

                showProgress(false);
                Intent intent = new Intent(activity, ChatListActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {
                // register device
                Log.d(this.getClass().getSimpleName(), "Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "Starting task to register device at yasme server");

                new DeviceRegistrationTask(activity, this.getClass())
                        .execute(Long.toString(userId), this.deviceProduct, this.getClass().getName());

            }
        } else {
            Log.d(getClass().getSimpleName(), "Login failed");
            ServerInfo serverInfo = DatabaseManager.INSTANCE.getServerInfo();
            if (serverInfo != null && !serverInfo.getLoginAllowed() && serverInfo.hasMessage()) {
                passwordView.setError(DatabaseManager.INSTANCE.getServerInfo().getMessage());
            } else {
                passwordView.setError(getString(R.string.error_incorrect_user_or_password));
            }
            passwordView.requestFocus();
            showProgress(false);
        }
    }

    public void onPostYasmeDeviceRegExecute(Boolean success, long deviceId) {
        if (!success) {
            Toaster.getInstance().toast(getResources().getString(R.string.device_registration_failed), Toast.LENGTH_LONG);
        } else {
            AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
            // Initialize the session a second time because the deviceId was missing
            /*SharedPreferences devicePrefs = activity.getSharedPreferences(
                    AbstractYasmeActivity.DEVICE_PREFS,
                    AbstractYasmeActivity.MODE_PRIVATE);
            long userId = devicePrefs.getLong(AbstractYasmeActivity.USER_ID, -1);
            if (userId < 0) {
                // Error ocurred
                Log.e(this.getClass().getSimpleName(), "Did not find user id in shared prefs");
                Toaster.getInstance().toast(getResources().getString(R.string.device_registration_failed), Toast.LENGTH_LONG);
                showProgress(false);
                return;
            }*/

            DatabaseManager.INSTANCE.setDeviceId(deviceId);

            Log.d(this.getClass().getSimpleName(), "Login after device registration at yasme server");
            showProgress(false);
            Intent intent = new Intent(activity, ChatListActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    /**
     * This method checks if there is a device in the DB
     */
    public boolean yasmeDeviceCheck() {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        //set the deviceProduct
        this.deviceProduct = Build.MANUFACTURER + " " + Build.MODEL;
        Log.d(this.getClass().getSimpleName(), "MODEL is " + Build.MODEL);
        Log.d(this.getClass().getSimpleName(), "DEVICE is " + Build.DEVICE);
        Log.d(this.getClass().getSimpleName(), "PRODUCT is " + Build.PRODUCT);
        Log.d(this.getClass().getSimpleName(), "MANUFACTURER is " + Build.MANUFACTURER);
        Log.d(this.getClass().getSimpleName(), "BRAND is " + Build.BRAND);
        Log.d(this.getClass().getSimpleName(), "NOW is " + this.deviceProduct);
        //try to load device from shared preferences
        SharedPreferences devicePrefs = activity.getSharedPreferences(
                AbstractYasmeActivity.DEVICE_PREFS,
                AbstractYasmeActivity.MODE_PRIVATE);
        long deviceId = devicePrefs.getLong(AbstractYasmeActivity.DEVICE_ID, -1);

        // load regId
        SharedPreferences pushPrefs = activity.
                getSharedPreferences(AbstractYasmeActivity.PUSH_PREFS,
                        AbstractYasmeActivity.MODE_PRIVATE);

        String googleRegId = pushPrefs.getString(AbstractYasmeActivity.PROPERTY_REG_ID, null);
        // TODO proper check

        if (deviceId == -1) {
            if (DebugManager.INSTANCE.isDebugMode()) {
                return DebugManager.INSTANCE.restoreData();
            }
            return false;
        }
        // TODO devices from server
        // TODO use case : plain app + old user + old device

        Log.d(this.getClass().getSimpleName(), "deviceId is " + deviceId);
        return true;
    }

    public void requestMail() {
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

        final EditText mail = new EditTextWithX(activity).getEditText();
        mail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);
        mail.setText(emailTmp);

        list.addView(mail);
        list.addView(requestEmailText, layoutParams);

        alert.setView(list);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        emailTmp = mail.getText().toString();
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void notifyFragment(LoginParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (param instanceof LoginProcessParam) {
            notifyFragment((LoginProcessParam) param);
        } else if (param instanceof DeviceRegistrationParam) {
            notifyFragment((DeviceRegistrationParam) param);
        }
    }

    public void notifyFragment(LoginProcessParam loginParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified with loginParam");

        onPostLoginExecute(loginParam.getSuccess(), loginParam.getUserId());
        Log.d(super.getClass().getSimpleName(), "Login-Status: " + loginParam.getSuccess());
    }

    public void notifyFragment(DeviceRegistrationParam deviceRegistrationParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified with deviceRegistrationParam");

        onPostYasmeDeviceRegExecute(deviceRegistrationParam.getSuccess(),
                deviceRegistrationParam.getDeviceId());

    }

    public static class LoginParam {
        protected Boolean success;

        public Boolean getSuccess() {
            return success;
        }
    }

    public static class LoginProcessParam extends LoginParam {
        private Long userId;

        public LoginProcessParam(Boolean success) {
            this.success = success;
            this.userId = DatabaseManager.INSTANCE.getUserId();
        }

        public Long getUserId() {
            return userId;
        }
    }

    public static class DeviceRegistrationParam extends LoginParam {
        private Long deviceId;

        public DeviceRegistrationParam(Boolean success) {
            this.success = success;
            this.deviceId = DatabaseManager.INSTANCE.getDeviceId();
        }

        public Long getDeviceId() {
            return deviceId;
        }
    }
}

