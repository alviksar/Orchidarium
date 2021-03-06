package xyz.alviksar.orchidarium.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.alviksar.orchidarium.BuildConfig;
import xyz.alviksar.orchidarium.OrchidariumContract;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.GlideApp;

import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

/**
 * Shows photos of orchids and detail data.
 * This is for a "admin" product flavor.
 */

public class DetailActivity extends AppCompatActivity
        implements BannerAdapter.BannerAdapterOnClickHandler {

    // An orchid that this activity shows
    private OrchidEntity mOrchid;

    // Flag if data was changed
    private boolean mDataHasChanged = false;

    @BindView(R.id.et_code)
    EditText mCodeEditText;

    @BindView(R.id.sw_put_up_for_sale)
    SwitchCompat mPutOnForSaleSwitch;

    @BindView(R.id.et_name)
    EditText mNameEditText;

    @BindView(R.id.sp_plant_age)
    Spinner mPlantAgeSpinner;

    @BindView(R.id.sp_pot_size)
    Spinner mPotSizeSpinner;

    @BindView(R.id.et_retail_price)
    EditText mRetaPriceEditText;

    @BindView(R.id.et_description)
    EditText mDescriptionEditText;

    @BindView(R.id.sp_currency)
    Spinner mCurrencySymbol;

    @BindView(R.id.pb_load_photo)
    ProgressBar mProgressBar;

    @BindView(R.id.iv_nice_photo)
    ImageView mNiceImageView;

    @BindView(R.id.rv_banner)
    RecyclerView mBannerRecyclerView;

    LinearLayoutManager mLayoutManager;
    BannerAdapter mBannerAdapter;

    MenuItem mSaveMenuItem;

    private String mUserName;
    private int mPlantAge;
    private Uri mSelectedImageUri = null;

    // Firebase vars
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    // Request codes values
    private static final int RC_AUTH_SIGN_IN = 1;
    private static final int RC_NICE_PHOTO_PICKER = 2;
    private static final int RC_REAL_PHOTO_PICKER = 3;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(OrchidEntity.EXTRA_ORCHID, mOrchid);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // Get authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */,
                                    true /* hints */)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build()))
//                                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                            .build(),
                    RC_AUTH_SIGN_IN);
        } else {
            mUserName = mFirebaseAuth.getCurrentUser().getUid();
        }

        // Init Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase
                .getReference().child(OrchidariumContract.REFERENCE_ORCHIDS_DATA);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage
                .getReference(OrchidariumContract.REFERENCE_ORCHIDS_PHOTOS);

        // Set an activity title
        String title;
        if (savedInstanceState == null || !savedInstanceState.containsKey(OrchidEntity.EXTRA_ORCHID)) {
            mOrchid = getIntent().getParcelableExtra(OrchidEntity.EXTRA_ORCHID);
        } else {
            mOrchid = savedInstanceState.getParcelable(OrchidEntity.EXTRA_ORCHID);
        }
        if (mOrchid == null) {
            title = getString(R.string.title_new_orchid);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
            mOrchid = new OrchidEntity();
        } else {
            title = mOrchid.getName();
        }
        setTitle(title);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(title);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*
        Check if a tablet portrait layout or not
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

        ArrayList<String> bannerList = new ArrayList<>(mOrchid.getRealPhotos());
        // Add an empty  item for the add photo image
        bannerList.add(getString(R.string.empty));

        // Specify an adapter
        mBannerAdapter = new BannerAdapter(bannerList, this);
        mBannerRecyclerView.setAdapter(mBannerAdapter);

        // Set orchid data
        mCodeEditText.setText(mOrchid.getCode());
        mNameEditText.setText(mOrchid.getName());
        String s;
        switch (mOrchid.getAge()) {
            case OrchidEntity.AGE_BLOOMING:
                s = getString(R.string.plant_age_blooming);
                break;
            case OrchidEntity.AGE_FLOWERING:
                s = getString(R.string.plant_age_flowering);
                break;
            case OrchidEntity.AGE_ONE_YEARS_BEFORE:
                s = getString(R.string.plant_age_one_years_before);
                break;
            case OrchidEntity.AGE_TWO_YEARS_BEFORE:
                s = getString(R.string.plant_age_two_years_before);
                break;
            case OrchidEntity.AGE_UNKNOWN:
                s = getString(R.string.empty);
                break;
            default:
                s = getString(R.string.empty);
        }
        mPlantAgeSpinner.setSelection(((ArrayAdapter<String>) mPlantAgeSpinner.getAdapter())
                .getPosition(s));

        /*
        http://qaru.site/questions/32545/how-to-set-selected-item-of-spinner-by-value-not-by-position
        */
        if (mOrchid.getPotSize() != null) {
            mPotSizeSpinner.setSelection(((ArrayAdapter<String>) mPotSizeSpinner.getAdapter())
                    .getPosition(mOrchid.getPotSize()));
        }
        if (mOrchid.getCurrencySymbol() == null
                || TextUtils.isEmpty(mOrchid.getCurrencySymbol())) {
            // Take the last chosen currency symbol
            String symbol = OrchidariumPreferences.getCurrencySymbol(this);
            mCurrencySymbol.setSelection(((ArrayAdapter<String>) mCurrencySymbol.getAdapter())
                    .getPosition(symbol));
        } else {
            mCurrencySymbol.setSelection(((ArrayAdapter<String>) mCurrencySymbol.getAdapter())
                    .getPosition(mOrchid.getCurrencySymbol()));
        }

        // Format a price string
        if (mOrchid.getRetailPrice() != 0)
            mRetaPriceEditText.setText(String.format(Locale.getDefault(),
                    "%.2f", mOrchid.getRetailPrice()));
        else
            mRetaPriceEditText.setText("");

        mDescriptionEditText.setText(mOrchid.getDescription());

        // Set listeners if tap on any TextEdit or Spinner
        mCodeEditText.setOnTouchListener(mTouchListener);
        mPutOnForSaleSwitch.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mRetaPriceEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPlantAgeSpinner.setOnTouchListener(mTouchListener);
        mPotSizeSpinner.setOnTouchListener(mTouchListener);

        mPutOnForSaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPutOnForSaleSwitch.setText(mPutOnForSaleSwitch.getTextOn());
                } else {
                    mPutOnForSaleSwitch.setText(mPutOnForSaleSwitch.getTextOff());
                }
            }
        });
        mPutOnForSaleSwitch.setChecked(mOrchid.getIsVisibleForSale());
        if (mOrchid.getIsVisibleForSale()) {
            mPutOnForSaleSwitch.setText(mPutOnForSaleSwitch.getTextOn());
        } else {
            mPutOnForSaleSwitch.setText(mPutOnForSaleSwitch.getTextOff());
        }

        // Set the integer to the constant values
        mPlantAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.plant_age_two_years_before))) {
                        mPlantAge = OrchidEntity.AGE_TWO_YEARS_BEFORE;
                    } else if (selection.equals(getString(R.string.plant_age_one_years_before))) {
                        mPlantAge = OrchidEntity.AGE_ONE_YEARS_BEFORE;
                    } else if (selection.equals(getString(R.string.plant_age_flowering))) {
                        mPlantAge = OrchidEntity.AGE_FLOWERING;
                    } else if (selection.equals(getString(R.string.plant_age_blooming))) {
                        mPlantAge = OrchidEntity.AGE_BLOOMING;
                    } else {
                        mPlantAge = OrchidEntity.AGE_UNKNOWN;
                    }
                } else {
                    mPlantAge = OrchidEntity.AGE_UNKNOWN;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPlantAge = OrchidEntity.AGE_UNKNOWN;
            }
        });

        GlideApp.with(mNiceImageView.getContext())
                .load(mOrchid.getNicePhoto())
                //  .centerCrop()
                .fitCenter()
                .into(mNiceImageView);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSaveMenuItem = menu.findItem(R.id.action_save);

        // If this is a new orchid, hide the "Delete" menu item.
        if (mOrchid == null || getTitle().equals(getString(R.string.title_new_orchid))) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setEnabled(false);
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
        try {
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_save:
                    saveOrchid();
                    //  finish();
                    return true;
                // Respond to a click on the "Delete" menu option
                case R.id.action_delete:
                    showDeleteConfirmationDialog();
                    return true;
                // Respond to a click on the "Up" arrow button in the app bar
                case android.R.id.home:
                    // Hook up the up button
                    // If the orchid hasn't changed, continue with navigating up to parent activity
                    if (!mDataHasChanged) {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                        return true;
                    }
                    /*
                     If there are unsaved changes, setup a dialog to warn the user.
                     Create a click listener to handle the user confirming that
                     changes should be discarded.
                     */
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }
        } catch (IllegalArgumentException e) {
            Snackbar.make(findViewById(R.id.coordinatorlayout),
                    e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_AUTH_SIGN_IN is the request code you passed into startActivityForResult(...)
        // when starting the sign in flow.
        if (requestCode == RC_AUTH_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK && mFirebaseAuth.getCurrentUser() != null) {
                mUserName = mFirebaseAuth.getCurrentUser().getUid();

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Snackbar.make(findViewById(R.id.coordinatorlayout), R.string.msg_sign_in_canceled,
                        Snackbar.LENGTH_LONG).show();
                finish();

            } else {
                // Sign in failed
                if (response == null) {
                    Snackbar.make(findViewById(R.id.coordinatorlayout),
                            R.string.msg_sign_in_canceled,
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Snackbar.make(findViewById(R.id.coordinatorlayout),
                            R.string.msg_no_connection_error,
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                Snackbar.make(findViewById(R.id.coordinatorlayout), response.getError().getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == RC_NICE_PHOTO_PICKER && resultCode == RESULT_OK) {
            // Get a nice photo
            mSelectedImageUri = data.getData();
            GlideApp.with(mNiceImageView.getContext()).clear(mNiceImageView);
            GlideApp.with(mNiceImageView.getContext())
                    .load(mSelectedImageUri)
                    .centerCrop()
                    .into(mNiceImageView);
        } else if (requestCode == RC_REAL_PHOTO_PICKER && resultCode == RESULT_OK) {
            // Get one of real photos
            if (data.getData() != null)
                mBannerAdapter.addImage(data.getData().toString());
        }
    }

    /**
     * Saves orchid data in database
     */
    private void saveOrchid() {
        if (mOrchid == null) {
            mOrchid = new OrchidEntity();
        }

        mOrchid.setIsVisibleForSale(mPutOnForSaleSwitch.isChecked());
        mOrchid.setCode(mCodeEditText.getText().toString().trim());
        mOrchid.setName(mNameEditText.getText().toString().trim());
        mOrchid.setAge(mPlantAge);
        mOrchid.setPotSize(mPotSizeSpinner.getSelectedItem().toString().trim());
        mOrchid.setRetailPrice(Double.valueOf(mRetaPriceEditText.getText().toString().trim()
                .replace(',', '.')));
        mOrchid.setDescription(mDescriptionEditText.getText().toString().trim());
        mOrchid.setCurrencySymbol(mCurrencySymbol.getSelectedItem().toString());
        mOrchid.setWriter(mUserName);
        mOrchid.setSaveTime(System.currentTimeMillis());
        // Save a chosen currency symbol
        OrchidariumPreferences.setCurrencySymbol(this, mOrchid.getCurrencySymbol());

        mProgressBar.setVisibility(View.VISIBLE);
        // Update the list of real photos and save orchid data in database
        changeListOfPhotosAndSaveDataToDb(
                getToDelete(mOrchid.getRealPhotos(), mBannerAdapter.getData()));

    }

    /**
     * Uploads the nice photo in Firebase Storage and save orchid data in the Database
     */
    private void uploadNicePhotoAndSaveDataToDb() {
        if (mSelectedImageUri != null) {

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
            mSaveMenuItem.setEnabled(false);

            // First, delete old image
            if (!TextUtils.isEmpty(mOrchid.getNicePhoto())) {
                // Delete old file
                final StorageReference deleteRef = mFirebaseStorage
                        .getReferenceFromUrl(mOrchid.getNicePhoto());
                deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Upload a new nice image
                        uploadNicePhotoAndFinish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String errMsg = exception.getMessage();
                        Snackbar.make(findViewById(R.id.coordinatorlayout), errMsg,
                                Snackbar.LENGTH_LONG).show();
                        int errorCode = ((StorageException) exception).getErrorCode();
                        if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                            // Can continue
                            uploadNicePhotoAndFinish();
                        }
                    }
                });
            } else {
                // Upload a new nice image
                uploadNicePhotoAndFinish();
            }


        } else {
            saveOrchidDataToDb();
            finish();
        }
    }

    /**
     *  Uploads the nice photo into Storage and saves the orchid data into the Database
     */
    private void uploadNicePhotoAndFinish() {
        final StorageReference photoRef = mStorageReference.child(mSelectedImageUri.getLastPathSegment());
        photoRef.putFile(mSelectedImageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                / taskSnapshot.getTotalByteCount();
                        mProgressBar.setProgress((int) progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mSaveMenuItem.setEnabled(true);
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
//                https://gist.github.com/jonathanbcsouza/13929ab81077645f1033bf9ce45beaab
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // When the image has successfully uploaded, get its download URL
                photoRef.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Take a new url
                                mOrchid.setNicePhoto(uri.toString());
                                mProgressBar.setVisibility(View.INVISIBLE);

                                // Save the object in the database
                                saveOrchidDataToDb();

                                // That is it. Uff...
                                finish();
                            }
                        });
            }
        });
    }

    private void saveOrchidDataToDb() {
        // Save or add new orchid
        if (TextUtils.isEmpty(mOrchid.getId())) {
            // Add new data
            mDatabaseReference.push().setValue(mOrchid);
        } else {
            // Replace existing data
            mDatabaseReference.child(mOrchid.getId()).setValue(mOrchid);
        }
    }

    /**
     * Check if changes were made
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDataHasChanged = true;
            return false;
        }
    };

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_delete_dialog);
        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the orchid.
                // deleteOrchid();
                deleteListOfPhotosAndObjectFromDb(
                        getToDelete(mOrchid.getRealPhotos(), new ArrayList<String>()));
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the orchid.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Deletes the orchid data from the Database.
     */
    private void deleteOrchid() {
        if (mOrchid != null && !TextUtils.isEmpty(mOrchid.getId())) {
            if (!TextUtils.isEmpty(mOrchid.getNicePhoto())) {
                // Delete image  file
                final StorageReference deleteRef
                        = mFirebaseStorage.getReferenceFromUrl(mOrchid.getNicePhoto());
                deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If file was deleted successfully, delete object from database
                        mDatabaseReference.child(mOrchid.getId()).removeValue();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String errMsg = exception.getMessage();
                        Snackbar.make(findViewById(R.id.coordinatorlayout), errMsg,
                                Snackbar.LENGTH_LONG).show();
                        int errorCode = ((StorageException) exception).getErrorCode();
                        if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                            // Can delete
                            mDatabaseReference.child(mOrchid.getId()).removeValue();
                        }
                    }
                });
            } else {
                mDatabaseReference.child(mOrchid.getId()).removeValue();
            }
        }
    }

    /**
     * Creates a “Discard changes” dialog
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_unsaved_changes_dialog);
        builder.setPositiveButton(R.string.btn_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.btn_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the orchid hasn't changed, continue with handling back button press
        if (!mDataHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @OnClick(R.id.btn_add_nice_photo)
    public void onClickBtn(View view) {
        choosePhoto(RC_NICE_PHOTO_PICKER);
    }

    @OnClick(R.id.iv_nice_photo)
    public void onClickImage(View view) {
        if (TextUtils.isEmpty(mOrchid.getNicePhoto())) return;
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mOrchid.getNicePhoto()), "image/*");
        startActivity(intent);
    }

    @Override
    public void onClickBannerPhoto(View view, String url, int position) {
        if (TextUtils.isEmpty(url)) {
            //  Starts an implicit intent to choose photo from the phone gallery
            choosePhoto(RC_REAL_PHOTO_PICKER);
        } else {
            //  Starts an implicit intent to show photo by viewer
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
    }

    /**
     * Starts an implicit intent to choose photo from the phone gallery
     *
     * @param requestCode
     */

    private void choosePhoto(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.msg_choose_image)),
                requestCode);
    }

    /**
     * Defines the list of added photos which have to be uploaded
     *
     * @param afterList The changed list
     * @return The list of added photos
     */
    private Stack<String> getToUpload(List<String> afterList) {
        Stack<String> toUpload = new Stack<>();
        for (String s : afterList) {
            Uri uri = Uri.parse(s);
            String host = uri.getScheme();
            if ("content".equals(host)) toUpload.push(s);
        }
        return toUpload;
    }

    /**
     * Compares lists and takes the difference
     *
     * @param beforeList The old list
     * @param afterList  The new list
     * @return The list of strings from the old list not included in the new list
     */
    private Stack<String> getToDelete(List<String> beforeList, List<String> afterList) {
        Stack<String> toDelete = new Stack<>();
        for (String s : beforeList) {
            if (!afterList.contains(s)) toDelete.push(s);
        }
        return toDelete;
    }

    /**
     * Updates the list of real photos and saves orchid data in database
     *
     * @param toDelete List of real photos to be deleted.
     */
    private void changeListOfPhotosAndSaveDataToDb(final Stack<String> toDelete) {
        if (toDelete.empty()) {
            // Next step
            // Upload photos to Firebase Storage and save orchid data in database
            uploadListOfPhotosAndSaveDataToDb(getToUpload(mBannerAdapter.getData()));
        } else {
            // Delete list of real photos
            final String photoUrl = toDelete.pop();
            if (!TextUtils.isEmpty(photoUrl)) {
                // Delete image file
                final StorageReference deleteRef
                        = mFirebaseStorage.getReferenceFromUrl(photoUrl);
                deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If file was deleted successfully, delete next one
                        mOrchid.getRealPhotos().remove(photoUrl);
                        changeListOfPhotosAndSaveDataToDb(toDelete);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String errMsg = exception.getMessage();
                        Snackbar.make(findViewById(R.id.coordinatorlayout), errMsg,
                                Snackbar.LENGTH_LONG).show();
                        int errorCode = ((StorageException) exception).getErrorCode();
                        if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                            // Can continue
                            mOrchid.getRealPhotos().remove(photoUrl);
                            changeListOfPhotosAndSaveDataToDb(toDelete);
                        }
                    }
                });
            }
        }
    }

    /**
     * Deletes photos form Firebase Storage by list
     *
     * @param photosToDelete The list of  photos to be deleted.
     */
    private void deleteListOfPhotosAndObjectFromDb(final Stack<String> photosToDelete) {
        if (photosToDelete.empty()) {
            // Next step
            deleteOrchid();
        } else {
            // Delete list of real photos
            final String photoUrl = photosToDelete.pop();
            if (!TextUtils.isEmpty(photoUrl)) {
                // Delete image file
                final StorageReference deleteRef
                        = mFirebaseStorage.getReferenceFromUrl(photoUrl);
                deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If file was deleted successfully, delete next one
                        mOrchid.getRealPhotos().remove(photoUrl);
                        deleteListOfPhotosAndObjectFromDb(photosToDelete);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String errMsg = exception.getMessage();
                        Snackbar.make(findViewById(R.id.coordinatorlayout), errMsg,
                                Snackbar.LENGTH_LONG).show();
                        int errorCode = ((StorageException) exception).getErrorCode();
                        if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                            // Can continue
                            mOrchid.getRealPhotos().remove(photoUrl);
                            deleteListOfPhotosAndObjectFromDb(photosToDelete);
                        }
                    }
                });
            }
        }
    }

    /**
     * Uploads photos into Firebase Storage and saves orchid data in the Database
     *
     * @param toUpload The list of real photos to be upload into Firebase Storage
     */
    private void uploadListOfPhotosAndSaveDataToDb(final Stack<String> toUpload) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (toUpload.empty()) {
            // All photos have been saved, so save the nice photo and the orchid data
            uploadNicePhotoAndSaveDataToDb();

        } else {
            // Save the list of real photos recursively
            String photoUrl = toUpload.pop();
            if (!TextUtils.isEmpty(photoUrl)) {
                Uri photoUri = Uri.parse(photoUrl);
                final StorageReference photoRef = mStorageReference.child(photoUri.getLastPathSegment());

                mProgressBar.setProgress(0);
                // Listen for state changes, errors, and completion of the upload.
                photoRef.putFile(photoUri).addOnProgressListener(
                        new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                        / taskSnapshot.getTotalByteCount();
                                mProgressBar.setProgress((int) progress);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mSaveMenuItem.setEnabled(true);
                        String errMsg = exception.getMessage();
                        Snackbar.make(findViewById(R.id.coordinatorlayout), errMsg,
                                Snackbar.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
//                https://gist.github.com/jonathanbcsouza/13929ab81077645f1033bf9ce45beaab
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //When the image has successfully uploaded, get its download URL
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Save new url
                                mOrchid.getRealPhotos().add(uri.toString());
                                mProgressBar.setVisibility(View.INVISIBLE);

                                // Upload next one
                                uploadListOfPhotosAndSaveDataToDb(toUpload);
                            }
                        });
                    }
                });
            }
        }
    }


}
