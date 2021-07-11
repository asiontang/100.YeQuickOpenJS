package cn.asiontang.app.quick_open_js;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RemoteViews;

/**
 * 1. [构建应用微件  |  Android 开发者  |  Android Developers](https://developer.android.google.cn/guide/topics/appwidgets?hl=zh-cn)
 */
public class AppWidgetConfigActivity extends MainActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //提示：当配置 Activity 首次打开时，请将 Activity 结果设为 RESULT_CANCELED 并注明 EXTRA_APPWIDGET_ID，
        // 如上面的第 5 步所示。这样，如果用户在到达末尾之前退出该 Activity，
        // 应用微件托管应用就会收到配置已取消的通知，因此不会添加应用微件。
        //https://developer.android.google.cn/guide/topics/appwidgets?hl=zh-cn
        setResult(RESULT_CANCELED, getIntent());
    }

    @Override
    protected void initView()
    {
        super.initView();

        ListView listView = findViewById(android.R.id.list);
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            final YeFile item = mAdapter.getItem(position);

            int mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            //NO_NEED:getDefaultSharedPreferences().edit()
            //NO_NEED:        .putString(mAppWidgetId + ".NAME", String.valueOf(item.Name))
            //NO_NEED:        .putString(mAppWidgetId + ".URI", item.mDocumentFile.getUri().toString())
            //NO_NEED:        .apply();

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);

            views.setTextViewText(android.R.id.title, item.Name);
            views.setOnClickPendingIntent(android.R.id.widget_frame, getOnClickPendingIntent(item));

            AppWidgetManager.getInstance(this).updateAppWidget(mAppWidgetId, views);

            onSaveClick();
        });
    }

    private PendingIntent getOnClickPendingIntent(final YeFile item)
    {
        final Intent dataIntent = new Intent(this, AppWidgetService.class);
        dataIntent.putExtra(AppWidgetService.EXTRA_KEY_URI, item.mDocumentFile.getUri());
        return PendingIntent.getService(this, 0, dataIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void onSaveClick()
    {
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
