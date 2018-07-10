package xyz.alviksar.orchidarium.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.sp_filter)
    Spinner mFilterSpinner;

    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_activity_main_2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("orchids");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                OrchidEntity orchid = dataSnapshot.getValue(OrchidEntity.class);
                // TODO: mOrchidAdapter.add(orchid);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);

        /*
        Set spinner into menu bar
        Thanks to Dércia Silva
        http://www.viralandroid.com/2016/03/how-to-add-spinner-dropdown-list-to-android-actionbar-toolbar.html
        */
        MenuItem item = menu.findItem(R.id.sp_filter);
        mFilterSpinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.maint_activity_menu_spinner_items, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilterSpinner.setAdapter(adapter);

        // Set spinner to the right state
        int defPosition = adapter.getPosition(OrchidariumPreferences.getMode(this));
        mFilterSpinner.setSelection(defPosition);

        mFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (mFilterSpinner.getSelectedItem() != null) {
                //    String mode = (String) mSpinner.getSelectedItem();
               //     int position = mSpinner.getSelectedItemPosition();
                 //   OrchidariumPreferences.setMode(getApplicationContext(), mode);
                    // TODO: updateList(mode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        } else if (id == R.id.action_add_new) {
            Intent intent = new Intent(MainActivity.this, StoreAdminActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startDetailActivity(View view) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        //    movieDetailIntent.putExtra(getString(R.string.movie_parcel_key), movie);
        startActivity(intent);
    }


}