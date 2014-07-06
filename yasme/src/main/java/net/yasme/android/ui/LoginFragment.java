package net.yasme.android.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.NotifyFragmentParameter;


public class LoginFragment extends Fragment implements NotifiableFragment<NotifyFragmentParameter> {

    // UI references.
    private EditText emailView;
    private EditText passwordView;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusMessageView;

    // Values for name, email and password at the time of the login attempt.
    private String emailTmp;
    private String passwordTmp;

    AbstractYasmeActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences storage = activity.getStorage();

        // open storagePreferences
        // Restore preferencesNAME
        emailTmp = activity.getSelfUser().getEmail();
        accessToken = activity.getAccessToken();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatlist, container,
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

