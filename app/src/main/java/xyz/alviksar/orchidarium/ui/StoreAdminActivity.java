package xyz.alviksar.orchidarium.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
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

    private int mPlantAge;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_admin);
        ButterKnife.bind(this);

        mOrchid = getIntent().getParcelableExtra(OrchidEntity.EXTRA_ORCHID);
        if (mOrchid == null) {
            setTitle(R.string.title_new_orchid);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.title_edit_orchid);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("orchids");

        mCodeEditText.setOnTouchListener(mTouchListener);
        mPutOnForSaleSwitch.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPlantAgeSpinner.setOnTouchListener(mTouchListener);
        mPotSizeSpinner.setOnTouchListener(mTouchListener);
        mRetaPriceEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);

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
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
                    finish();
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

    private void saveOrchid() {
        if (mOrchid == null) {
            mOrchid = new OrchidEntity();
        }

        mOrchid.setIsVisibleForSale(mPutOnForSaleSwitch.isChecked());
        mOrchid.setCode(mCodeEditText.getText().toString().trim());
        mOrchid.setName(mNameEditText.getText().toString().trim());
        mOrchid.setAge(mPlantAge);
        mOrchid.setPotSize(mPotSizeSpinner.getSelectedItem().toString().trim());
        mOrchid.setRetailPrice(Double.valueOf(mRetaPriceEditText.getText().toString().trim()));
        mOrchid.setDescription(mDescriptionEditText.getText().toString().trim());

//        mOrchid = DummyData.getOrchid(22);
        mDatabaseReference.push().setValue(mOrchid);
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
                // User clicked the "Delete" button, so delete the pet.
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
        if (mOrchid != null) {
            // TODO: delete orchid
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
                // and continue editing the pet.
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
        // If the pet hasn't changed, continue with handling back button press
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
}
