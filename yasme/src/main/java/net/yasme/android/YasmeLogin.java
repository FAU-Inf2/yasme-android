package net.yasme.android;

import net.yasme.android.connection.AuthorizationTask;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.connection.UserTask;
import net.yasme.android.storage.DatabaseManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class YasmeLogin extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;

    protected long id;
    protected String[] loginReturn = new String[2];
    protected String accessToken;

    // Values for name, email and password at the time of the login attempt.
    private String name;
    private String email;
    private String password;
    private long userId;

    // focusView for validate()
    private View focusView = null;

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!ConnectionTask.isInitialized()) {
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),getResources().getString(R.string.server_host),getResources().getString(R.string.server_port));
        }

        //Initialize database (once in application)
        if(!DatabaseManager.isInitialized()) {
            DatabaseManager.init(this);
        }

        // open storagePreferences
        // Restore preferencesNAME
        SharedPreferences storage = getSharedPreferences(Constants.STORAGE_PREFS,
                MODE_PRIVATE);
        email = storage.getString(Constants.USER_MAIL, "");
        loginReturn[1] = storage.getString("accesToken1", null);

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
                });

        findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        registerDialog();
                    }
                });
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
                        new UserRegistrationTask().execute(inputName,
                                inputMail, inputPassword, inputPasswordCheck);
                    }
                });

        // "Cancel" button
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.yasme_login, menu);
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

        boolean cancel = validate();

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
            authTask = new UserLoginTask();
            authTask.execute((Void) null);
        }
    }

    private boolean validate() {
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
        return cancel;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
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

    public void start() {
        Intent intent = new Intent(this, YasmeHome.class);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous task used to register the user.
     *
     * @params params[0] is name
     * @params params[1] is email
     * @params params[2] is password
     * @params params[3] is password_check
     */
    public class UserRegistrationTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: ueberpruefen, ob user schon existiert
            String name = params[0];
            String email = params[1];
            String password = params[2];
            String password_check = params[3];

            if (!password.equals(password_check)) {
                return false;
            }
            try {
                id = UserTask.getInstance().registerUser(new User(password, name,
                        email));
            } catch (RestServiceException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.registration_successful),
                        Toast.LENGTH_SHORT).show();
                start();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.registration_not_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {

            try {
                // DEBUG:
                System.out.println("e-Mail: " + email + " " + "Passwort: "
                        + password);

                loginReturn = AuthorizationTask.getInstance().loginUser(new User(email,
                        password));

                userId = Long.parseLong(loginReturn[0]);
                accessToken = loginReturn[1];

                System.out.println(loginReturn[0]);
                // accessToken storage
                SharedPreferences storage = getSharedPreferences(Constants.STORAGE_PREFS,
                        MODE_PRIVATE);
                SharedPreferences.Editor editor = storage.edit();
                editor.putLong(Constants.USER_ID, Long.parseLong(loginReturn[0]));
                editor.putString("accesToken", loginReturn[1]);
                editor.putString(Constants.USER_MAIL, email);
                editor.commit();

            } catch (RestServiceException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            showProgress(false);
            if (success) {
                new UpdateDBTask().execute();
                start();
            } else {
                passwordView
                        .setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }

    public class UpdateDBTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {
            ChatTask chatTask = ChatTask.getInstance();
            DatabaseManager dbManager = DatabaseManager.getInstance();

            int numberOfChats = 16;//dbManager.getNumberOfChats();

            Chat chat;
            for(int i = 0; i < numberOfChats; i++) {

                try {
                    chat = chatTask.getInfoOfChat(i, userId, accessToken);
                } catch (RestServiceException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                dbManager.updateChat(chat);
            }
            return true;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences storage = getSharedPreferences(Constants.STORAGE_PREFS,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(Constants.USER_MAIL, email);
        editor.putLong(Constants.USER_ID, id);
        editor.putString("accesToken1", loginReturn[1]);

        // Commit the edits!
        editor.commit();
    }
}