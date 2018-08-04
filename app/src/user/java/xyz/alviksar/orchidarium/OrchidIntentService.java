package xyz.alviksar.orchidarium;

import android.app.Activity;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;
import xyz.alviksar.orchidarium.model.OrchidEntity;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class OrchidIntentService extends IntentService {
    private static final String ACTION_UPDATE_WIDGET = "xyz.alviksar.orchidarium.action.update_widget";


    public OrchidIntentService() {
        super("OrchidIntentService");
    }

    /**
     * Starts this service to perform update a widget image. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, OrchidIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                    handleActionUpdateWidgets();
            }
        }
    }


    /**
     * Handle action update widget in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidgets() {
        showRandomOrchid();
    }

    public void showRandomOrchid() {
        final ArrayList<OrchidEntity> orchidList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("orchids");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot orchidSnapshot : dataSnapshot.getChildren()) {
                    orchidList.add(orchidSnapshot.getValue(OrchidEntity.class));
                }
                int i = (int) (Math.random() * orchidList.size());
                OrchidWidgetProvider.
                        updateWidgetsInMainUiTread(getApplicationContext(), orchidList.get(i));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.d("Error trying to get data for order" +
                        "" + databaseError);
            }
        });
    }


}
