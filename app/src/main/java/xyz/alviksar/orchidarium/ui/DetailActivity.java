package xyz.alviksar.orchidarium.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xyz.alviksar.orchidarium.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("Phragmipedium besseae");
    }
}
