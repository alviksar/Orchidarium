package xyz.alviksar.orchidarium.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import xyz.alviksar.orchidarium.BuildConfig;
import xyz.alviksar.orchidarium.OrchidariumContract;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * List of orchid photos
 */
public class MainActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final Float TILE_WIDTH_INCHES = 1.15f;

    @BindView(R.id.rv_orchids)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.tv_error_message)
    TextView mErrorMessage;

    @BindView(R.id.btn_order)
    FloatingActionButton mMakeOrderButton;

    SearchView mSearchView;

    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;

    // Shopping cart
    ArrayList<String> mCart;

    // Vars for current state
    private String mSearchQuery = null;
    private boolean mHiddenOnly = false;
    private Parcelable mSavedRecyclerLayoutState = null;

    // Consts for saving curent state
    private static final String BUNDLE_SEARCH_QUERY = "MainActivity.mSearchQuery";
    private static final String BUNDLE_HIDDEN_ONLY = "MainActivity.mHiddenOnly";
    private static final String BUNDLE_RECYCLER_LAYOUT = "MainActivity.mRecyclerView.layout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_RECYCLER_LAYOUT))
                mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            if (savedInstanceState.containsKey(BUNDLE_SEARCH_QUERY))
                mSearchQuery = savedInstanceState.getString(BUNDLE_SEARCH_QUERY);
            if (savedInstanceState.containsKey(BUNDLE_HIDDEN_ONLY))
                mHiddenOnly = savedInstanceState.getBoolean(BUNDLE_HIDDEN_ONLY);
        }

        // Check network connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Start watching data
            setRecyclerAdapter(mSearchQuery);
        } else {
            // Set no connection error message
            showErrorMessage(R.string.msg_no_connection_error);
        }

        mCart = OrchidariumPreferences.getCartContent(this);

        if (BuildConfig.FLAVOR.equals("admin")) {
            mMakeOrderButton.setVisibility(View.GONE);
        } else {
            if (mCart.isEmpty()) {
                mMakeOrderButton.setVisibility(View.GONE);
            } else {
                mMakeOrderButton.setVisibility(View.VISIBLE);
            }
        }

        // Subscribe to notification
        if (OrchidariumPreferences.isNotificationOn(this)) {
            FirebaseMessaging
                    .getInstance().subscribeToTopic(OrchidariumContract.NOTIFICATION_TOPIC);
        } else {
            FirebaseMessaging
                    .getInstance().unsubscribeFromTopic(OrchidariumContract.NOTIFICATION_TOPIC);
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }


    private void setRecyclerAdapter(String searchQuery) {
        if (mFirebaseRecyclerAdapter != null)
            mFirebaseRecyclerAdapter.stopListening();
        showLoading();
        Timber.i("Search for '" + searchQuery + "' started.");
        Query query;
        if (BuildConfig.FLAVOR.equals("admin")) {
            if (mHiddenOnly) {
                // Show only invisible to customers
                query = FirebaseDatabase.getInstance()
                        .getReference()
                        .child(OrchidariumContract.REFERENCE_ORCHIDS_DATA)
                        .orderByChild(OrchidariumContract.FIELD_ISVISIBLEFORSALE)
                        .equalTo(false);
            } else {
                if (TextUtils.isEmpty(searchQuery)) {
                    // Show all orchids
                    query = FirebaseDatabase.getInstance()
                            .getReference()
                            .child(OrchidariumContract.REFERENCE_ORCHIDS_DATA)
                            .orderByChild(OrchidariumContract.FIELD_FORSALETIME);
                } else {
                    // Show only that names start with searchQuery string
                    query = FirebaseDatabase.getInstance()
                            .getReference()
                            .child(OrchidariumContract.REFERENCE_ORCHIDS_DATA)
                            .orderByChild(OrchidariumContract.FIELD_NAME)
                            .startAt(searchQuery)
                            .endAt(searchQuery + "\uf8ff");
                }
            }
        } else {
            // Show only visible for customers
            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(OrchidariumContract.REFERENCE_ORCHIDS_DATA)
                    .orderByChild(OrchidariumContract.FIELD_ISVISIBLEFORSALE)
                    .equalTo(true);
        }
        /*
        https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
         */
        FirebaseRecyclerOptions<OrchidEntity> options =
                new FirebaseRecyclerOptions.Builder<OrchidEntity>()
                        .setQuery(query, OrchidEntity.class)
                        .build();

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<OrchidEntity,
                OrchidViewHolder>(options) {
            @NonNull
            @Override
            public OrchidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the OrchidViewHolder, in this case we are using
                // a custom layout called R.layout.list_item_orchid for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_orchid, parent, false);

                return new OrchidViewHolder(view, MainActivity.this);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrchidViewHolder holder, int position,
                                            @NonNull OrchidEntity model) {
                // Bind the OrchidEntity object to the OrchidViewHolder
                String key = getRef(position).getKey();

                if (BuildConfig.FLAVOR.equals("user")) {
                    holder.bindOrchid(model, key, mCart.contains(key));
                } else {
                    holder.bindOrchid(model, key, false);
                }
            }

            @Override
            public void onDataChanged() {
                // Called each time there is a new data snapshot
                showData();
                if (mSavedRecyclerLayoutState != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
                }
            }

            @Override
            public void onError(@NonNull DatabaseError e) {
                showErrorMessage(R.string.msg_error_getting_data);
            }
        };

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
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);

        mFirebaseRecyclerAdapter.startListening();
        invalidateOptionsMenu();

    }

    @Override
    protected void onDestroy() {
        if (mFirebaseRecyclerAdapter != null)
            mFirebaseRecyclerAdapter.stopListening();
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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
            if (savedInstanceState.containsKey(BUNDLE_SEARCH_QUERY))
                mSearchQuery = savedInstanceState.getString(BUNDLE_SEARCH_QUERY);
            if (savedInstanceState.containsKey(BUNDLE_HIDDEN_ONLY))
                mHiddenOnly = savedInstanceState.getBoolean(BUNDLE_HIDDEN_ONLY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null)
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT,
                    mRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString(BUNDLE_SEARCH_QUERY, mSearchQuery);
        outState.putBoolean(BUNDLE_HIDDEN_ONLY, mHiddenOnly);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQuery(mSearchQuery, false);
        mSearchView.clearFocus();

        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.setQuery(mSearchQuery, false);
                mSearchView.clearFocus();
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchQuery = "";
                setRecyclerAdapter(mSearchQuery);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (BuildConfig.FLAVOR.equals("user")) {
            menu.findItem(R.id.action_add_new).setVisible(false);
            menu.findItem(R.id.action_show_hidden).setVisible(false);
            menu.findItem(R.id.action_sign_out).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        }
        if (BuildConfig.FLAVOR.equals("admin")) {
            menu.findItem(R.id.action_search).setVisible(true);
            if (mHiddenOnly) {
                setTitle(getString(R.string.title_invisible_to_customers));
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_show_hidden).setIcon(R.drawable.ic_visibility_off_gold_24dp);
            } else {
                setTitle(getString(R.string.app_name));
            }
        }
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
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_show_hidden:
                mHiddenOnly = !mHiddenOnly;
                setRecyclerAdapter(mSearchQuery);
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

    @Override
    public boolean onQueryTextSubmit(String searchQuery) {
        setRecyclerAdapter(searchQuery);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchQuery = newText;
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(OrchidariumPreferences.PREF_CONTENTS_OF_THE_CART)) {
            mCart = OrchidariumPreferences.getCartContent(this);
            mFirebaseRecyclerAdapter.notifyDataSetChanged();
            if (BuildConfig.FLAVOR.equals("user")) {
                if (mCart.isEmpty()) {
                    mMakeOrderButton.setVisibility(View.GONE);
                } else {
                    mMakeOrderButton.setVisibility(View.VISIBLE);
                }
            }
        }
        invalidateOptionsMenu();
    }

    /**
     * Makes email to order chosen orchids.
     *
     * @param orchidList List of orchids for ordering.
     */
    public void composePurchaseOrder(ArrayList<OrchidEntity> orchidList) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.order_address)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_subject));

        StringBuilder body =
                new StringBuilder();
        body.append(getString(R.string.order_disclaimer));
        body.append(getString(R.string.order_mail_body));

        int n = 1;
        for (OrchidEntity orchid : orchidList) {
            body.append(String.format(Locale.getDefault(), "\n%d.[%8s] %s",
                    n, orchid.getCode(), orchid.getName()));
            n++;
        }

        intent.putExtra(Intent.EXTRA_TEXT, body.toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.btn_order)
    public void onClickOrderButton(View view) {
        orderByEmail();
    }

    /**
     * Collects chosen orchids and makes an order
     */
    public void orderByEmail() {
        final ArrayList<OrchidEntity> orchidList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child(OrchidariumContract.REFERENCE_ORCHIDS_DATA);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot orchidSnapshot : dataSnapshot.getChildren()) {
                    OrchidEntity orchid = orchidSnapshot.getValue(OrchidEntity.class);
                    String key = orchidSnapshot.getKey();
                    if (mCart.contains(key) && orchid != null && orchid.getIsVisibleForSale()) {
                        orchidList.add(orchid);
                    }
                }
                composePurchaseOrder(orchidList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.d("Error trying to get data for the order" +
                        "" + databaseError);
            }
        });
    }

}