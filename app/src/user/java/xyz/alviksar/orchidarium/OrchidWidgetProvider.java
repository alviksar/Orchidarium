package xyz.alviksar.orchidarium;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;

import java.util.concurrent.ExecutionException;

import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * Implementation of App Widget functionality.
 */
public class OrchidWidgetProvider extends AppWidgetProvider {

//    private AppWidgetTarget appWidgetTarget;
//
//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);
//
//    //    views.setTextViewText(R.id.appwidget_text, widgetText);
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
//
//    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
//        ComponentName myWidget = new ComponentName(context, OrchidWidgetProvider.class);
//        AppWidgetManager manager = AppWidgetManager.getInstance(context);
//        manager.updateAppWidget(myWidget, remoteViews);
//    }

    public static void updateWidgets(Context context, String url)
            throws InterruptedException, ExecutionException {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, OrchidWidgetProvider.class));
        int maxWidth = 160, maxHeight = 160;  // For the first time, why not...
        for (int appWidgetId : appWidgetIds) {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int width = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            int height = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
            if (width > maxWidth) maxWidth = width;
            if (height > maxHeight) maxHeight = height;
        }
        /*
        https://github.com/bumptech/glide/wiki/Loading-and-Caching-on-Background-Threads
        */
        Bitmap myBitmap = GlideApp.with(context)
                .asBitmap()
                .load(url)
                .centerCrop()
                .submit(maxWidth, maxHeight)
                .get();
        for (int appWidgetId : appWidgetIds) {
            remoteViews.setImageViewBitmap(R.id.iv_orchid, myBitmap);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        OrchidIntentService.startActionUpdateWidgets(context);
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

