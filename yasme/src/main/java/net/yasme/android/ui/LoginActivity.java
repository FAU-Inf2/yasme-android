package net.yasme.android.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import net.yasme.android.asyncTasks.DeviceRegistrationTask;
import net.yasme.android.asyncTasks.UserLoginTask;
import net.yasme.android.asyncTasks.UserRegistrationTask;
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
    private DeviceRegistrationTask devRegTask = null;
    private CloudMessaging cloudMessaging = null;

    protected String accessToken;

    // Values for name, email and password at the time of the login attempt.
    private String name;
    private String email;
    private String password;
    private long userId;

    private String deviceProduct;

    // focusView for validate()
    private View focusView = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;

    SharedPreferences storage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //GCM Begin
        cloudMessaging = CloudMessaging.getInstance(this);

        if (cloudMessaging.checkPlayServices()) {
            String regid = cloudMessaging.getRegistrationId();
            System.out.println("[DEBUG] Empty?" + regid.isEmpty());
            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(cloudMessaging.TAG, "No valid Google Play Services APK found.");
        }
        //GCM End

        // open storagePreferences
        // Restore preferencesNAME
        storage = getSharedPreferences(STORAGE_PREFS,
                MODE_PRIVATE);
        email = storage.getString(USER_MAIL, "");
        accessToken = storage.getString(ACCESSTOKEN, null);

        // Set up the login form.
        // email = getIntent().getStringExtra(USER_EMAIL);
        emailView = (EditText) findViewById(R.id.email);
        emailView.setText(email);

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

    // TODO: Strings nach strings.xml bringen
    private void registerDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter the values");

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText name = new EditText(this);
        name.setHint("Name");
        list.addView(name, layoutParams);
        final EditText mail = new EditText(this);
        mail.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        mail.setHint("E-Mail");
        list.addView(mail, layoutParams);
        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        password.setHint("Passwort");
        list.addView(password, layoutParams);
        final EditText password_check = new EditText(this);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        password_check.setHint("Passwort");
        list.addView(password_check, layoutParams);
        alert.setView(list);
        //TODO: Input type seems to change nothing??

        // "OK" button to save the values
        alert.setPositiveButton("Register now!",
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
        alert.setNegativeButton("Cancel",
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
        email = emailView.getText().toString();
        password = passwordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (password.length() < 4) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(email)) {
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
            authTask.execute(email, password);
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
    public boolean deviceCheck() {

        //set the deviceProduct
        this.deviceProduct = Build.MANUFACTURER + " " + Build.MODEL ;
        Log.d(this.getClass().getSimpleName(), "[DEBUG] MODEL is " + Build.MODEL);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] DEVICE is " + Build.DEVICE);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] PRODUCT is " + Build.PRODUCT);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] MANUFACTURER is " + Build.MANUFACTURER);
        Log.d(this.getClass().getSimpleName(), "[DEBUG] BRAND is " + Build.BRAND);

        Log.d(this.getClass().getSimpleName(), "[DEBUG] NOW is " + this.deviceProduct);
        //try to load device from shared preferences
        SharedPreferences prefs = getSharedPreferences(DEVICE_PREFS,
                MODE_PRIVATE);
        long deviceId = prefs.getLong(DEVICE_ID, -1);
        if (deviceId == -1) {
            return false;
        }
        // TODO devices from server
        // TODO use case : plain app + old user + old device

        Log.d(this.getClass().getSimpleName(), "[DEBUG] deviceId is" + deviceId);
        return true;
    }

    public void onPostDeviceRegExecute(Boolean success, long deviceId) {
        if (success) {
            Log.d(this.getClass().getSimpleName(), "[DEBUG] Login after device registration");
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        }
    }

    public void onPostLoginExecute(Boolean success, long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;

        showProgress(false);

        if (success) {
            // check if there is a device in the Database
            if (deviceCheck() == true) {
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device exists in Database");
                Intent intent = new Intent(this, ChatListActivity.class);
                startActivity(intent);
            } else {
                // TODO register device
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Device does not exist in Database");
                Log.d(this.getClass().getSimpleName(), "[DEBUG] Starting task to register device");
                devRegTask = new DeviceRegistrationTask(getApplicationContext(), storage, this);
                devRegTask.execute(this.accessToken, Long.toString(this.userId),this.deviceProduct);

            }
        } else {
            passwordView.setError(getString(R.string.error_incorrect_password));
            passwordView.requestFocus();
        }
    }

    public void onPostRegisterExecute(Boolean success, String email, String password) {
        this.email = email;
        this.password = password;

        showProgress(false);

        if (success) {
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
        editor.putString(USER_MAIL, email);
        editor.putLong(USER_ID, userId);
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