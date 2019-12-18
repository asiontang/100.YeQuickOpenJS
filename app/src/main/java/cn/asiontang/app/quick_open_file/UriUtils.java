package cn.asiontang.app.quick_open_file;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.util.List;

/**
 * <h1>
 * 此工具类使用方法:
 * </h1>
 * <h2>1.在项目AndroidManifest.xml中增加以下代码</h2>
 * <pre>
 * <code>&lt;application&gt;
 *  &lt;provider
 *   android:name="androidx.core.content.FileProvider"
 *   android:authorities="${applicationId}.FileProvider" --代码中可使用context.getApplicationInfo().packageName，或BuildConfig.APPLICATION_ID
 * 获取 ${applicationId}
 *   android:exported="false" --必须为false，不然会报错崩溃。
 *   android:grantUriPermissions="true"&gt;
 *       	&lt;meta-data
 *       android:name="android.support.FILE_PROVIDER_PATHS"
 *       android:resource="@xml/file_paths" /&gt;
 *  &lt;/provider&gt;
 * 	&lt;/application&gt;
 * </code></pre>
 *
 * <h2>2.在项目res/xml中增加file_paths.xml</h2>
 * <pre><code>&lt;?xml version="1.0" encoding="utf-8"?&gt;
 * &lt;paths xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 * &lt;external-path
 * name="sd_card_data_dir"
 * path="xx_dir" /&gt;
 * &lt;/paths&gt;
 * </code></pre>
 *
 * <h2>3.在项目中替换所有Uri.fromFile</h2>
 * <p><b style='color:red;'>必须配合</b>{@link #grantPermission(Context, Intent,
 * Uri)}一起使用！否则无法正确赋予此方法返回的Uri读写权限。</p>
 * <br/>
 * <p>使用系统默认APP打开指定路径的图片的例子如下:</p>
 * <pre>
 * //老版本用法:
 * <code>final Intent intent = new Intent();
 * intent.setAction(Intent.ACTION_VIEW);
 * intent.setDataAndType(<b style='color:red;'>Uri.fromFile(path)</b>, "image/*");
 * context.startActivity(intent);
 * </code>
 * //新版本用法:
 * <code><b style='color:red;'>final Uri uri = UriUtils.fromFile(context, path);</b>
 *
 * final Intent intent = new Intent();
 * intent.setAction(Intent.ACTION_VIEW);
 * intent.setDataAndType(uri, "image/*");
 *
 * <b style='color:red;'>UriUtils.grantPermission(context, intent, uri);</b>
 *
 * context.startActivity(intent);
 * </code></pre>
 *
 * <h2>参考资料:</h2>
 * <li><a href="https://developer.android.com/reference/android/support/v4/content/FileProvider.html">FileProvider
 * | external-path 定义查询
 * | Android Developers</a></li>
 * <li><a href="https://developer.android.com/training/camera/photobasics.html">Taking Photos
 * Simply
 * | Android Developers</a></li>
 * <li><a href="https://developer.android.com/reference/android/content/Context.html#grantUriPermission(java.lang.String,%20android.net.Uri,%20int)">Context.grantUriPermission
 * | Android Developers</a></li>
 * <li><a href="http://www.phpstudy.net/c.php/112859.html">Android,_7.0系统拍照后，使用系统截图功能，截图保存时崩溃如何解决，Android
 * - phpStudy</a></li>
 */
public final class UriUtils
{
    /**
     * 用于替代  {@link Uri#fromFile(File)} 。以便在Android 7.0 及其之后的版本系统使用时不崩溃。
     * <p><b style='color:red;'>必须配合</b>{@link #grantPermission(Context, Intent,
     * Uri)}一起使用！否则无法正确赋予此方法返回的Uri读写权限。</p>
     * <br/>
     * <p>使用系统默认APP打开指定路径的图片的例子如下:</p>
     * <pre>
     * //老版本用法:
     * <code>final Intent intent = new Intent();
     * intent.setAction(Intent.ACTION_VIEW);
     * intent.setDataAndType(<b style='color:red;'>Uri.fromFile(path)</b>, "image/*");
     * context.startActivity(intent);
     * </code>
     * //新版本用法:
     * <code><b style='color:red;'>final Uri uri = UriUtils.fromFile(context, path);</b>
     *
     * final Intent intent = new Intent();
     * intent.setAction(Intent.ACTION_VIEW);
     * intent.setDataAndType(uri, "image/*");
     *
     * <b style='color:red;'>UriUtils.grantPermission(context, intent, uri);</b>
     *
     * context.startActivity(intent);
     * </code></pre>
     *
     * @param context 上下文
     * @param file    需要获取Uri格式的File地址
     * @return 低于 Android 7.0 系统版本返回 {@link Uri#fromFile(File)}。之后的版本全部通过{@link
     * FileProvider#getUriForFile(Context, String, File)} 来返回Uri.
     */
    //public static Uri fromFile(Context context, File file)
    //{
    //    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
    //        return Uri.fromFile(file);

    //    return FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".FileProvider", file);
    //}

    /**
     * @see #fromFile(Context, File)
     */
    //public static Uri fromFile(Context context, String filePath)
    //{
    //    return fromFile(context, new File(filePath));
    //}

    /**
     * Android 4.4 kitkat以上及以下根据uri获取路径的方法，例如从相册获取照片或文件时，从4.4以上的系统，就拿不到绝对路径了。
     * 此时再去{@link #grantPermission(Context, Intent, Uri)}请求授权的话,就提示没有权限了.所以需要从系统的Provider里获取原始路径,再去请求授权.
     */
    public static String getProviderOriginalFilePath(final Context context, final Uri uri)
    {
        return GetPathFromUri4kitkat.getPath(context, uri);
    }

    /**
     * <b style='color:red;'>必须配合</b> {@link #fromFile(Context, File)} 配套使用.需要先通过{@link
     * #fromFile(Context, File)}得到以 "content"
     * 开头的 Uri。然后才能给第三方应用临时赋予此Uri读写权限
     *
     * @param context 上下文
     * @param intent  即将传递给第三方应用的Intent
     * @param uri     需要赋予给第三方应用临时读写权限的Uri!
     */
    public static void grantPermission(final Context context, final Intent intent, final Uri uri)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return;

        try
        {
            //API 24(Android N)之后的系统,别的APP需要访问SDCard指定路径,必须由本APP提供权限才行．需要在 AndroidManifest.xml 里定义FileProvider
            if ((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if ((intent.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //将 uri读写权限 授权给 所有可打开的 Intent 应用
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resInfoList != null && resInfoList.size() > 0)
                for (ResolveInfo resolveInfo : resInfoList)
                    try
                    {
                        context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    catch (SecurityException se)
                    {
                        //碰到某些情况下会报错。
                        //SecurityException: Uid 10342 does not have permission to uri 0 @ content://com.android.providers.media.documents/document/image%3A75954
                        //LogEx.w("UriUtils.grantPermission.for", resolveInfo.activityInfo.packageName, se);
                        //se.printStackTrace();
                    }
        }
        catch (Exception ignore)
        {
            //LogEx.w("UriUtils.grantPermission.outer", ignore);
            //ignore.printStackTrace();
        }
    }

    /**
     * <li><a href="https://www.cnblogs.com/huangzhen22/p/5160128.html">Android 4.4 根据uri获取路径的方法 - 黄忠 - 博客园</a></li>
     * <li><a href="https://www.2cto.com/kf/201502/376975.html">Android 4.4 kitkat以上及以下根据uri获取路径的方法 - Android移动开发技术文章_手机开发 - 红黑联盟</a></li>
     */
    public static class GetPathFromUri4kitkat
    {
        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
        {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try
            {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst())
                {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            }
            finally
            {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }

        /**
         * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
         */
        @SuppressLint("NewApi")
        public static String getPath(final Context context, final Uri uri)
        {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
            {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri))
                {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type))
                    {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri))
                {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri))
                {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type))
                    {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    else if ("video".equals(type))
                    {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }
                    else if ("audio".equals(type))
                    {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme()))
            {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme()))
            {
                return uri.getPath();
            }
            return null;
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        public static boolean isDownloadsDocument(Uri uri)
        {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        public static boolean isExternalStorageDocument(Uri uri)
        {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        public static boolean isMediaDocument(Uri uri)
        {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }
    }
}