package cn.asiontang.app.quick_open_js;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * 1. [构建应用微件  |  Android 开发者  |  Android Developers](https://developer.android.google.cn/guide/topics/appwidgets?hl=zh-cn)
 */
public class AppWidgetProvider extends android.appwidget.AppWidgetProvider
{
    private SharedPreferences mDefaultSharedPreferences;

    protected SharedPreferences getDefaultSharedPreferences(final Context context)
    {
        if (mDefaultSharedPreferences != null)
            return mDefaultSharedPreferences;
        return mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * [一个android应用向Home screen添加多个Widget_qjbagu的专栏-CSDN博客](https://blog.csdn.net/qjbagu/article/details/6694346)
     *
     * @param mAppWidgetId 如果在生成PendingIntent时，第二个参数相同，那么就相当于在原来的PendingIntent上修改，我们看到的当然是最后一次修改的结果。
     * @see AppWidgetConfigActivity#getOnClickPendingIntent(int, MainActivity.YeFile)
     */
    @SuppressWarnings("JavadocReference")
    private PendingIntent getOnClickPendingIntent(Context context, final int mAppWidgetId, final String uri)
    {
        final Intent dataIntent = new Intent(context, AppWidgetService.class);
        dataIntent.putExtra(AppWidgetService.EXTRA_KEY_URI, Uri.parse(uri));
        return PendingIntent.getService(context, mAppWidgetId, dataIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds)
    {
        SharedPreferences.Editor edit = getDefaultSharedPreferences(context).edit();
        for (int appWidgetId : appWidgetIds)
        {
            edit.remove(appWidgetId + ".NAME");
            edit.remove(appWidgetId + ".URI");
        }
        edit.apply();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int mAppWidgetId : appWidgetIds)
        {
            final String name = getDefaultSharedPreferences(context).getString(mAppWidgetId + ".NAME", null);
            final String uri = getDefaultSharedPreferences(context).getString(mAppWidgetId + ".URI", null);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            views.setTextViewText(android.R.id.title, name);
            views.setOnClickPendingIntent(android.R.id.widget_frame, getOnClickPendingIntent(context, mAppWidgetId, uri));

            appWidgetManager.updateAppWidget(mAppWidgetId, views);
        }
    }
}

