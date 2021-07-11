package cn.asiontang.app.quick_open_js;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

/**
 * 1. [构建应用微件  |  Android 开发者  |  Android Developers](https://developer.android.google.cn/guide/topics/appwidgets?hl=zh-cn)
 */
public class AppWidgetProvider extends android.appwidget.AppWidgetProvider
{
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.e("--------------", "onUpdate() called with: context = [" + context + "], appWidgetManager = [" + appWidgetManager + "], appWidgetIds = [" + appWidgetIds + "]");
        //final int N = appWidgetIds.length;
        //// Perform this loop procedure for each App Widget that belongs to this provider
        //for (int i = 0; i < N; i++)
        //{
        //    int appWidgetId = appWidgetIds[i];
        //    // Create an Intent to launch ExampleActivity
        //    Intent intent = new Intent(context, MainActivity.class);
        //    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //    // Get the layout for the App Widget and attach an on-click listener
        //    // to the button
        //    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        //    views.setOnClickPendingIntent(android.R.id.text1, pendingIntent);
        //    // Tell the AppWidgetManager to perform an update on the current app widget
        //    appWidgetManager.updateAppWidget(appWidgetId, views);
        //}
    }
}

