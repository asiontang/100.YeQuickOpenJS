package cn.asiontang.app.quick_open_js;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RemoteViews;

import androidx.documentfile.provider.DocumentFile;

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
    protected boolean isTheFileExcluded(final DocumentFile file)
    {
        //可能希望桌面小组件能直接选中被排除的某些常用功能.例如一键停止脚本.
        //NO:return super.isTheFileExcluded(file);
        return false;
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

            getDefaultSharedPreferences().edit()
                    .putString(mAppWidgetId + ".NAME", String.valueOf(item.Name))
                    .putString(mAppWidgetId + ".URI", item.mDocumentFile.getUri().toString())
                    .apply();

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);

            views.setTextViewText(android.R.id.title, item.Name);
            views.setOnClickPendingIntent(android.R.id.widget_frame, getOnClickPendingIntent(mAppWidgetId, item));

            AppWidgetManager.getInstance(this).updateAppWidget(mAppWidgetId, views);

            onSaveClick();
        });
    }

    /**
     * [一个android应用向Home screen添加多个Widget_qjbagu的专栏-CSDN博客](https://blog.csdn.net/qjbagu/article/details/6694346)
     *
     * @param mAppWidgetId 如果在生成PendingIntent时，第二个参数相同，那么就相当于在原来的PendingIntent上修改，我们看到的当然是最后一次修改的结果。
     * @see AppWidgetProvider#getOnClickPendingIntent(Context, int, String)
     */
    @SuppressWarnings("JavadocReference")
    private PendingIntent getOnClickPendingIntent(final int mAppWidgetId, final YeFile item)
    {
        final Intent dataIntent = new Intent(this, AppWidgetService.class);
        dataIntent.putExtra(AppWidgetService.EXTRA_KEY_URI, item.mDocumentFile.getUri());
        return PendingIntent.getService(this, mAppWidgetId, dataIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void onSaveClick()
    {
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
