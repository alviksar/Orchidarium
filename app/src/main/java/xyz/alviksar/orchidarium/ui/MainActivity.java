package xyz.alviksar.orchidarium.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity {

    private static final Float TILE_WIDTH_INCHES = 1.0f;

    Spinner mFilterSpinner;

    @BindView(R.id.rv_orchids)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.tv_error_message)
    TextView mErrorMessage;

    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    private Parcelable mSavedRecyclerLayoutState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("orchids");

        // Calculate the number of columns in the grid
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int mColumnWidthPixels;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.ydpi);

        } else {   // ORIENTATION_PORTRAIT
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.xdpi);

        }
        int columns = Math.max(1, metrics.widthPixels / mColumnWidthPixels);

        /*
        https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
         */
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("orchids");
//                .limitToLast(50);

        FirebaseRecyclerOptions<OrchidEntity> options =
                new FirebaseRecyclerOptions.Builder<OrchidEntity>()
                        .setQuery(query, OrchidEntity.class)
                        .build();

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<OrchidEntity, OrchidViewHolder>(options) {
            @Override
            public OrchidViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_orchid, parent, false);

                return new OrchidViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(OrchidViewHolder holder, int position, @NonNull OrchidEntity model) {
                // Bind the OrchidEntity object to the OrchidViewHolder
                holder.bindOrchid(model);
            }

            @Override
            public void onDataChanged() {
                // Called each time there is a new data snapshot. You may want to use this method
                // to hide a loading spinner or check for the "no documents" state and update your UI.
                showData();
                if (mSavedRecyclerLayoutState != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
                }
            }

            @Override
            public void onError(DatabaseError e) {
                // Called when there is an error getting data. You may want to update
                // your UI to display an error message to the user.
                // ...
                showErrorMessage(R.string.msg_error_getting_data);
            }
        };
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
        GridLayoutManager layoutManager =
                new GridLayoutManager(this, columns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        // Check network connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            showLoading();
        } else {
            // Set no connection error message
            showErrorMessage(R.string.msg_no_connection_error);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();
    }

    private static final String BUNDLE_RECYCLER_LAYOUT = "MainActivity.mRecyclerView.layout";

    /**
     * https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state
     */
    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_RECYCLER_LAYOUT))
                mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*
        Set spinner into menu bar
        Thanks to Dércia Silva
        http://www.viralandroid.com/2016/03/how-to-add-spinner-dropdown-list-to-android-actionbar-toolbar.html
        */
        MenuItem item = menu.findItem(R.id.sp_filter);
        mFilterSpinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.maint_activity_menu_spinner_items, R.layout.menu_item_spinner);
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
    /**
     * This method will hide everything except the TextView error message
     * and set the appropriate text to it.
     */
    private void showErrorMessage(int msgResId) {
        mLoadingIndicator.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(msgResId);
    }

    /**
     * This method will make the loading indicator visible and hide the RecyclerView and error
     * message.
     */
    private void showLoading() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }
    /**
     * This method will make the RecyclerView visible and hide the error message and
     * loading indicator.
     */
    private void showData() {
        mLoadingIndicator.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}