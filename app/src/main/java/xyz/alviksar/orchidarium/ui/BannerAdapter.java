package xyz.alviksar.orchidarium.ui;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;
import java.util.Stack;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.util.GlideApp;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerAdapterViewHolder> {


    /**
     * The interface to handle clicks on items within this Adapter
     */
    public interface BannerAdapterOnClickHandler {
        void onClickBannerPhoto(String url);
    }

    private List<String> mDataset;
    private  BannerAdapterOnClickHandler mClickHandler;

    public BannerAdapter(List<String> myDataset, BannerAdapterOnClickHandler clickHandler) {
        mDataset = myDataset;
        mClickHandler = clickHandler;

    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BannerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        // create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_banner, parent, false);
        BannerAdapterViewHolder vh = new BannerAdapterViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull BannerAdapterViewHolder holder, int position) {
        final int emptyPadding = 32;
        final int photoPadding = 4;
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String photoUrl = mDataset.get(position);
        holder.mImageView.setCropToPadding(true);

        if (TextUtils.isEmpty(photoUrl)) {
            holder.mImageView.setPadding(emptyPadding, emptyPadding, emptyPadding, emptyPadding);
            GlideApp.with(holder.mImageView.getContext())
                    .load(R.drawable.ic_add_a_photo_gray_24dp)
                    .centerCrop()
                    .into(holder.mImageView);
        } else {
            holder.mImageView.setPadding(photoPadding, photoPadding, photoPadding, photoPadding);
            GlideApp.with(holder.mImageView.getContext())
                    .load(photoUrl)
                    .centerCrop()
                    .into(holder.mImageView);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset != null) {
            return mDataset.size();
        } else {
            return 0;
        }
    }

    public List<String> getData() {
        return mDataset;
    }

    public void addImage(String imageUri) {
        if (mDataset != null && mDataset.size() > 0) {
            mDataset.add(mDataset.size()-1, imageUri);
            // After the new data is set, call notifyDataSetChanged
            notifyDataSetChanged();
        }
    }

    class BannerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
                mClickHandler.onClickBannerPhoto(mDataset.get(position));
            }
        }

    }
}
