package net.yasme.android.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import net.yasme.android.asyncTasks.UserLoginTask;
import net.yasme.android.asyncTasks.YasmeDeviceRegistrationTask;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.NotifyFragmentParameter;
import net.yasme.android.storage.DatabaseManager;


public class LoginFragment extends Fragment implements NotifiableFragment<NotifyFragmentParameter> {


    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask authTask = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;

    // values for devices google + yasme server
    private String deviceProduct;
    private String googleRegId;

    // Values for name, email and password at the time of the login attempt.
    private String emailTmp;
    private String passwordTmp;

    // focusView for validate()
    private View focusView = null;

    protected String accessToken;
    AbstractYasmeActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // open storagePreferences
        // Restore preferencesNAME
        emailTmp = activity.getSelfUser().getEmail();
        accessToken = activity.getAccessToken();

        // Set up the login form.
        emailView = (EditText) getView().findViewById(R.id.email);
        emailView.setText(emailTmp);

        passwordView = (EditText) getView().findViewById(R.id.password);
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

        loginFormView = getView().findViewById(R.id.login_form);
        loginStatusView = getView().findViewById(R.id.login_status);
        loginStatusMessageView = (TextView) getView().findViewById(R.id.login_status_message);

        getView().findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                }
        );

        getView().findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        registerDialog();
                    }
                }
        );
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
        if (authTask != null) {
            return;
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
            authTask = new UserLoginTask(activity.getStorage());
            authTask.execute(emailTmp, passwordTmp);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            loginStatusView.setVisibility(View.VISIBLE);
            loginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            loginFormView.setVisibility(View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void onPostLoginExecute(Boolean success, long userId, String accessToken) {
        activity.getSelfUser().setId(userId);
        SharedPreferences.Editor editor = activity.getStorage().edit();
        editor.putString(AbstractYasmeActivity.ACCESSTOKEN, accessToken);
        editor.commit();

        showProgress(false);

        if (success) {
            //Initialize database (once in application)
            if (!DatabaseManager.isInitialized()) {
                DatabaseManager.init(activity.getApplicationContext(), userId, accessToken);
            }
            // check if there is a device in the Database
            if (yasmeDeviceCheck() == true) {
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device exists in Database");
                Intent intent = new Intent(activity.getApplicationContext(), ChatListFragment.class);
                startActivity(intent);
            } else {
                // TODO register device
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Starting task to register device at yasme server");
                YasmeDeviceRegistrationTask yasmeDevRegTask =
                        new YasmeDeviceRegistrationTask(activity.getStorage());
                yasmeDevRegTask.execute(this.accessToken, Long.toString(userId),
                        this.deviceProduct, this.googleRegId);

            }
        } else {
            passwordView.setError(getString(R.string.error_incorrect_user_or_password));
            passwordView.requestFocus();
        }
    }

    public void onPostYasmeDeviceRegExecute(Boolean success, long deviceId) {
        if (success) {
            Log.d(this.getClass().getSimpleName(), "[DEBUG] Login after device registration at yasme server");
            Intent intent = new Intent(activity.getApplicationContext(), ChatListFragment.class);
            startActivity(intent);
        }
    }

    /**
     * This method checks if there is a device in the DB
     */
    public boolean yasmeDeviceCheck() {

        //set the deviceProduct
        this.deviceProduct = Build.MANUFACTURER + " " + Build.MODEL ;
        Log.d(this.getClass().getSimpleName(), "[DEBUG] MODEL is " + Build.MODEL);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] DEVICE is " + Build.DEVICE);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] PRODUCT is " + Build.PRODUCT);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] MANUFACTURER is " + Build.MANUFACTURER);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] BRAND is " + Build.BRAND);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] NOW is " + this.deviceProduct);
        //try to load device from shared preferences
        SharedPreferences devicePrefs = activity.getSharedPreferences(
                AbstractYasmeActivity.DEVICE_PREFS,
                AbstractYasmeActivity.MODE_PRIVATE);
        long deviceId = devicePrefs.getLong(AbstractYasmeActivity.DEVICE_ID, -1);

        // load regId
        //TODO: SharedPrefs umbenennen und als String in der AbstractYasmeActivity speichern
        SharedPreferences pushPrefs = activity.
                getSharedPreferences(LoginActivity.class.getSimpleName(),
                        AbstractYasmeActivity.MODE_PRIVATE);

        String regId = pushPrefs.getString(AbstractYasmeActivity.PROPERTY_REG_ID,null);
        this.googleRegId = regId;
        // TODO proper check

        if (deviceId == -1) {
            return false;
        }
        // TODO devices from server
        // TODO use case : plain app + old user + old device

        Log.d(this.getClass().getSimpleName(), "[DEBUG] deviceId is " + deviceId);
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container,
                false);
        return rootView;
    }

    @Override
    public void notifyFragment(NotifyFragmentParameter param) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        LoginParam loginParam = ((LoginParam)param);
    }


    public static class LoginParam implements NotifyFragmentParameter {
        private Boolean success;
        private Long userId;
        private String accessToken;

        public LoginParam(Boolean success, Long userId, String accessToken) {
            this.success = success;
            this.userId = userId;
            this.accessToken = accessToken;
        }

        public Long getUserId() {
            return userId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public Boolean getSuccess() {
            return success;
        }
    }
}

