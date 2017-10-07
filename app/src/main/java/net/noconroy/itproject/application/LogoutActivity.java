package net.noconroy.itproject.application;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LogoutActivity extends AppCompatActivity {

    private String access_token;
    private LogoutActivity.LogoutTask mTask = null;

    // UI reference
    private ProgressBar mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        Intent i = getIntent();
        this.access_token = i.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);

        if(access_token != null) {
            mLoading = (ProgressBar) findViewById(R.id.progressBar);
            mLoading.setVisibility(View.VISIBLE);
            logout();
        }
    }


    public void goBackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goBackToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void logout() {
        if(mTask != null) {
            return;
        }
        else {
            mTask = new LogoutTask();
            mTask.execute((Void) null);
        }
    }

    public class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        private String mLogoutRequest;
        public LogoutTask() {
            // do nothing
        }

        @Override
        protected  Boolean doInBackground(Void... params) {
            mLogoutRequest = NetworkHelper.Logout(access_token);
            if(mLogoutRequest.equals("200")) {
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;
            if(success) {
                // go back to login page
                goBackToLoginActivity();
            }
            else {
                // else go back to main activity
                runOnUiThread(logout_error);
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
        }
    }

    private Runnable logout_error = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Logout failed.", Toast.LENGTH_SHORT).show();
            goBackToMainActivity();
        }
    };
}
