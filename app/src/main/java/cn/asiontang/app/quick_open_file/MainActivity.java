package cn.asiontang.app.quick_open_file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;
import eu.chainfire.libsuperuser.Shell;


/**
 * <pre>
 * </pre>
 */
@SuppressLint({"StaticFieldLeak", "DefaultLocale"})
public class MainActivity extends Activity
{
    private static final String KEY_STR_SELECTED_FOLDER_URI = "UserSelectedFolderUri";
    private static final String TAG = "-------";
    private final List<YeFile> mFileList = new ArrayList<>();
    private BaseAdapterEx3<YeFile> mAdapter;
    private Uri mUserSelectedFolderUri;

    private void initData()
    {
        final String uriStr = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_STR_SELECTED_FOLDER_URI, null);
        if (!TextUtils.isEmpty(uriStr))
            mUserSelectedFolderUri = Uri.parse(uriStr);
    }

    private void initView()
    {
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(mAdapter = new BaseAdapterEx3<YeFile>(this, R.layout.activity_main_listview_item, mFileList)
        {
            @Override
            public void convertView(final ViewHolder viewHolder, final YeFile item)
            {
                viewHolder.getTextView(android.R.id.title).setText(item.Name);
            }
        });
        Toast.makeText(this, "长按任意项重新选择目录", Toast.LENGTH_SHORT).show();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                startSelectFolder();
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                //√成功的尝试4: 通过Root直接调用RunIntentActivity来绕开 android:exported=False 检测,直接启动脚本.
                final YeFile item = mAdapter.getItem(position);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //必须使用Root(SU)启动AM,否则会报错:ActivityManager: Permission Denial: startActivity asks to run as user -2 but is calling from user 0; this requires android.permission.INTERACT_ACROSS_USERS_FULL

                        //方法1:
                        //Shell.SU.run(String.format("am start -n org.autojs.autojspro/org.autojs.autojs.external.open.RunIntentActivity --es path \"%s\"", UriUtils.getProviderOriginalFilePath(getApplicationContext(), item.mDocumentFile.getUri())));

                        //方法2:
                        Shell.SU.run(String.format("am start -n org.autojs.autojspro/org.autojs.autojs.external.open.RunIntentActivity -d \"%s\"", UriUtils.getProviderOriginalFilePath(getApplicationContext(), item.mDocumentFile.getUri())));
                    }
                }).start();

                //×失败的尝试3:提示 android:exported=False 无法跨APP调用
                //Intent intent = new Intent();
                //intent.setClassName("org.autojs.autojspro", "org.autojs.autojs.external.open.RunIntentActivity");
                //intent.putExtra("path", item.mDocumentFile.getUri().toString());
                //startActivity(intent);

                //×失败的尝试2:会提示验证失败.而且打开的是"选择脚本"的界面,而不是运行脚本.
                //Intent intent = new Intent();
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.setClassName("org.autojs.autojspro", "org.autojs.autojs.external.tasker.PluginActivity");
                //intent.setAction("com.twofortyfouram.locale.intent.action.EDIT_SETTING");
                //startActivity(intent);

                //×失败的尝试1:会提示验证失败.
                //Intent intent = new Intent();
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.setAction(Intent.ACTION_VIEW);
                //intent.setDataAndType(item.mDocumentFile.getUri(), "application/x-javascript");
                //startActivity(intent);
            }
        });
    }

    /**
     * <li><a href="https://stackoverflow.com/questions/26744842/how-to-use-the-new-sd-card-access-api-presented-for-android-5-0-lollipop">How to use the new SD card access API presented for Android 5.0 (Lollipop)? - Stack Overflow</a></li>
     */
    private void loadFiles()
    {
        try
        {
            getContentResolver().takePersistableUriPermission(mUserSelectedFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //DocumentsContract.ACTION_DOCUMENT_SETTINGS;
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, mUserSelectedFolderUri);

            // List all existing files inside picked directory
            for (DocumentFile file : pickedDir.listFiles())
            {
                if (!file.isFile())
                    continue;
                mFileList.add(new YeFile(file));
            }
            mAdapter.refresh();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (resultCode != RESULT_OK || data == null || data.getData() == null)
            return;

        mUserSelectedFolderUri = data.getData();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KEY_STR_SELECTED_FOLDER_URI, data.getData().toString()).apply();
        loadFiles();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initData();

        initView();

        if (mUserSelectedFolderUri == null)
            startSelectFolder();
        else
            loadFiles();
    }

    private void startSelectFolder()
    {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 0);
    }

    public class YeFile
    {
        public final DocumentFile mDocumentFile;
        public CharSequence Name;

        public YeFile(final DocumentFile file)
        {
            Name = file.getName();
            mDocumentFile = file;
        }
    }
}
