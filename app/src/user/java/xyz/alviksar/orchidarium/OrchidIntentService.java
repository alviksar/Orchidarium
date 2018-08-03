package xyz.alviksar.orchidarium;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

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
    public static void startActionUpdateWidget(Context context, String param1, String param2) {
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
                handleActionUpdateWidget();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidget() {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
