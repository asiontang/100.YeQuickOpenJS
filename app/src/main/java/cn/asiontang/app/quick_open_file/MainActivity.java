package cn.asiontang.app.quick_open_file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String KEY_INT_SELECTED_FOLDER_INDEX = "UserSelectedFolderUriIndex";
    private static final String KEY_STR_SELECTED_FOLDER_URI_LIST = "UserSelectedFolderUriList";
    private static final String TAG = "-------";
    private final List<YeFile> mFileList = new ArrayList<>();
    private final List<Uri> mUserSelectedFolderUriList = new ArrayList<>();
    private BaseAdapterEx3<YeFile> mAdapter;
    private int mUserSelectedFolderUriIndex = 0;
    private SharedPreferences mDefaultSharedPreferences;
    private RadioGroup mLayoutFolderItems;

    private void excludeTheFile(final YeFile item)
    {
        getDefaultSharedPreferences().edit().putBoolean(item.mDocumentFile.getUri().toString(), true).apply();
        if (mAdapter.getOriginaItems() == null)
            return;

        mAdapter.getOriginaItems().remove(item);
        mAdapter.refresh();

        Toast.makeText(this, item.Name + " 排除成功", Toast.LENGTH_SHORT).show();
    }

    private SharedPreferences getDefaultSharedPreferences()
    {
        if (mDefaultSharedPreferences != null)
            return mDefaultSharedPreferences;
        return mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initData()
    {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences();

        mUserSelectedFolderUriIndex = defaultSharedPreferences.getInt(KEY_INT_SELECTED_FOLDER_INDEX, 0);

        final String[] uriStr = defaultSharedPreferences.getString(KEY_STR_SELECTED_FOLDER_URI_LIST, "").split("\f");
        if (uriStr.length > 0)
            for (String s : uriStr)
                if (!TextUtils.isEmpty(s))
                    mUserSelectedFolderUriList.add(Uri.parse(s));
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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                excludeTheFile(mAdapter.getItem(position));
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

        mLayoutFolderItems = findViewById(R.id.layoutFolderItems);
        mLayoutFolderItems.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId)
            {
                if (checkedId == -1)
                    return;
                RadioButton view = group.findViewById(checkedId);
                if (view == null || !view.isChecked())
                    return;
                mUserSelectedFolderUriIndex = mLayoutFolderItems.indexOfChild(view) - 2/*排除前面默认的+号按钮和|分隔符*/;
                getDefaultSharedPreferences().edit().putInt(KEY_INT_SELECTED_FOLDER_INDEX, mUserSelectedFolderUriIndex).apply();

                loadFiles((Uri) view.getTag());
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                startSelectFolder();
            }
        });
    }

    /**
     * <li><a href="https://stackoverflow.com/questions/26744842/how-to-use-the-new-sd-card-access-api-presented-for-android-5-0-lollipop">How to use the new SD card access API presented for Android 5.0 (Lollipop)? - Stack Overflow</a></li>
     */
    private void loadFiles(Uri mUserSelectedFolderUri)
    {
        try
        {
            getContentResolver().takePersistableUriPermission(mUserSelectedFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //DocumentsContract.ACTION_DOCUMENT_SETTINGS;
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, mUserSelectedFolderUri);

            // List all existing files inside picked directory
            mFileList.clear();
            for (DocumentFile file : pickedDir.listFiles())
            {
                if (!file.isFile())
                    continue;
                //过滤掉手动排除过的文件
                if (getDefaultSharedPreferences().getBoolean(file.getUri().toString(), false))
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

        if (!mUserSelectedFolderUriList.contains(data.getData()))
            mUserSelectedFolderUriList.add(data.getData());

        //添加选择的目录到配置文件里
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences();
        String[] uriSet = defaultSharedPreferences.getString(KEY_STR_SELECTED_FOLDER_URI_LIST, "").split("\f");
        List<String> list = new ArrayList<>(Arrays.asList(uriSet));
        if (!list.contains(data.getData().toString()))
            list.add(data.getData().toString());
        defaultSharedPreferences.edit().putString(KEY_STR_SELECTED_FOLDER_URI_LIST, TextUtils.join("\f", list.toArray(new String[0]))).apply();

        //加载当前目录的最新数据
        loadFiles(data.getData());

        //刷新目录列表
        refreshUserSelectedFolderItemList(data.getData());
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initData();

        initView();

        //刷新目录列表
        if (mUserSelectedFolderUriList.isEmpty())
            startSelectFolder();
        else
        {
            Uri mUserSelectedFolderUri = mUserSelectedFolderUriList.get(Math.min(mUserSelectedFolderUriIndex, mUserSelectedFolderUriList.size() - 1));
            refreshUserSelectedFolderItemList(mUserSelectedFolderUri);
            loadFiles(mUserSelectedFolderUri);
        }
    }

    private void refreshUserSelectedFolderItemList(Uri mUserSelectedFolderUri)
    {
        //先移除旧的.
        if (mLayoutFolderItems.getChildCount() > 2)
            mLayoutFolderItems.removeViews(2, mLayoutFolderItems.getChildCount() - 2);

        //再添加新的.
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (int i = 0; i < mUserSelectedFolderUriList.size(); i++)
        {
            final Uri uri = mUserSelectedFolderUriList.get(i);
            final RadioButton child = (RadioButton) layoutInflater.inflate(R.layout.activity_main_folder_item, mLayoutFolderItems, false);
            child.setId(i);
            child.setTag(uri);
            child.setChecked(uri.equals(mUserSelectedFolderUri));
            child.setText(uri.getLastPathSegment().substring(uri.getLastPathSegment().indexOf("/") + 1).replace("primary:", ""));
            child.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(final View v)
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("是否删除" + child.getText())
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which)
                                {
                                    int index = mLayoutFolderItems.indexOfChild(child);

                                    mUserSelectedFolderUriList.remove(uri);

                                    Uri mUserSelectedFolderUri = mUserSelectedFolderUriList.get(Math.min(index, mUserSelectedFolderUriList.size() - 1));
                                    refreshUserSelectedFolderItemList(mUserSelectedFolderUri);
                                    loadFiles(mUserSelectedFolderUri);

                                    String[] uriSet = getDefaultSharedPreferences().getString(KEY_STR_SELECTED_FOLDER_URI_LIST, "").split("\f");
                                    {
                                        List<String> list = new ArrayList<>(Arrays.asList(uriSet));
                                        list.remove(uri.toString());
                                        getDefaultSharedPreferences().edit().putString(KEY_STR_SELECTED_FOLDER_URI_LIST, TextUtils.join("\f", list.toArray(new String[0]))).apply();
                                    }

                                    Toast.makeText(MainActivity.this, child.getText() + " 删除成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    return false;
                }
            });
            mLayoutFolderItems.addView(child);
        }
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
