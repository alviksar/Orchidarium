package xyz.alviksar.orchidarium.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;

public class StoreAdminActivity extends AppCompatActivity {

    @BindView(R.id.sw_put_up_for_sale)
    SwitchCompat mPutOnForSaleSwitch;

    @BindView(R.id.tv_state)
    TextView mStateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_admin);
        ButterKnife.bind(this);

        mPutOnForSaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mStateTextView.setText(mPutOnForSaleSwitch.getTextOn());
                } else {
                    mStateTextView.setText(mPutOnForSaleSwitch.getTextOff());
                }
            }
        });
    }

}
