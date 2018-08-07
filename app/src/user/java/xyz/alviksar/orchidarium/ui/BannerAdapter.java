package xyz.alviksar.orchidarium.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * The adapter for RecyclerView that shows a list of real photos in DetailActivity.
 * This is for a user product flavor.
 */
public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerAdapterViewHolder> {

    /**
     * The interface to handle clicks on items within this Adapter
     */
    public interface BannerAdapterOnClickHandler {
        void onClickBannerPhoto(View view, String url, int position);
    }

    private ArrayList<String> mDataset;
    private BannerAdapterOnClickHandler mClickHandler;

    BannerAdapter(ArrayList<String> myDataset, BannerAdapterOnClickHandler clickHandler) {
        mDataset = myDataset;
        mClickHandler = clickHandler;

    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BannerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        // Create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_banner, parent, false);
        BannerAdapterViewHolder vh = new BannerAdapterViewHolder(v);
        return vh;
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(@NonNull BannerAdapterViewHolder holder, int position) {
        final int photoPadding = 1;
        String photoUrl = mDataset.get(position);
        holder.mImageView.setCropToPadding(true);

        if (!TextUtils.isEmpty(photoUrl)) {
            holder.mImageView.setPadding(photoPadding, photoPadding, photoPadding, photoPadding);
            GlideApp.with(holder.mImageView.getContext())
                    .load(photoUrl)
                    .fitCenter()
                    .into(holder.mImageView);
        }

    }

    // Return the size of dataset
    @Override
    public int getItemCount() {
        if (mDataset != null) {
            return mDataset.size();
        } else {
            return 0;
        }
    }

    ArrayList<String> getData() {
        return mDataset;
    }

    class BannerAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ImageView mImageView;

        BannerAdapterViewHolder(ImageView v) {
            super(v);
            mImageView = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (mDataset != null) {
                mClickHandler.onClickBannerPhoto(view, mDataset.get(position), position);
            }
        }
    }
}

