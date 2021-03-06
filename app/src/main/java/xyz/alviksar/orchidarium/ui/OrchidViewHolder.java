package xyz.alviksar.orchidarium.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.BuildConfig;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * The ViewHolder for FirebaseRecyclerAdapter in MainActivity.
 */

public class OrchidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;
    private Activity mActivity;

    @BindView(R.id.iv_nice_photo)
    ImageView mNiceImageView;

    @BindView(R.id.tv_price)
    TextView mPriceTextView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.iv_state)
    ImageView mStateImageView;

    private OrchidEntity mOrchidItem;

    public OrchidViewHolder(View itemView, Activity activity) {
        super(itemView);
        mContext = itemView.getContext();
        mActivity = activity;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindOrchid(OrchidEntity orchid, String key, Boolean inCart) {
        mOrchidItem = orchid;
        mOrchidItem.setId(key);

        mNameTextView.setText(orchid.getName());

        // Format a price string
        if (orchid.getCurrencySymbol().equals(mContext.getString(R.string.sign_usd))) {
            mPriceTextView.setText(String.format(Locale.getDefault(),
                    "$ %.2f", orchid.getRetailPrice()));
        } else {
            if (orchid.getCurrencySymbol().equals(mContext.getString(R.string.sign_rur))) {
                mPriceTextView.setText(String.format(Locale.getDefault(),
                        "%.0f %s", orchid.getRetailPrice(), orchid.getCurrencySymbol()));
            } else {
                mPriceTextView.setText(String.format(Locale.getDefault(),
                        "%.2f %s", orchid.getRetailPrice(), orchid.getCurrencySymbol()));
            }
        }

        if (BuildConfig.FLAVOR.equals("admin")) {
            mStateImageView.setImageResource(R.drawable.ic_visibility_off_white_24dp);
            if (mOrchidItem.getIsVisibleForSale()) {
                mStateImageView.setVisibility(View.GONE);
            } else {
                mStateImageView.setVisibility(View.VISIBLE);
            }
        }

        if (BuildConfig.FLAVOR.equals("user")) {
            mStateImageView.setImageResource(R.drawable.ic_shopping_cart_yellow_24dp);
            if (inCart) {
                mStateImageView.setVisibility(View.VISIBLE);
            } else {
                mStateImageView.setVisibility(View.GONE);
            }
        }
        GlideApp.with(mNiceImageView.getContext())
                .load(mOrchidItem.getNicePhoto())
                .centerCrop()
                .into(mNiceImageView);
    }

    @Override
    public void onClick(View view) {
        // Start a detail activity
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(OrchidEntity.EXTRA_ORCHID, mOrchidItem);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View sharedView = view.findViewById(R.id.iv_nice_photo);
            mContext.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            mActivity,
                            sharedView,
                            sharedView.getTransitionName())
                            .toBundle());
        } else {
            mContext.startActivity(intent);
        }
    }
}

