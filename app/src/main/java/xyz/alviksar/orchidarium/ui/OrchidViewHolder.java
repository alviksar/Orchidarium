package xyz.alviksar.orchidarium.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.DateFormatter;
import xyz.alviksar.orchidarium.util.GlideApp;


public class OrchidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;

    View mView;
    Context mContext;

    @BindView(R.id.iv_nice_photo)
    ImageView mNiceImageView;

    @BindView(R.id.iv_in_cart)
    ImageView mInCartImageView;

    @BindView(R.id.tv_price)
    TextView mPriceTextView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_for_sale_time)
    TextView mForSaleTimeTextView;

    private OrchidEntity mOrchidItem;


    public OrchidViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindOrchid(OrchidEntity orchid, String key) {
        mNameTextView.setText(orchid.getName());
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

        if (orchid.getForSaleTime() > 0)
            mForSaleTimeTextView.setText(DateFormatter.timeFrom(orchid.getForSaleTime()));
        mOrchidItem = orchid;
        mOrchidItem.setId(key);
        GlideApp.with(mNiceImageView.getContext())
                .load(mOrchidItem.getNicePhoto())
                .centerCrop()
                .into(mNiceImageView);
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(mContext, StoreAdminActivity.class);
        intent.putExtra(OrchidEntity.EXTRA_ORCHID, mOrchidItem);
        mContext.startActivity(intent);

    }
}

