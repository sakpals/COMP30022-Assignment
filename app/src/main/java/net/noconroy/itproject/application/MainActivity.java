package net.noconroy.itproject.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private String access_token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String access_token = intent.getStringExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE);
        this.access_token = access_token;
        if (access_token != null){

            // Hide register button
            Button registerButton = (Button) findViewById(R.id.Registerbutton);
            registerButton.setVisibility(View.GONE);

            // Change text of Login button
            Button loginButton = (Button) findViewById(R.id.LoginButton);
            loginButton.setText("Logged in");

            // Disable logging in, as you're already logged
            loginButton.setEnabled(false);

            Button friendsButton = (Button) findViewById(R.id.FriendsButton);
            friendsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friends(view);
                }
            });
        }
    }

    public void register(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void camera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void friends(View view) {
        Intent intent = new Intent(MainActivity.this, Friends.class);
        intent.putExtra(RegisterActivity.ACCESS_TOKEN_MESSAGE, access_token);
        startActivity(intent);
    }

}
