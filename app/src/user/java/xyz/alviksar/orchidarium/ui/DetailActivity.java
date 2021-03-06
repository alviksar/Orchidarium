package xyz.alviksar.orchidarium.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * Shows photos of orchids and detail data.
 * This is for a "user" product flavor.
 */

public class DetailActivity extends AppCompatActivity
        implements BannerAdapter.BannerAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private OrchidEntity mOrchid;

    @BindView(R.id.tv_code)
    TextView mCodeTextView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_plant_age)
    TextView mPlantAgeTextView;

    @BindView(R.id.tv_pot_size)
    TextView mPotSizeTextView;

    @BindView(R.id.tv_retail_price)
    TextView mPriceTextView;

    @BindView(R.id.tv_description)
    TextView mDescriptionTextView;

    @BindView(R.id.pb_load_photo)
    ProgressBar mProgressBar;

    @BindView(R.id.btn_add_nice_photo)
    FloatingActionButton mAddToCartButton;

    @BindView(R.id.iv_nice_photo)
    ImageView mNiceImageView;

    @BindView(R.id.rv_banner)
    RecyclerView mBannerRecyclerView;
    LinearLayoutManager mLayoutManager;
    BannerAdapter mBannerAdapter;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(OrchidEntity.EXTRA_ORCHID, mOrchid);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        String title;
        if (savedInstanceState == null || !savedInstanceState.containsKey(OrchidEntity.EXTRA_ORCHID)) {
            mOrchid = getIntent().getParcelableExtra(OrchidEntity.EXTRA_ORCHID);
        } else {
            mOrchid = savedInstanceState.getParcelable(OrchidEntity.EXTRA_ORCHID);
        }
        if (mOrchid == null) {
            title = getString(R.string.title_new_orchid);
            invalidateOptionsMenu();
            mOrchid = new OrchidEntity();
        } else {
            title = mOrchid.getName();
        }
        setTitle(title);

        if (OrchidariumPreferences.inCart(this, mOrchid))
            mAddToCartButton.setImageResource(R.drawable.ic_remove_shopping_cart_white_24dp);
        else
            mAddToCartButton.setImageResource(R.drawable.ic_add_shopping_cart_white_24dp);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        /*
        Check a current mode if a tablet portrait
        https://stackoverflow.com/questions/9279111/determine-if-the-device-is-a-smartphone-or-tablet
        */
        boolean tabletPort = getResources().getBoolean(R.bool.isTabletPort);
        if (tabletPort) {
            // If a tablet portrait screen, use 2 columns grid
            mLayoutManager = new GridLayoutManager(this, 2);
        } else {
            // Use a linear layout manager
            mLayoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        }
        mBannerRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        ArrayList<String> bannerList = new ArrayList<>(mOrchid.getRealPhotos());
        mBannerAdapter = new BannerAdapter(bannerList, this);
        mBannerRecyclerView.setAdapter(mBannerAdapter);

        mCodeTextView.setText(mOrchid.getCode());
        mNameTextView.setText(mOrchid.getName());

        String s = "";
        switch (mOrchid.getAge()) {
            case OrchidEntity.AGE_BLOOMING:
                s = getResources().getString(R.string.plant_age_blooming);
                break;
            case OrchidEntity.AGE_FLOWERING:
                s = getResources().getString(R.string.plant_age_flowering);
                break;
            case OrchidEntity.AGE_ONE_YEARS_BEFORE:
                s = getResources().getString(R.string.plant_age_one_years_before);
                break;
            case OrchidEntity.AGE_TWO_YEARS_BEFORE:
                s = getResources().getString(R.string.plant_age_two_years_before);
                break;
            default:
                s = "";
        }
        mPlantAgeTextView.setText(s);

        mPotSizeTextView.setText(mOrchid.getPotSize());

        if (mOrchid.getRetailPrice() != 0) {
            if (mOrchid.getCurrencySymbol().equals(getString(R.string.sign_usd))) {
                mPriceTextView.setText(String.format(Locale.getDefault(),
                        "$ %.2f", mOrchid.getRetailPrice()));
            } else {
                if (mOrchid.getCurrencySymbol().equals(getString(R.string.sign_rur))) {
                    mPriceTextView.setText(String.format(Locale.getDefault(),
                            "%.0f %s", mOrchid.getRetailPrice(), mOrchid.getCurrencySymbol()));
                } else {
                    mPriceTextView.setText(String.format(Locale.getDefault(),
                            "%.2f %s", mOrchid.getRetailPrice(), mOrchid.getCurrencySymbol()));
                }
            }
        } else
            mPriceTextView.setText("");

        mDescriptionTextView.setText(mOrchid.getDescription());

        GlideApp.with(mNiceImageView.getContext())
                .load(mOrchid.getNicePhoto())
                //  .centerCrop()
                .fitCenter()
                .into(mNiceImageView);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mOrchid != null) {
            // Show an in/out cart icon
            MenuItem menuItem = menu.findItem(R.id.action_cart);
            if (OrchidariumPreferences.inCart(this, mOrchid))
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Hook up the up button
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
            case R.id.action_share:
                shareOrchidPhotos(mOrchid);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareOrchidPhotos(OrchidEntity mOrchid) {
//        ArrayList<Uri> imageUris = new ArrayList<Uri>();
//        for(String imageUr1: mOrchid.getRealPhotos()) {
//            imageUris.add(Uri.parse(imageUr1));
//        }
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
//        shareIntent.setType("image/*");
//        startActivity(Intent.createChooser(shareIntent, getString(R.string.msg_share_images)));
        StringBuilder text = new StringBuilder();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        text.append(mOrchid.getNicePhoto()).append("\n\n");
        for (String imageUr1 : mOrchid.getRealPhotos()) {
            text.append(imageUr1).append("\n\n");
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, text.toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.msg_share_images)));
    }

    @OnClick(R.id.iv_nice_photo)
    public void onClickImage(View view) {
        // Start an implicit intent to view image
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mOrchid.getNicePhoto()), "image/*");
        startActivity(intent);
    }

    private void cartTransfer() {
        if (OrchidariumPreferences.inCart(this, mOrchid)) {
            // Set icon and remove the orchid from the shopping cart
            mAddToCartButton.setImageResource(R.drawable.ic_add_shopping_cart_white_24dp);
            OrchidariumPreferences.removeFromCart(this, mOrchid);
        } else {
            // Set icon and put the orchid into the shopping cart
            mAddToCartButton.setImageResource(R.drawable.ic_remove_shopping_cart_white_24dp);
            OrchidariumPreferences.addToCart(this, mOrchid);
        }
        invalidateOptionsMenu();
    }

    @OnClick(R.id.btn_add_nice_photo)
    public void onClickCartButton(View view) {
        cartTransfer();
    }

    @Override
    public void onClickBannerPhoto(View view, String url, int position) {
        // Start an implicit intent to view image
        Intent intent = new Intent(DetailActivity.this,
                PhotoGalleryActivity.class);
        intent.setData(Uri.parse(url));
        intent.putExtra(OrchidEntity.EXTRA_ORCHID_NAME, mOrchid.getName());
        intent.putStringArrayListExtra(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST,
                mBannerAdapter.getData());
        intent.putExtra(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST_POSITION, position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View sharedView = view.findViewById(R.id.iv_real_photo);
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            DetailActivity.this,
                            sharedView,
                            sharedView.getTransitionName())
                            .toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(OrchidariumPreferences.PREF_CONTENTS_OF_THE_CART)) {
            invalidateOptionsMenu();
        }
    }
}
