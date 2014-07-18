package net.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.DeviceRegistrationTask;
import net.yasme.android.asyncTasks.server.UserLoginTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.activities.ChatListActivity;


public class LoginFragment extends Fragment implements NotifiableFragment<LoginFragment.LoginParam> {

    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask authTask = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;
    private Fragment spinner;

    // values for devices yasme server
    private String deviceProduct;

    // Values for name, email and password at the time of the login attempt.
    private String emailTmp;
    private String passwordTmp;

    // focusView for validate()
    private View focusView = null;

    protected String accessToken;
    protected AbstractYasmeActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();

        // Restore preferences
        emailTmp = activity.getStorage().getString(AbstractYasmeActivity.USER_MAIL, "@yasme.net");
        accessToken = activity.getAccessToken();

        spinner = new SpinnerFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        //ObserverRegistry.getRegistry(ObserverRegistry.Observers.LOGINFRAGMENT).register(this);
        Log.d(this.getClass().getSimpleName(), "Try to get LoginObservableInstance");
        FragmentObservable<LoginFragment, LoginParam> obs = ObservableRegistry.getObservable(LoginFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");

        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<LoginFragment, LoginParam> obs =
                ObservableRegistry.getObservable(LoginFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
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

        loginFormView = rootView.findViewById(R.id.login_form);
        loginStatusView = rootView.findViewById(R.id.login_status);
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

        return rootView;
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
            authTask = new UserLoginTask(activity.getStorage(), activity.getApplicationContext());
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
        } else if (passwordTmp.length() < 4) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid name.
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

            this.getView().setVisibility(View.GONE);
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, spinner).commit();
            //spinner.getView().setVisibility(View.VISIBLE);
            //spinner.onStart();

            authTask.execute(emailTmp, passwordTmp);
            authTask = null;
        }
    }

    public void onPostLoginExecute(Boolean success, long userId) {

        if (!ConnectionTask.isInitializedSession()) {
            ConnectionTask.initSession(userId, accessToken);
        }

        activity.getSelfUser().setId(userId);
        SharedPreferences.Editor editor = activity.getStorage().edit();
        //showProgress(false);
        activity.setSignedInFlag(success);
        editor.putBoolean(AbstractYasmeActivity.SIGN_IN, activity.getSignedInFlag());

        if (success) {
            //Initialize database (once in application)
            if (!DatabaseManager.INSTANCE.isInitialized()) {
                DatabaseManager.INSTANCE.init(activity.getApplicationContext(), activity.getStorage(), userId);
            }

            SharedPreferences devicePrefs = activity.getSharedPreferences(
                    AbstractYasmeActivity.DEVICE_PREFS,
                    AbstractYasmeActivity.MODE_PRIVATE);

            // check if there is a device in the Database
            if (yasmeDeviceCheck()) {
                Log.d(this.getClass().getSimpleName(), "Device exists in Database");

                long deviceId = devicePrefs.getLong(AbstractYasmeActivity.DEVICE_ID, -1);
                if (deviceId < 0) {
                    // Error ocurred
                    Log.e(this.getClass().getSimpleName(), "Could not load registered device's id from shared prefs");
                    return;
                }

                // Initialize the session a second time because the deviceId was missing
                ConnectionTask.initSession(userId, deviceId, accessToken);

                Intent intent = new Intent(activity, ChatListActivity.class);
                startActivity(intent);
            } else {
                // TODO register device
                Log.d(this.getClass().getSimpleName(), "Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "Starting task to register device at yasme server");

                new DeviceRegistrationTask(activity.getStorage(), activity)
                        .execute(Long.toString(userId), this.deviceProduct);

            }
            editor.commit();
        } else {
            passwordView.setError(getString(R.string.error_incorrect_user_or_password));
            passwordView.requestFocus();
            editor.commit();
        }
    }

    public void onPostYasmeDeviceRegExecute(Boolean success, long deviceId) {
        if (success) {

            // Initialize the session a second time because the deviceId was missing
            SharedPreferences devicePrefs = activity.getSharedPreferences(
                    AbstractYasmeActivity.DEVICE_PREFS,
                    AbstractYasmeActivity.MODE_PRIVATE);
            long userId = devicePrefs.getLong(AbstractYasmeActivity.USER_ID, -1);
            if (userId < 0) {
                // Error ocurred
                Log.e(this.getClass().getSimpleName(), "Did not find user id in shared prefs");
                return;
            }
            ConnectionTask.initSession(userId, deviceId, accessToken);

            Log.d(this.getClass().getSimpleName(), "Login after device registration at yasme server");
            Intent intent = new Intent(activity, ChatListActivity.class);
            startActivity(intent);
        }
    }

    /**
     * This method checks if there is a device in the DB
     */
    public boolean yasmeDeviceCheck() {

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
            return false;
        }
        // TODO devices from server
        // TODO use case : plain app + old user + old device

        Log.d(this.getClass().getSimpleName(), "deviceId is " + deviceId);
        return true;
    }

    @Override
    public void notifyFragment(LoginParam param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (param instanceof LoginProcessParam)
            notifyFragment((LoginProcessParam) param);
        else if (param instanceof DeviceRegistrationParam)
            notifyFragment((DeviceRegistrationParam) param);
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

        public LoginProcessParam(Boolean success, Long userId) {
            this.success = success;
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }
    }

    public static class DeviceRegistrationParam extends LoginParam {
        private Long deviceId;

        public DeviceRegistrationParam(Boolean success, Long deviceId) {
            this.success = success;
            this.deviceId = deviceId;
        }

        public Long getDeviceId() {
            return deviceId;
        }
    }
}

