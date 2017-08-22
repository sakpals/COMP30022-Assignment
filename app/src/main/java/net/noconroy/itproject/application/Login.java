package net.noconroy.itproject.application;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.matrix.androidsdk.rest.model.RoomResponse;
import org.matrix.androidsdk.rest.model.login.Credentials;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Matrix matrix;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        showProgress(false);

        matrix = Matrix.getInstance(this);
        if(matrix.getSession() != null) {
            Intent i = new Intent(this, UserSearch.class);
            startActivity(i);
        }
    }

    private class UserPass {
        public String username;
        public String password;
        public UserPass(String _u, String _p) {username = _u; password = _p;}
    }

    /**
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * @return UserPass
     */
    private UserPass getDetails() {
        if(matrix.isLoggingIn())
            return null;
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        // Check data is loaded
        View focusView = null;
        if (TextUtils.isEmpty(password) || password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
        }
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return null;
        }
        return new UserPass(username, password);
    }

    /**
     * To be executed on a successful auth. matrix session must be established
     */
    private void onSuccessfulAuth() {
        matrix.getSession().getRoomsApiClient().joinRoom("#public:itproject.noconroy.net", new Callback<RoomResponse>() {
            @Override
            public void onGood(RoomResponse item) {
                Toast.makeText(Login.this, "Joined public room", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBad(Exception e) {
                Toast.makeText(Login.this, "Failed to join public room: " + e, Toast.LENGTH_SHORT).show();
            }
        });
        Intent i = new Intent(this, UserSearch.class);
        startActivity(i);
    }

    private void attemptRegistration() {
        UserPass up = getDetails();
        if(up == null)
            return;

        showProgress(true);
        matrix.register(up.username, up.password, new Callback<Credentials>() {
            @Override
            public void onGood(Credentials item) {
                onSuccessfulAuth();
            }

            @Override
            public void onBad(Exception e) {
                Toast.makeText(Login.this, e.toString(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     */
    private void attemptLogin() {
        UserPass up = getDetails();
        if(up == null)
            return;

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        matrix.login(up.username, up.password, new Callback<Credentials>() {
            @Override
            public void onGood(Credentials item) {
                onSuccessfulAuth();
            }

            @Override
            public void onBad(Exception e) {
                Toast.makeText(Login.this, e.toString(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
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
}

