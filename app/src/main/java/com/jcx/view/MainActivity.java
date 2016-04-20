package com.jcx.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcx.R;
import com.jcx.communication.BlueToothImp;
import com.jcx.util.FileFilter;
import com.jcx.util.FileOper;
import com.jcx.util.FileUtil;
import com.jcx.util.ListViewSwipeGesture;
import com.jcx.util.ZipUtil;
import com.jcx.view.adapter.AsynLoadImg;
import com.jcx.view.adapter.MyAdapter;
import com.zxing.activity.CaptureActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Cui on 16-4-6.
 */
public class MainActivity extends AppCompatActivity{
    private String rootDictionary=null;
    private ListView file_list;
    private TextView path_info,tv_title;
    private Context context;
    private File[] listfiles;
    private MyAdapter adapter;
    private File parentFilePath=null,fileUsedInContextMenu;
    private AsynLoadImg asynLoadImg;

    private String fileType=null;
    private EditText editText;
    private int dialogTag=-1;//0：标志重命名的dialog；1：标志兴建文件夹的dialog

    private FileOper fileOper;
    private String srcFilePath=null;
    private String desFilePath=null;
    private String currentFilePath=null;
    private FloatingActionButton fab_confirm,fab_cancel;
    private CharSequence[] items;

    private String appFolder=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_confirm= (FloatingActionButton) findViewById(R.id.fab_confirm);
        fab_cancel= (FloatingActionButton) findViewById(R.id.fab_cancel);
        fab_confirm.hide();
        fab_cancel.hide();

        forceShowOverflowMenu();
        context=this;
        initUI();
    }

    /**
     * 对文件进行过滤和排序
     * @return File[]数组listfiles，存储SDcard中的文件
     */
    private File[] getListfiles(File getFileLists){
        File file=getFileLists;
        listfiles=file.listFiles(new FileFilter());
        listfiles= FileUtil.sort(listfiles);
        return listfiles;
    }
    /**
     * 绑定listview,为listview绑定监听,将SDcard中的File显示到界面中
     */
    private void initUI() {
        tv_title= (TextView) findViewById(R.id.tv_title);
        file_list= (ListView) findViewById(R.id.file_list);
        file_list.setOnItemClickListener(new fileItemClickListener());
        file_list.setOnItemLongClickListener(new fileItemLongClickListener());
        path_info= (TextView) findViewById(R.id.pathinfo);

        //获得文件的名称然后显示到listview中
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            currentFilePath=Environment.getExternalStorageDirectory().getPath();

            appFolder=currentFilePath+"/FT/files";
            File fileTemp0=new File(appFolder);
            if (!fileTemp0.exists()) {
                newFolder("/FT/files");
            }

            int indexOf=currentFilePath.lastIndexOf("/");
            rootDictionary=currentFilePath.substring(0,indexOf);
            File fileTemp=new File(rootDictionary);
            rootDictionary=fileTemp.getName();
            System.out.println("rootDictionary:"+rootDictionary);
            listfiles=getListfiles(Environment.getExternalStorageDirectory());
            adapter=new MyAdapter(context,listfiles,file_list);
            file_list.setAdapter(adapter);

            ListViewSwipeGesture touchListener = new ListViewSwipeGesture(file_list, swipeListener, this);
            touchListener.SwipeType	=	ListViewSwipeGesture.Item_swipe_firstInvoked;    //设置两个选项列表项的背景
            file_list.setOnTouchListener(touchListener);

        }else {

            Toast.makeText(context, getString(R.string.sd_readerror), Toast.LENGTH_SHORT).show();
        }
    }
    ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {

        @Override
        public void HalfSwipeListView(int position) {
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this,"发送", Toast.LENGTH_SHORT).show();
        }

    };
    /**
     * 设置点击菜单时menu显示在actionbar下面
     */
    private void forceShowOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                    System.out.println("setOverFlowItemIcon successed");
                } catch (Exception e) {
                }
            }
            else {
                System.out.println("setOverFlowItemIcon failed");
            }
        }else {
            System.out.println("setOverFlowItemIcon failed2");
        }
        return super.onMenuOpened(featureId, menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    /**
     * overFlowMenu菜单的点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_fileAccept:

                items=new CharSequence[3];
                items[0]=getString(R.string.fileAccept_bluetooth);
                items[1]=getString(R.string.fileAccept_hotspot);
                items[2]=getString(R.string.fileAccept_network);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle(R.string.acceptFile_title);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_bluetooth))) {
                            //TODO -------->通过蓝牙接受文件
                        }else if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点接收文件
                        }else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络接收文件
                        }
                    }
                });
                builder.show();
                break;
            case R.id.action_creatqrcode:
                Intent intent2=new Intent(MainActivity.this, CreateQRCodeActivity.class);
                startActivity(intent2);
                break;
            case R.id.action_scanqrcode:
                Intent intent=new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.action_new_folder:
                AlertDialog.Builder new_folder_dialog=new AlertDialog.Builder(context);
                new_folder_dialog.setTitle(R.string.menu_new_folder);
                editText=new EditText(context);
                editText.setHint(R.string.input_folderName);
                new_folder_dialog.setView(editText);
                dialogTag=1;
                new_folder_dialog.setPositiveButton(R.string.rename_sure, new dialogOnConfirmClickListener());
                new_folder_dialog.setNegativeButton(R.string.rename_undo, new dialogOnCancelClickListener());
                new_folder_dialog.show();
                break;
            case R.id.action_file_paste:
                desFilePath=currentFilePath;
                if (srcFilePath != null) {
                    int indexOf = srcFilePath.lastIndexOf("/");
                    int srcFilePathLength = srcFilePath.length();
                    String fileNameOfCopyed = srcFilePath.substring(indexOf + 1, srcFilePathLength);
                    desFilePath = srcFilePath + "/" + fileNameOfCopyed;
                    File fileUsedForPaste = new File(desFilePath);
                    if (!fileUsedForPaste.exists()) {
                        boolean fileCopyResult = fileOper.copy(srcFilePath, currentFilePath);
                        if (fileCopyResult) {
                            srcFilePath = null;
                            desFilePath = null;
                            viewUpdate();
                            Toast.makeText(context, R.string.filePaste_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, R.string.filePaste_fail, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, R.string.file_already_exist, Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * ContextMenu功能菜单的点击事件的处理
     * @param item 获得被点击的菜单
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position=menuInfo.position;
        fileUsedInContextMenu=listfiles[position];
        switch (item.getItemId())
        {
            case R.id.menu_context_copy:
                srcFilePath=listfiles[position].getAbsolutePath();
                System.out.println("srcFilePath:"+srcFilePath);
                if (srcFilePath!=null) {
                    Toast.makeText(context, R.string.fileCopy_success, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_context_move:
                tv_title.setText(R.string.tv_text_choseTargetDictionary);
                srcFilePath=listfiles[position].getAbsolutePath();
                fab_confirm.show();
                fab_cancel.show();
                floatingActionButtonClickListener();
                break;
            case R.id.menu_file_send:
                items=new CharSequence[3];
                items[0]=getString(R.string.fileSend_bluetooth);
                items[1]=getString(R.string.fileSend_hotspot);
                items[2]=getString(R.string.fileSend_network);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle(R.string.sendFile_title);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileSend_bluetooth))) {
                            //TODO -------->通过蓝牙发送文件
                        }else if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点发送文件
                        }else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络发送文件
                        }
                    }
                });
                builder.show();
                break;
            case R.id.menu_context_rename:
                AlertDialog.Builder dialog=new AlertDialog.Builder(context);
                dialog.setTitle(R.string.rename);
                editText=new EditText(context);
                editText.setHint(R.string.input_newname);
                editText.setText(fileUsedInContextMenu.getName());
                editText.selectAll();
                dialog.setView(editText);
                dialogTag=0;
                dialog.setPositiveButton(R.string.rename_sure, new dialogOnConfirmClickListener());
                dialog.setNegativeButton(R.string.rename_undo, new dialogOnCancelClickListener());
                dialog.show();
                break;
            case R.id.menu_context_delete:
                boolean fileDeleteReturn;
                fileDeleteReturn=fileOper.delete(listfiles[position].getAbsolutePath());
                viewUpdate();
                asynLoadImg.isAllow=true;
                if (fileDeleteReturn) {
                    Toast.makeText(context, R.string.fileDelete_success, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, R.string.fileDelete_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_file_zip:
                srcFilePath=listfiles[position].getAbsolutePath();
                ZipUtil zipUtil=new ZipUtil();
                String zipFileName=srcFilePath+".zip";
                try {
                    zipUtil.zip(srcFilePath, zipFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_file_unzip:
                srcFilePath=listfiles[position].getAbsolutePath();
                ZipUtil zipUtil1=new ZipUtil();
                String destFilePath=currentFilePath+"/"+"FT"+"/files";
                try {
                    zipUtil1.unZip(srcFilePath, destFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            String result=data.getExtras().getString("result");
            BlueToothImp blueToothImp=new BlueToothImp(this);
            blueToothImp.connect(result);
        }
    }
    /**
     * 设置listView item的点击监听事件
     */
    public class fileItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //点击文件夹的处理
            if (listfiles[position].isDirectory())
            {
                parentFilePath=listfiles[position].getParentFile();
                path_info.append("/" + listfiles[position].getName());

                currentFilePath=currentFilePath+"/"+listfiles[position].getName();

                listfiles=listfiles[position].listFiles(new FileFilter());
                listfiles= FileUtil.sort(listfiles);

                adapter.upDate(listfiles);

            }else{//点击文件的处理
                //TODO ----->do something when click a file item.
            }
        }

    }

    /**
     * 监听floatActionbutton的点击事件；floatActionButton用来选择目标文件夹移动
     */
    public void floatingActionButtonClickListener(){
        fab_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("srcFilePath:"+srcFilePath+"\n"+"currentFilePath:"+currentFilePath);
                if (srcFilePath != null) {
                    boolean fileMoveResult=fileOper.move(srcFilePath,currentFilePath);
                    if (fileMoveResult) {
                        Toast.makeText(context,R.string.fileMove_success,Toast.LENGTH_SHORT).show();
                        viewUpdate();
                    }else {
                        Toast.makeText(context,R.string.fileMove_fail,Toast.LENGTH_SHORT).show();
                    }
                }
                fab_confirm.hide();
                fab_cancel.hide();
            }
        });
        fab_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                srcFilePath=null;
                fab_confirm.hide();
                fab_cancel.hide();
            }
        });
        tv_title.setText(R.string.tv_text_default);
    }

    /**
     * 设置listView item的长按事件，弹出Contextmenu功能选择菜单
     */
    public class fileItemLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (listfiles[position].isDirectory())
            {
                fileType=getString(R.string.folder);
            }else {
                fileType=getString(R.string.file);
            }
            file_list.setOnCreateContextMenuListener(menuListener);
            fileOper=new FileOper();

            return false;
        }
    }

    ListView.OnCreateContextMenuListener menuListener=new ListView.OnCreateContextMenuListener(){

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(getString(R.string.menu_HeaderTitle_action)+fileType);
            MenuInflater menuInflater=getMenuInflater();
            menuInflater.inflate(R.menu.file_menu,menu);
        }
    };

    /**
     * dialog输入框“取消”按钮的点击事件
     */
    public class dialogOnCancelClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    }
    /**
     * dialog输入框“确定”按钮的点击事件
     */
    public class dialogOnConfirmClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String name = editText.getText().toString();
            if (editText != null && !"".equals(name)) {
                switch (dialogTag)
                {
                    case 0:
                        reNmae(name);
                        dialogTag=-1;
                        break;
                    case 1:
                        newFolder(name);
                        dialogTag=-1;
                        break;
                    default:
                        break;
                }
            } else {
                Toast.makeText(context, getString(R.string.empty_warning), Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 更新视图
     */
    private void viewUpdate(){
        File fileNeedToBeUpdate=new File(currentFilePath);
        adapter.upDate(getListfiles(fileNeedToBeUpdate));
        if (adapter.state!=null)
        {
            file_list.onRestoreInstanceState(adapter.state);
        }
    }
    /**
     * 设置手机back健的点击事件
     * @param keyCode
     * @param event 点击事件的类型
     * @return 返回点击事件的结果，bollean类型，true表示操作成功，反之失败
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK){
            if(parentFilePath==null){
                finish();
            }else
            if (!parentFilePath.getName().equals("")&&!parentFilePath.getName().equals(rootDictionary)) {
                listfiles = parentFilePath.listFiles(new FileFilter());
                listfiles = FileUtil.sort(listfiles);
                adapter.upDate(listfiles);
                parentFilePath=parentFilePath.getParentFile();

                String path=path_info.getText().toString();
                int indexOf=path.lastIndexOf("/");
                path=path.substring(0,indexOf);
                path_info.setText(path);

                indexOf=currentFilePath.lastIndexOf("/");
                currentFilePath=currentFilePath.substring(0, indexOf);

                if (adapter.state!=null)
                {
                    file_list.setAdapter(adapter);
                    file_list.onRestoreInstanceState(adapter.state);
                }
                asynLoadImg.isAllow=true;
            }else {
                finish();
            }
        }
        return true;
    }
    /**
     * 新建文件夹
     * @param name 从dialog输入框中获得的新文件夹的名字
     */
    private void newFolder(String name){
        File fileUsedForNewFolder=new File(currentFilePath+"/"+name);
        if (!fileUsedForNewFolder.exists())
        {
            boolean newFolderResult=fileUsedForNewFolder.mkdir();
            if (newFolderResult)
            {
                viewUpdate();
                Toast.makeText(context,getString(R.string.newFolder_success),Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context,getString(R.string.newFolder_fail),Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(context,getString(R.string.folderIsExist),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 文件的重命名
     * @param name 从dialog中获得的文件的新名字
     */
    private void reNmae(String name){
        String path1=fileUsedInContextMenu.getPath();
        int indexof=path1.lastIndexOf("/");
        path1=path1.substring(0,indexof);
        boolean renameResult=fileUsedInContextMenu.renameTo(new File(path1+"/"+name));
        if (renameResult)
        {
            viewUpdate();
            Toast.makeText(context, getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
        }
    }
}
