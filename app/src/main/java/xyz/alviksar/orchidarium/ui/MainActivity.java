package xyz.alviksar.orchidarium.ui;

import android.app.SearchManager;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity {

    private static final Float TILE_WIDTH_INCHES = 1.0f;

    @BindView(R.id.rv_orchids)
    RecyclerView mRecyclerView;
    // For saving state
    private static final String BUNDLE_RECYCLER_LAYOUT = "MainActivity.mRecyclerView.layout";

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.tv_error_message)
    TextView mErrorMessage;

    // Firebase
    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    private Query mQuery;

    private Parcelable mSavedRecyclerLayoutState = null;
    private String mSearchString;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_RECYCLER_LAYOUT))
                mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            if (savedInstanceState.containsKey("mSearchString"))
                mSearchString = savedInstanceState.getString("mSearchString");
        }


        // Calculate the number of columns in the grid
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int mColumnWidthPixels;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.ydpi);

        } else {   // ORIENTATION_PORTRAIT
            mColumnWidthPixels = Math.round(TILE_WIDTH_INCHES * metrics.xdpi);
        }
        int columns = Math.max(1, metrics.widthPixels / mColumnWidthPixels);

        // Check network connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            showLoading();
            handleSearchIntent(getIntent());
            GridLayoutManager layoutManager =
                    new GridLayoutManager(this, columns);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);
            // Start watching data
//            mFirebaseRecyclerAdapter.startListening();
        } else {
            // Set no connection error message
            showErrorMessage(R.string.msg_no_connection_error);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(Intent intent) {
        if (mFirebaseRecyclerAdapter != null)
            mFirebaseRecyclerAdapter.stopListening();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchString = intent.getStringExtra(SearchManager.QUERY);
            if (mSearchView != null)
                mSearchView.setQuery(mSearchString, false);
            Toast.makeText(this, "Search for'" + mSearchString + "' started.",
                    Toast.LENGTH_LONG).show();
            mQuery = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("orchids").orderByChild("name")
                    .startAt(mSearchString)
                    .endAt(mSearchString + "\uf8ff");
        } else {
            mQuery = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("orchids").orderByChild("saveTime");
        }

        /*
        https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
         */
        FirebaseRecyclerOptions<OrchidEntity> options =
                new FirebaseRecyclerOptions.Builder<OrchidEntity>()
                        .setQuery(mQuery, OrchidEntity.class)
                        .build();

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<OrchidEntity,
                OrchidViewHolder>(options) {
            @NonNull
            @Override
            public OrchidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the BannerAdapterViewHolder, in this case we are using
                // a custom layout called R.layout.list_item_orchid for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_orchid, parent, false);
                return new OrchidViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrchidViewHolder holder, int position,
                                            @NonNull OrchidEntity model) {
                // Bind the OrchidEntity object to the OrchidViewHolder
                String key = getRef(position).getKey();
                holder.bindOrchid(model, key);
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
                showErrorMessage(R.string.msg_error_getting_data);
            }

            // New ones at the top of the list
            // https://stackoverflow.com/questions/34156996/firebase-data-desc-sorting-in-android
            @NonNull
            @Override
            public OrchidEntity getItem(int position) {
                //  return super.getItem(position);
                return super.getItem(getItemCount() - (position + 1));
            }

        };
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
        mFirebaseRecyclerAdapter.startListening();

        // https://gist.github.com/Kishanjvaghela/67c42f8f32efaa2fadb682bc980e9280
    }

    @Override
    protected void onDestroy() {
        mFirebaseRecyclerAdapter.stopListening();
        super.onDestroy();
    }

    /**
     * https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state
     */
    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_RECYCLER_LAYOUT))
                mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            if (savedInstanceState.containsKey("mSearchString"))
                mSearchString = savedInstanceState.getString("mSearchString");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
        mSearchString = mSearchView.getQuery().toString();
        outState.putString("mSearchString", mSearchString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_filter).getActionView();
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQuery(mSearchString, true);
        mSearchView.clearFocus();

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                                           @Override
                                           public boolean onClose() {
                                               mSearchString = "";
                                               mSearchView.setQuery("", false);
                                               mSearchView.clearFocus();

                                               return false;
                                           }
                                       });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            case R.id.action_add_new:
                Intent intent = new Intent(MainActivity.this, StoreAdminActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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