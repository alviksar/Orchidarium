package xyz.alviksar.orchidarium.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.util.GlideApp;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<String> mDataset;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        ViewHolder(ImageView v) {
            super(v);
            mImageView = v;
        }
    }

    public BannerAdapter(List<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BannerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        // create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_banner, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int emptyPadding = 32;
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String photoUrl = mDataset.get(position);
        if (TextUtils.isEmpty(photoUrl)) {
            holder.mImageView.setCropToPadding(true);
            holder.mImageView.setPadding(emptyPadding, emptyPadding, emptyPadding, emptyPadding);
            GlideApp.with(holder.mImageView.getContext())
                    .load(R.drawable.ic_add_a_photo_gray_24dp)
                    .centerCrop()
                    .into(holder.mImageView);
        } else {
            GlideApp.with(holder.mImageView.getContext())
                    .load(photoUrl)
                    .centerCrop()
                    .into(holder.mImageView);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

