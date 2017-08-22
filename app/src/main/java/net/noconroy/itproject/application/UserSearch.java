package net.noconroy.itproject.application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.matrix.androidsdk.rest.model.Search.SearchUsersResponse;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.util.Log;

public class UserSearch extends AppCompatActivity {

    private static final String LOG_TAG = "User Search";

    private Matrix matrix;
    private Context context;

    private EditText displaynameText;
    private Button displaynameButton;
    private EditText searchText;
    private Button searchButton;
    private ListView searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        matrix = Matrix.getInstance(this);
        context = this;

        displaynameText = (EditText) findViewById(R.id.displayname_text);
        displaynameButton = (Button) findViewById(R.id.displayname_button);
        searchText = (EditText) findViewById(R.id.search_text);
        searchButton = (Button) findViewById(R.id.search_button);
        searchResults = (ListView) findViewById(R.id.search_results);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Click here to log out", Snackbar.LENGTH_LONG)
                        .setAction("LOGOUT", new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                matrix.logout(new Callback<Void>() {
                                    @Override
                                    public void onGood(Void _) {
                                        Intent login = new Intent(context, Login.class);
                                        context.startActivity(login);
                                    }

                                    @Override
                                    public void onBad(Exception e) {
                                        Log.e(LOG_TAG, "Could not log out");
                                    }
                                });

                            }
                        }).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        displaynameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setname();
            }
        });

        ((TextView) findViewById(R.id.id_text)).setText(matrix.getSession().getMyUserId());
    }

    private void setname() {
        String n = displaynameText.getText().toString();
        matrix.getSession().getMyUser().updateDisplayName(n, new Callback<Void>() {

            @Override
            public void onGood(Void item) {
                Toast.makeText(UserSearch.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBad(Exception e) {
                Toast.makeText(UserSearch.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void search() {
        String q = searchText.getText().toString();
        matrix.getSession().searchUsers(q, 10, null, new Callback<SearchUsersResponse>() {

            @Override
            public void onBad(Exception e) {
                Log.e(LOG_TAG, "## logout() : error: " + e.getMessage());
            }

            @Override
            public void onGood(SearchUsersResponse searchUsersResponse) {
                ArrayAdapter<User> adapter = new ArrayAdapter<User>(context, android.R.layout.simple_list_item_1, searchUsersResponse.results) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup container) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, container, false);
                        }

                        User u = getItem(position);
                        ((TextView) convertView.findViewById(android.R.id.text1))
                                .setText(u.displayname + ": " + u.user_id);
                        return convertView;
                    }
                };
                searchResults.setAdapter(adapter);
            }
        });
    }
}
