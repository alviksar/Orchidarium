package xyz.alviksar.orchidarium.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import xyz.alviksar.orchidarium.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Search %"+searchQuery+"% started.", Toast.LENGTH_LONG).show();

            //  doMySearch(query);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Search %"+searchQuery+"% started.", Toast.LENGTH_LONG).show();
            //  doMySearch(searchQuery);
        }

    }
}
