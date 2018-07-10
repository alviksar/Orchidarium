package xyz.alviksar.orchidarium.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity {

    private static final Float TILE_WIDTH_INCHES = 1.0f;

    @BindView(R.id.sp_filter)
    Spinner mFilterSpinner;

    @BindView(R.id.rv_orchids)
    RecyclerView mRecyclerView;

    private FirebaseRecyclerAdapter mFirebaseAdapter;

    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_activity_main_2);
        ButterKnife.bind(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("orchids");
//        mRecyclerView = findViewById(R.id.rv_movies);
//        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
//        mErrorMessage = findViewById(R.id.tv_error_message);

        // Calculate the number of columns in the grid
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int mColumnWidthPixels;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.ydpi);

        } else {   // ORIENTATION_PORTRAIT
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.xdpi);

        }
        int columns = Math.max(1, metrics.widthPixels / mColumnWidthPixels);


        GridLayoutManager layoutManager =
                new GridLayoutManager(this, columns);
        // Create PosterAdapter with this context and this OnClickHandler

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        setUpFirebaseAdapter();

    }

    private void setUpFirebaseAdapter() {
        mFirebaseAdapter = new FirebaseRecyclerAdapter<OrchidEntity, OrchidViewHolder>
                (OrchidEntity.class, R.layout.orchid_list_item, OrchidViewHolder.class,
                        mDatabaseReference) {

            @NonNull
            @Override
            public OrchidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull OrchidViewHolder holder, int position, @NonNull OrchidEntity model) {
                holder.bindOrchid(model);
            }

        };
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFirebaseAdapter);
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
                // Getting Post failed, log a message
             // TODO:   Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);

        /*
        // TODO:
        https://www.learnhowtoprogram.com/android/data-persistence/firebase-recycleradapter
        https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
         */

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