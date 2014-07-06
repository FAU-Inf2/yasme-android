package net.yasme.android.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.YasmeDeviceRegistrationTask;
import net.yasme.android.asyncTasks.UserLoginTask;
import net.yasme.android.asyncTasks.UserRegistrationTask;
import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.gcm.CloudMessaging;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AbstractYasmeActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;
    private UserRegistrationTask regTask = null;
    private YasmeDeviceRegistrationTask yasmeDevRegTask = null;
    private CloudMessaging cloudMessaging = null;

    protected String accessToken;

    // Values for name, email and password at the time of the login attempt.
    private String emailTmp;
    private String passwordTmp;
    private long userIdTmp;

    // values for devices google + yasme server
    private String deviceProduct;
    private String googleRegId;

    // focusView for validate()
    private View focusView = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //temporäre Lösung:
        HttpClient.context = this.getApplicationContext();


        //GCM Begin
        cloudMessaging = CloudMessaging.getInstance(this);

        if (cloudMessaging.checkPlayServices()) {
            String regid = cloudMessaging.getRegistrationId();
            System.out.println("[DEBUG] Empty?" + regid.isEmpty());
            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(AbstractYasmeActivity.TAG, "No valid Google Play Services APK found.");
        }
        //GCM End

        // open storagePreferences
        // Restore preferencesNAME
        storage = getSharedPreferences(STORAGE_PREFS,
                MODE_PRIVATE);
        emailTmp = getSelfUser().getEmail();
        accessToken = getAccessToken();

        // Set up the login form.
        // email = getIntent().getStringExtra(USER_EMAIL);
        emailView = (EditText) findViewById(R.id.email);
        emailView.setText(emailTmp);

        passwordView = (EditText) findViewById(R.id.password);
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

        loginFormView = findViewById(R.id.login_form);
        loginStatusView = findViewById(R.id.login_status);
        loginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                }
        );

        findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        registerDialog();
                    }
                }
        );

        regTask = new UserRegistrationTask(getApplicationContext(), storage, this);
    }

    private void registerDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.registration_title));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText name = new EditText(this);
        name.setHint(R.string.registration_name);
        list.addView(name, layoutParams);
        final EditText mail = new EditText(this);
        mail.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        mail.setHint(R.string.registration_email);
        list.addView(mail, layoutParams);
        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        password.setHint(R.string.registration_password);
        list.addView(password, layoutParams);
        final EditText password_check = new EditText(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
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

        //focusView = null; //Edit by Flo

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            authTask = new UserLoginTask(getApplicationContext(), storage, this);
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

    /*
    * This method checks if there is a device in the DB
    * */
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
        SharedPreferences devicePrefs = getSharedPreferences(DEVICE_PREFS,
                MODE_PRIVATE);
        long deviceId = devicePrefs.getLong(DEVICE_ID, -1);

        // load regId
        //TODO: SharedPrefs umbenennen und als String in der AbstractYasmeActivity speichern
        SharedPreferences pushPrefs = getSharedPreferences(LoginActivity.class.getSimpleName(), MODE_PRIVATE);

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

    public void onPostYasmeDeviceRegExecute(Boolean success, long deviceId) {
        if (success) {
            Log.d(this.getClass().getSimpleName(), "[DEBUG] Login after device registration at yasme server");
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        }
    }

    public void onPostLoginExecute(Boolean success, long userId, String accessToken) {
        getSelfUser().setId(userId);
        SharedPreferences.Editor editor = getStorage().edit();
        editor.putString(AbstractYasmeActivity.ACCESSTOKEN, accessToken);
        editor.commit();

        showProgress(false);

        if (success) {
            // check if there is a device in the Database
            if (yasmeDeviceCheck() == true) {
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device exists in Database");
                Intent intent = new Intent(this, ChatListActivity.class);
                startActivity(intent);
            } else {
                // TODO register device
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Starting task to register device at yasme server");
                yasmeDevRegTask = new YasmeDeviceRegistrationTask(storage, this);
                yasmeDevRegTask.execute(this.accessToken, Long.toString(userId),this.deviceProduct,this.googleRegId);

            }
        } else {
            passwordView.setError(getString(R.string.error_incorrect_user_or_password));
            passwordView.requestFocus();
        }
    }

    public void onPostRegisterExecute(Boolean success, String email, String password) {

        showProgress(false);

        if (success) {
            getSelfUser().setEmail(email);
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(
                            R.string.registration_successful),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(
                            R.string.registration_not_successful),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cloudMessaging.checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences storage = getSharedPreferences(STORAGE_PREFS,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(USER_MAIL, getSelfUser().getEmail());
        editor.putLong(USER_ID, getSelfUser().getId());
        editor.putString(ACCESSTOKEN, accessToken);

        // Commit the edits!
        editor.commit();
    }

    public void registerInBackground() {

        new AsyncTask<Void, Void, String>() {

            protected String doInBackground(Void[] params) {
                return cloudMessaging.registerInBackground();
            }

            protected void onPostExecute(String msg) {
                //Zu diesem Zeitpunkt ist die RegId bereits als SharedPref.
                // in AbstractYasmeActivity.PROPERTY_REG_ID abgelegt.
                System.out.println(msg);

            }

        }.execute(null, null, null);
    }
}