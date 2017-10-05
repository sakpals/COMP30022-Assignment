package net.noconroy.itproject.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.noconroy.itproject.application.Chat.ChatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void camera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void addFriend(View view){
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }

    public void startChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);

        // Change this in order to add the users id and name from server
        Bundle userClickedOn = new Bundle();
        userClickedOn.putString("id", "1");         // replace with proper id
        userClickedOn.putString("name", "bob");     // replace with proper name etc.
        intent.putExtras(userClickedOn);

        startActivity(intent);
    }
}
