package xyz.alviksar.orchidarium.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import xyz.alviksar.orchidarium.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_activity_main);
    }

    public void startDetailActivity(View view) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
    //    movieDetailIntent.putExtra(getString(R.string.movie_parcel_key), movie);
        startActivity(intent);    }
}