package xyz.alviksar.orchidarium;

import android.app.Activity;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.ExecutionException;

import xyz.alviksar.orchidarium.util.GlideApp;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class OrchidIntentService extends IntentService {
    private static final String ACTION_UPDATE_WIDGET = "xyz.alviksar.orchidarium.action.update_widget";

//    private static final String EXTRA_PARAM1 = "xyz.alviksar.orchidarium.user.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "xyz.alviksar.orchidarium.user.extra.PARAM2";

    public OrchidIntentService() {
        super("OrchidIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, OrchidIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                try {
                    handleActionUpdateWidgets();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Handle action update widget in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidgets() throws ExecutionException, InterruptedException {

        Context context = getApplicationContext();
        String url = "https://firebasestorage.googleapis.com/v0/b/orchidarium-7df3d.appspot.com/o/orchid_photos%2Fimage%3A5418?alt=media&token=2bbdf645-4dcb-41f6-acbf-b10b7bbfe828";

        OrchidWidgetProvider.updateWidgets(context, url);
    }


}
