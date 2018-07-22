package xyz.alviksar.orchidarium.ui;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.util.GlideApp;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerAdapterViewHolder> {


    /**
     * The interface to handle clicks on items within this Adapter
     */
    public interface BannerAdapterOnClickHandler {
        void onClickBannerPhoto(View view, String url, int position);
    }

    private ArrayList<String> mDataset;
    private BannerAdapterOnClickHandler mClickHandler;

    public BannerAdapter(ArrayList<String> myDataset, BannerAdapterOnClickHandler clickHandler) {
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
        final int photoPadding = 1;
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

    public ArrayList<String> getmDataset() {
        return mDataset;
    }

    ArrayList<String> getData() {
        return mDataset;
    }

    public void addImage(String imageUri) {
        if (mDataset != null && mDataset.size() > 0) {
            mDataset.add(mDataset.size() - 1, imageUri);
            // After the new data is set, call notifyDataSetChanged
            notifyDataSetChanged();
        }
    }

    class BannerAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
        ImageView mImageView;

        BannerAdapterViewHolder(ImageView v) {
            super(v);
            mImageView = v;
            v.setOnClickListener(this);
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (mDataset != null) {
                mClickHandler.onClickBannerPhoto(view, mDataset.get(position), position);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            if (position != mDataset.size() - 1) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_photo:
                    int position = getAdapterPosition();
                    if (position != mDataset.size() - 1) {
                        mDataset.remove(position);
                        notifyDataSetChanged();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
}

