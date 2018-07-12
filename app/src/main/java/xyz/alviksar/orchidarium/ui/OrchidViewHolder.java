package xyz.alviksar.orchidarium.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.util.DateFormatter;


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

    @BindView(R.id.tv_for_sale_time)
    TextView mFoeSaleTimeTextView;

    OrchidEntity mOrchidItem;


    public OrchidViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindOrchid(OrchidEntity orchid, String key) {
        mNameTextView.setText(orchid.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        mPriceTextView.setText(currencyFormat.format(orchid.getRetailPrice()));
        if (orchid.getForSaleTime() > 0)
            mFoeSaleTimeTextView.setText(DateFormatter.timeFrom(orchid.getForSaleTime()));
        mOrchidItem = orchid;
        mOrchidItem.setId(key);
    }
//    public void onClick(View v) {
//        Toast.makeText(v.getContext(), "Delete icon has been clicked", Toast.LENGTH_LONG).show();
//        String taskTitle = taskObject.get(getAdapterPosition()).getTask();
////        Log.d(TAG, "Task Title " + taskTitle);
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//        Query applesQuery = ref.orderByChild("task").equalTo(taskTitle);
//        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
//                    appleSnapshot.getRef().removeValue();
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//              // TODO:  Log.e(TAG, "onCancelled", databaseError.toException());
//            }
//        });
//    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(mContext, StoreAdminActivity.class);
        intent.putExtra(OrchidEntity.EXTRA_ORCHID, mOrchidItem);
        mContext.startActivity(intent);

//        Toast.makeText(mContext, mOrchidItem.getName(), Toast.LENGTH_LONG).show();
//        int itemPosition = getLayoutPosition();

        //      intent.putExtra("position", itemPosition + "");
//        OrchidEntity orchid =
//
//
//        mContext.startActivity(intent);
/*
        final ArrayList<OrchidEntity> orchids = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orchids");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    restaurants.add(snapshot.getValue(Restaurant.class));
                }

                int itemPosition = getLayoutPosition();

                Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                intent.putExtra("position", itemPosition + "");
                intent.putExtra("restaurants", Parcels.wrap(restaurants));

                mContext.startActivity(intent);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        */
    }
}

