package xyz.alviksar.orchidarium;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.util.GlideApp;

/**
 * Implementation of App Widget functionality.
 */
public class OrchidWidgetProvider extends AppWidgetProvider {

    private AppWidgetTarget appWidgetTarget;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);

    //    views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);



    }
    /*
    https://futurestud.io/tutorials/glide-loading-images-into-notifications-and-appwidgets
     */

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, OrchidWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
//        }
        String url = "https://firebasestorage.googleapis.com/v0/b/orchidarium-7df3d.appspot.com/o/orchid_photos%2Fimage%3A5418?alt=media&token=2bbdf645-4dcb-41f6-acbf-b10b7bbfe828";

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.orchid_widget);

        appWidgetTarget = new AppWidgetTarget(context, R.id.iv_orchid, remoteViews, appWidgetIds) {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };

        GlideApp.with(context.getApplicationContext())
                .asBitmap()
                .load(url)
                .into(appWidgetTarget);

        pushWidgetUpdate(context, remoteViews);
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

