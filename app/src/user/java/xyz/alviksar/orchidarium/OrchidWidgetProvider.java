package xyz.alviksar.orchidarium;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.ui.DetailActivity;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * Implementation of App Widget functionality.
 */
public class OrchidWidgetProvider extends AppWidgetProvider {

    /**
     * Updates widget
     *
     * @param context  Context used to get the widget
     * @param remoteViews Widget views
     */
    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, OrchidWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }

    /**
     *  Updates widget image and set OnClickPendingIntent.
     *
     * @param context
     * @param orchid
     */
    public static void updateWidgetsInMainUiTread(final Context context, OrchidEntity orchid) {
        /*
        https://futurestud.io/tutorials/glide-loading-images-into-notifications-and-appwidgets
         */
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, OrchidWidgetProvider.class));
        AppWidgetTarget appWidgetTarget =
                new AppWidgetTarget(context, R.id.iv_orchid, remoteViews, appWidgetIds) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource,
                                        Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };

        // Show a new photo
        GlideApp
                .with(context.getApplicationContext())
                .asBitmap()
                .load(orchid.getNicePhoto())
                .into(appWidgetTarget);
        remoteViews.setTextViewText(R.id.tv_orchid_name, orchid.getName());

        // Make intent to start an appropriate detail activity
        Intent intent = new Intent(context, DetailActivity.class);

        // Make the pending intent unique
        // https://stackoverflow.com/a/5158408/9682456
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        intent.putExtra(OrchidEntity.EXTRA_ORCHID, orchid);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        // Get the PendingIntent containing the entire back stack
        PendingIntent pendIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.iv_orchid, pendIntent);

        // Update widget
        pushWidgetUpdate(context, remoteViews);
    }


//    public static void updateWidgetsInBackground(Context context, OrchidEntity orchid)
//    {
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
//                new ComponentName(context, OrchidWidgetProvider.class));
//        int maxWidth = 110, maxHeight = 110;  // for the first time, why not...
//        for (int appWidgetId : appWidgetIds) {
//            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
//            int width = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
//            int height = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
//            if (width > maxWidth) maxWidth = width;
//            if (height > maxHeight) maxHeight = height;
//        }
//        /*
//        https://github.com/bumptech/glide/wiki/Loading-and-Caching-on-Background-Threads
//        */
//        Bitmap widgetBitmap = null;
//        try {
//            widgetBitmap = GlideApp.with(context)
//                    .asBitmap()
//                    .load(orchid.getNicePhoto())
//                    .centerCrop()
//                    .submit(maxWidth, maxHeight)
//                    .get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        for (int appWidgetId : appWidgetIds) {
//            remoteViews.setImageViewBitmap(R.id.iv_orchid, widgetBitmap);
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//        }
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Call IntentService to gat new data and update itself
        OrchidIntentService.startActionUpdateWidgets(context);
    }


}

