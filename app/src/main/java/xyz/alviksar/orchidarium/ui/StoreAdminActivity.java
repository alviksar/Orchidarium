package xyz.alviksar.orchidarium.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.alviksar.orchidarium.BuildConfig;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;
import xyz.alviksar.orchidarium.model.OrchidEntity;

public class StoreAdminActivity extends AppCompatActivity {

    private OrchidEntity mOrchid;
    private boolean mDataHasChanged = false;

    @BindView(R.id.et_code)
    EditText mCodeEditText;

    @BindView(R.id.sw_put_up_for_sale)
    SwitchCompat mPutOnForSaleSwitch;

    @BindView(R.id.tv_state)
    TextView mStateTextView;

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

    MenuItem mSaveMenuItem;

    private int mPlantAge;
    private Uri mSelectedImageUri = null;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private FirebaseAuth mFirebaseAuth;
    private String mUserName;
    //    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;


    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    private Parcelable mSavedRecyclerLayoutState = null;

    // Request codes value
    private static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(OrchidEntity.EXTRA_ORCHID, mOrchid);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_admin);

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
                    RC_SIGN_IN);
        } else {
            mUserName = mFirebaseAuth.getCurrentUser().getUid();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("orchids");

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference("orchid_photos");

        if (savedInstanceState == null || !savedInstanceState.containsKey(OrchidEntity.EXTRA_ORCHID)) {
            mOrchid = getIntent().getParcelableExtra(OrchidEntity.EXTRA_ORCHID);
        } else {
            mOrchid = savedInstanceState.getParcelable(OrchidEntity.EXTRA_ORCHID);
        }
        if (mOrchid == null) {
            setTitle(R.string.title_new_orchid);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.title_edit_orchid);
        }

        ButterKnife.bind(this);

        mCodeEditText.setText(mOrchid.getCode());
        mNameEditText.setText(mOrchid.getName());

        mPutOnForSaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mStateTextView.setText(R.string.label_available_for_order);
                } else {
                    mStateTextView.setText(R.string.label_hidden);
                }
            }
        });
        mPutOnForSaleSwitch.setChecked(mOrchid.getIsVisibleForSale());

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
        mPlantAgeSpinner.setSelection(((ArrayAdapter<String>) mPlantAgeSpinner.getAdapter())
                .getPosition(s));


        // http://qaru.site/questions/32545/how-to-set-selected-item-of-spinner-by-value-not-by-position
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

        mRetaPriceEditText.setText(String.format(Locale.getDefault(),
                "%.2f", mOrchid.getRetailPrice()));
        mDescriptionEditText.setText(mOrchid.getDescription());

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
                    mStateTextView.setText(mPutOnForSaleSwitch.getTextOn());
                } else {
                    mStateTextView.setText(mPutOnForSaleSwitch.getTextOff());
                }
            }
        });

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
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPlantAge = OrchidEntity.AGE_UNKNOWN;
            }
        });

        GlideApp.with(mNiceImageView.getContext())
                .load(mOrchid.getNicePhoto())
                .centerCrop()
                .into(mNiceImageView);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSaveMenuItem = menu.findItem(R.id.action_save);
        // If this is a new orchid, hide the "Delete" menu item.
        if (mOrchid == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
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
                        NavUtils.navigateUpFromSameTask(StoreAdminActivity.this);
                        return true;
                    }

                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(StoreAdminActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...)
        // when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK && mFirebaseAuth.getCurrentUser() != null) {
                mUserName = mFirebaseAuth.getCurrentUser().getUid();

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled",
                        Toast.LENGTH_SHORT).show();
                finish();

            } else {
                // Sign in failed
                if (response == null) {
                    Toast.makeText(this, "Sign in canceled",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.msg_no_connection_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, String.format("Sign in error: $s", response.getError()),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            mSelectedImageUri = data.getData();
            GlideApp.with(mNiceImageView.getContext()).clear(mNiceImageView);
            GlideApp.with(mNiceImageView.getContext())
                    .load(mSelectedImageUri)
                    .centerCrop()
                    .into(mNiceImageView);
        }
    }

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
        
        if (mSelectedImageUri != null) {
            // Upload a new nice image
            final StorageReference photoRef = mStorageReference.child(mSelectedImageUri.getLastPathSegment());

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
            mSaveMenuItem.setEnabled(false);

            // Listen for state changes, errors, and completion of the upload.
            photoRef.putFile(mSelectedImageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    mProgressBar.setProgress((int) progress);
//                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                    System.out.println("Upload is paused");
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

                    //When the image has successfully uploaded, get its download URL
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mOrchid.setNicePhoto(uri.toString());
                            mProgressBar.setVisibility(View.INVISIBLE);
//                            GlideApp.with(mNiceImageView.getContext())
//                                    .load(mOrchid.getNicePhoto())
//                                    .centerCrop()
//                                    .into(mNiceImageView);
                            saveOrchidDataToDB();
                            finish();
                        }
                    });
                }
            });


        } else {
            saveOrchidDataToDB();
            finish();
        }

    }

    private void saveOrchidDataToDB() {
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
                deleteOrchid();
                NavUtils.navigateUpFromSameTask(StoreAdminActivity.this);
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
     * Perform the deletion of the orchid in the database.
     */
    private void deleteOrchid() {
        if (mOrchid != null && !TextUtils.isEmpty(mOrchid.getId())) {
            mDatabaseReference.child(mOrchid.getId()).removeValue();
        }
    }

    /**
     * Create a “Discard changes” dialog
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

    /**
     * Hook up the back button
     */
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

    // Upload an image for a flowering orchid
    @OnClick(R.id.btn_add_nice_photo)
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Choose the image"),
                RC_PHOTO_PICKER);
    }


}
