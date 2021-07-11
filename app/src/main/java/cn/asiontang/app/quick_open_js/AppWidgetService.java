package cn.asiontang.app.quick_open_js;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

/**
 *
 */
public class AppWidgetService extends Service
{
    public static final String EXTRA_KEY_URI = "Uri";

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        Uri uri = intent.getParcelableExtra(EXTRA_KEY_URI);

        MainActivity.startAutoJsByUri(getApplication(), uri);

        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
