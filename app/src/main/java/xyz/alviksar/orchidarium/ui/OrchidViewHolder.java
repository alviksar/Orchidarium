package xyz.alviksar.orchidarium.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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

    OrchidEntity orchidItem;


    public OrchidViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindOrchid(OrchidEntity orchid) {
        mNameTextView.setText(orchid.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        mPriceTextView.setText(currencyFormat.format(orchid.getRetailPrice()));
        orchidItem = orchid;
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
        Toast.makeText(mContext, orchidItem.getName(), Toast.LENGTH_LONG).show();
        int itemPosition = getLayoutPosition();

        Intent intent = new Intent(mContext, StoreAdminActivity.class);
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

