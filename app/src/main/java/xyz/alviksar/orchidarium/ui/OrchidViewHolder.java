package xyz.alviksar.orchidarium.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;


public class OrchidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;

    View mView;
    Context mContext;

    @BindView(R.id.iv_nice_photo)
    ImageView mNicePhotoImageView;

    @BindView(R.id.iv_in_cart)
    ImageView mInCartImageView;

    @BindView(R.id.tv_price)
    TextView mPriceTextView;

    @BindView(R.id.tv_name)
    TextView mNameTextView;

    @BindView(R.id.tv_beginning)
    TextView mBeginningTextView;


    public OrchidViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindOrchid(OrchidEntity orchid) {
//        ImageView restaurantImageView = (ImageView) mView.findViewById(R.id.restaurantImageView);
//        TextView nameTextView = (TextView) mView.findViewById(R.id.restaurantNameTextView);
//        TextView categoryTextView = (TextView) mView.findViewById(R.id.categoryTextView);
//        TextView ratingTextView = (TextView) mView.findViewById(R.id.ratingTextView);

        /*
        Picasso.with(mContext)
                .load(restaurant.getImageUrl())
                .resize(MAX_WIDTH, MAX_HEIGHT)
                .centerCrop()
                .into(restaurantImageView);
                */

//        nameTextView.setText(restaurant.getName());
//        categoryTextView.setText(restaurant.getCategories().get(0));
//        ratingTextView.setText("Rating: " + restaurant.getRating() + "/5");

        mNameTextView.setText(orchid.getName());
        mPriceTextView.setText(String.valueOf(orchid.getRetailPrice()));
    }

    @Override
    public void onClick(View view) {

        final ArrayList<OrchidEntity> orchids = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orchids");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            /*
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    restaurants.add(snapshot.getValue(Restaurant.class));
                }

                int itemPosition = getLayoutPosition();

                Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                intent.putExtra("position", itemPosition + "");
                intent.putExtra("restaurants", Parcels.wrap(restaurants));

                mContext.startActivity(intent);
                */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}