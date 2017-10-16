package net.noconroy.itproject.application;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.noconroy.itproject.application.callbacks.AuthenticationCallback;

/**
 * A login screen that offers login via email/password.
 */
public class HomeActivity extends AppCompatActivity {

    public static final String AT_PREFS = "AccessTokenPrefs";
    public static final String AT_PREFS_KEY = "AccessTokenPrefs_KEY";

    // Error messages
    private static final String FAILED_REGISTER_LOGIN_MESSAGE = "Register/Sign In Unsuccessful";

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLogin();

        setContentView(R.layout.activity_home);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

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

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void authSuccessful(String access_token) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.ACCESS_TOKEN_INTENT_KEY, access_token);
        startActivity(intent);
        finish();
    }

    private void attemptAuthentication(final String username, final String password) {

        final AuthenticationCallback registerCallback = new AuthenticationCallback(this) {
            @Override
            public void onAuthenticated(String access_token) {
                SharedPreferences settings = getSharedPreferences(AT_PREFS, 0);
                settings.edit().putString(AT_PREFS_KEY, access_token).commit();
                authSuccessful(access_token);
            }

            @Override
            public void onFailure(Failure f) {
                showProgress(false);
                Toast t = Toast.makeText(getApplicationContext(),
                        FAILED_REGISTER_LOGIN_MESSAGE, Toast.LENGTH_SHORT);
                t.show();
            }
        };

        AuthenticationCallback loginCallback = new AuthenticationCallback(this) {
            @Override
            public void onAuthenticated(String access_token) {
                SharedPreferences settings = getSharedPreferences(AT_PREFS, 0);
                settings.edit().putString(AT_PREFS_KEY, access_token).commit();
                authSuccessful(access_token);
            }

            @Override
            public void onFailure(Failure f) {
                NetworkHelper.Register(username, password, "", "", registerCallback);
            }
        };

        NetworkHelper.Login(username, password, loginCallback);

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            attemptAuthentication(username, password);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    private void checkLogin() {
        SharedPreferences settings = getSharedPreferences(AT_PREFS, 0);
        String access_token = settings.getString(AT_PREFS_KEY, "");
        if(!access_token.equals("")) authSuccessful(access_token);
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

