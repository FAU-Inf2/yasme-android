package net.yasme.android;

import net.yasme.android.entities.User;
import net.yasme.android.connection.UserTask;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

	public final static String USER_NAME = "net.yasme.andriod.USER_NAME";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask authTask = null;

	protected String url = null;
	protected String id;
	protected String accessToken;

	// Values for name, email and password at the time of the login attempt.
	private String name;
	private String email;
	private String password;

	// focusView for validate()
	private View focusView = null;

	// UI references.
	private EditText nameView;
	private EditText passwordView;
	private View loginFormView;
	private View loginStatusView;
	private TextView loginStatusMessageView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// get URL
		url = getResources().getString(R.string.server_url);

		// Set up the login form.
		name = getIntent().getStringExtra(USER_NAME);
		nameView = (EditText) findViewById(R.id.name);
		nameView.setText(name);

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
						// TODO: register-Methode
						// zusätzliche email-View erzeugen
						//newView();
						attemptLogin();
					}
				});
	}
	
	private void newView() {
		LinearLayout neu = new LinearLayout(this);
		LinearLayout currentLayout = (LinearLayout) findViewById(R.layout.activity_login);
        
        TextView emailView = new TextView(this);
        neu.addView(emailView);
        currentLayout.addView(neu);
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
		focusView = null;

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
		nameView.setError(null);
		passwordView.setError(null);

		// Store values at the time of the login attempt.
		name = nameView.getText().toString();
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
		if (TextUtils.isEmpty(name)) {
			nameView.setError(getString(R.string.error_field_required));
			focusView = nameView;
			cancel = true;
		}
		return cancel;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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
		id = name;
		intent.putExtra(USER_ID, id);
		intent.putExtra(USER_NAME, name);
		startActivity(intent);
	}

	/**
	 * Represents an asynchronous task used to register the user.
	 */
	public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: ueberpruefen, ob user schon existiert

			id = new UserTask(url)
					.registerUser(new User(password, name, email));
			if (id == null) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			authTask = null;
			showProgress(false);

			// TODO: komplette Eingabe der Anmeldedaten eines neuen Users
			/*
			 * Intent intent = new Intent(); intent.putExtra(USER_NAME, name);
			 * startActivity(intent);
			 */

			if (success) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.registration_successful),
						Toast.LENGTH_SHORT).show();
			} else {
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
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			//accessToken = new UserTask(url).loginUser(new User(password, name));

			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			authTask = null;
			showProgress(false);

			start();

			if (success) {
				finish();
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
}