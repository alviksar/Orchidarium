package xyz.alviksar.orchidarium;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.ExecutionException;

import xyz.alviksar.orchidarium.model.OrchidEntity;
import xyz.alviksar.orchidarium.ui.DetailActivity;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * Implementation of App Widget functionality.
 */
public class OrchidWidgetProvider extends AppWidgetProvider {

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, OrchidWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }

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

        GlideApp
                .with(context.getApplicationContext())
                .asBitmap()
                .load(orchid.getNicePhoto())
                .into(appWidgetTarget);
        remoteViews.setTextViewText(R.id.tv_orchid_name, orchid.getName());

        Intent intent = new Intent(context, DetailActivity.class);
        // Make the pending intent unique
        // https://stackoverflow.com/a/5158408/9682456
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Identifies the particular widget
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
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
  //      OrchidIntentService.startActionUpdateWidgets(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        OrchidIntentService.startActionUpdateWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

