package xyz.alviksar.orchidarium.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import timber.log.Timber;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.GlideApp;

public class PhotoGalleryActivity extends AppCompatActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static ArrayList<String> mPhotos;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        Timber.i("Activity started");

        int currentPosition;
        if (savedInstanceState == null) {
            mTitle = getIntent().getStringExtra(OrchidEntity.EXTRA_ORCHID_NAME);
            currentPosition = getIntent().
                    getIntExtra(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST_POSITION, 0);
            mPhotos = getIntent().getStringArrayListExtra(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST);
            if (TextUtils.isEmpty(mPhotos.get(mPhotos.size() - 1)))
                mPhotos.remove(mPhotos.size() - 1);  // Remove last (empty) item.
        } else {
            mTitle = savedInstanceState.getString(OrchidEntity.EXTRA_ORCHID_NAME);
            currentPosition = savedInstanceState.
                    getInt(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST_POSITION, 0);
            mPhotos = savedInstanceState.getStringArrayList(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle(mTitle);

        } else
            setTitle(mTitle);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.tv_container);

        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setCurrentItem(currentPosition);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(OrchidEntity.EXTRA_ORCHID_NAME, mTitle);
        outState.putInt(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST_POSITION, mViewPager.getCurrentItem());
        outState.putStringArrayList(OrchidEntity.EXTRA_ORCHID_PHOTO_LIST, mPhotos);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Hook up the up button
                // Continue with navigating up to parent activity
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // User clicked on a menu option in the app bar overflow menu
//            switch (item.getItemId()) {
//                // Respond to a click on the "Up" arrow button in the app bar
//                case android.R.id.home:
//                    // Hook up the up button
//                    // If the orchid hasn't changed, continue with navigating up to parent activity
//                    NavUtils.navigateUpFromSameTask(PhotoGalleryActivity.this);
//                    return true;
//                    default:
//            }
//        }
//    }
//

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
            final int position = getArguments().getInt(ARG_SECTION_NUMBER);
            ImageView imageView = rootView.findViewById(R.id.iv_real_photo);

            if (!TextUtils.isEmpty(mPhotos.get(position))) {
                GlideApp.with(imageView.getContext())
                        .load(mPhotos.get(position))
                        .fitCenter()
                        .into(imageView);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mPhotos.get(position)), "image/*");
                    startActivity(intent);
                }
            });
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            if (mPhotos == null)
                return 0;
            else
                return mPhotos.size();
        }
    }
}
