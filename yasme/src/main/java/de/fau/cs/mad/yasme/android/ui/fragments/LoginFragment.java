package de.fau.cs.mad.yasme.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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

import de.fau.cs.mad.yasme.android.BuildConfig;
import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.DeviceRegistrationTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.UserLoginTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.ServerInfo;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.DebugManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatListActivity;


public class LoginFragment extends Fragment implements NotifiableFragment<LoginFragment.LoginParam> {

    protected String accessToken;
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

        // Restore preferences
        // In debug app, show @yasme.net
        String defaultEmail = (BuildConfig.DEBUG) ? "@yasme.net" : "";
        emailTmp = DatabaseManager.INSTANCE.getSharedPreferences().getString(AbstractYasmeActivity.USER_MAIL, defaultEmail);
        accessToken = activity.getAccessToken();
    }

    @Override
    public void onStart() {
        super.onStart();
        //ObserverRegistry.getRegistry(ObserverRegistry.Observers.LOGINFRAGMENT).register(this);
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
            authTask = new UserLoginTask(true);
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
            showProgress(true);

            authTask.execute(emailTmp, passwordTmp, this.getClass().getName());
            authTask = null;
        }
    }

    public void onPostLoginExecute(Boolean success, long userId) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        //if (!ConnectionTask.isInitializedSession()) {
        //    ConnectionTask.initSession(userId, accessToken);
        //}

        activity.getSelfUser().setId(userId);
        SharedPreferences.Editor editor = DatabaseManager.INSTANCE.getSharedPreferences().edit();
        //showProgress(false);
        activity.setSignedInFlag(success);
        editor.putBoolean(AbstractYasmeActivity.SIGN_IN, activity.getSignedInFlag());

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
                // TODO register device
                Log.d(this.getClass().getSimpleName(), "Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "Starting task to register device at yasme server");

                new DeviceRegistrationTask(activity)
                        .execute(Long.toString(userId), this.deviceProduct, this.getClass().getName());

            }
            editor.commit();
        } else {
            Log.d(getClass().getSimpleName(), "Login failed");
            ServerInfo serverInfo = DatabaseManager.INSTANCE.getServerInfo();
            if (serverInfo != null && serverInfo.hasMessage()) {
                passwordView.setError(DatabaseManager.INSTANCE.getServerInfo().getMessage());
            } else {
                passwordView.setError(getString(R.string.error_incorrect_user_or_password));
            }
            passwordView.requestFocus();
            editor.commit();
            showProgress(false);
        }
    }

    public void onPostYasmeDeviceRegExecute(Boolean success, long deviceId) {
        if (success) {
            AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
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

            //ConnectionTask.initSession(userId, deviceId, accessToken);
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

