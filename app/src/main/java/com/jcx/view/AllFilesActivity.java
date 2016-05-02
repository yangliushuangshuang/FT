package com.jcx.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcx.R;
import com.jcx.communication.BlueToothImp;
import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.communication.WifiDirectImp;
import com.jcx.util.FileFilter;
import com.jcx.util.FileOper;
import com.jcx.util.FileUtil;
import com.jcx.util.GetFileSize;
import com.jcx.util.Util;
import com.jcx.util.ZipUtil;
import com.jcx.view.adapter.AsynLoadImg;
import com.jcx.view.adapter.FileManagerAdapter;
import com.jcx.view.myListView.SwipeMenu;
import com.jcx.view.myListView.SwipeMenuCreator;
import com.jcx.view.myListView.SwipeMenuItem;
import com.jcx.view.myListView.SwipeMenuListView;
import com.zxing.activity.CaptureActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Cui on 16-4-6.
 */
public class AllFilesActivity extends AppCompatActivity{
    private SwipeMenuListView file_list;//自定义的listview
    private TextView path_info,tv_title;//布局中的控件
    private FloatingActionButton fab_confirm,fab_cancel;
    private Context context;
    private File[] listfiles;//存放从存储卡中读出的文件列表
    private FileManagerAdapter adapter;
    private AsynLoadImg asynLoadImg;//异步加载图片

    private File parentFilePath=null,fileUsedInContextMenu;//parentFilePath:记录上一级父目录，fileUsedInContextMenu:记录下长按列表选中的文件
    private String fileType=null;
    private EditText editText;//dialog中的编辑框
    private int dialogTag=-1;//0：标志重命名的dialog；1：标志兴建文件夹的dialog
    private ProgressDialog progressDialog;

    private String rootDictionary=null;//更目录name
    private String srcFilePath=null;//移动，重命名等用到的原目录
    private String desFilePath=null;//目标目录
    private String currentFilePath=null;//当前所在的目录
    private String zipFileWillBeSend=null;//将要发送的压缩包的路径
    private CharSequence[] items;//发送选择菜单和编辑方式选择菜单的列表集合

    private FileOper fileOper;
    private BlueToothImp blueToothImp;
    private HotSpotImp hotSpotImp;
    private InetUDPImp inetUDPImp;
    private WifiDirectImp wifiDirectImp;
    private Util util;
    private GetFileSize getFileSize;

    private String flag;//mainActivity与CreatQRCodeActivity通信的标志
    private int resultTypeOfScan=-1;
    private int hotSpotSendIsConnected=-1;

    private RelativeLayout rl_waiting;

    private boolean isStop=false;
    private Dialog menuDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allfiles_main);


        blueToothImp=new BlueToothImp(this);
        hotSpotImp=new HotSpotImp(this);
        inetUDPImp=new InetUDPImp("127.0.0.1");
        wifiDirectImp=new WifiDirectImp(this);
        getFileSize=new GetFileSize();

        if (progressDialog == null) {
            progressDialog=new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        context=this;
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blueToothImp.registerBluetoothReceiver();
        wifiDirectImp.registerWifiDirectReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        blueToothImp.unregisterBluetoothReceiver();
        wifiDirectImp.unregister();
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不现实标题
        forceShowOverflowMenu();

        rl_waiting= (RelativeLayout) findViewById(R.id.allfiles_main_rl_waiting);
        tv_title= (TextView) findViewById(R.id.tv_title);
        file_list= (SwipeMenuListView) findViewById(R.id.file_list);
        file_list.setOnItemClickListener(new fileItemClickListener());
        file_list.setOnItemLongClickListener(new fileItemLongClickListener());
        path_info= (TextView) findViewById(R.id.pathinfo);
        fab_confirm= (FloatingActionButton) findViewById(R.id.fab_confirm);
        fab_cancel= (FloatingActionButton) findViewById(R.id.fab_cancel);
        fab_confirm.hide();
        fab_cancel.hide();

        //获得文件的名称然后显示到listview中
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            currentFilePath=Environment.getExternalStorageDirectory().getPath();
            int indexOf=currentFilePath.lastIndexOf("/");
            rootDictionary=currentFilePath.substring(0, indexOf);
            File fileTemp=new File(rootDictionary);
            rootDictionary=fileTemp.getName();
            System.out.println("rootDictionary:"+rootDictionary);

            listfiles=getListfiles(Environment.getExternalStorageDirectory());
            adapter=new FileManagerAdapter(context,listfiles,file_list);
            file_list.setAdapter(adapter);
            createSwipeMenu();

        }else {

            Toast.makeText(context, getString(R.string.sd_readerror), Toast.LENGTH_SHORT).show();
        }
    }
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
        return super.onMenuOpened(featureId, menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            Class<?> clazz = Class.forName("android.support.v7.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //下面传入参数
            m.invoke(menu, true);
            System.out.println("setOverFlowItemIcon successed");

        } catch (Exception e) {
            e.printStackTrace();
        }
        getMenuInflater().inflate(R.menu.all_files_menu, menu);
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
        String selectItem=null;
        switch (id) {
            case android.R.id.home:
                if (parentFilePath == null) {
                    finish();
                }else if (!parentFilePath.getName().equals("")&&!parentFilePath.getName().equals(rootDictionary)) {
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
                break;
            case R.id.action_fileAccept://TODO 通过热点接收文件


                items=new CharSequence[4];
                items[0]=getString(R.string.fileAccept_bluetooth);
                items[1]=getString(R.string.fileAccept_hotspot);
                items[2]=getString(R.string.fileAccept_network);
                items[3]=getString(R.string.fileAccept_wifidiect);

                final AlertDialog.Builder builder = new AlertDialog.Builder(AllFilesActivity.this);
                builder.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_bluetooth))) {
                            //TODO -------->通过蓝牙接受文件

                        } else if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点接收文件
//                            Bitmap qrCodeBitmap = hotSpotImp.getQRCode();
//                            Intent intent=new Intent(AllFilesActivity.this,ShowQRCodeActivity.class);
//                            intent.putExtra(Config.KEY, Config.VALUE_HOTSPOT);
//                            startActivityForResult(intent,1);
                            View  view=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_view,null);
                            AlertDialog.Builder builder =new AlertDialog.Builder(AllFilesActivity.this);
                            ImageView iv_qrcode= (ImageView) view.findViewById(R.id.dialog_QRCode_image);
                            iv_qrcode.setImageBitmap(hotSpotImp.getQRCode());
                            builder.setView(view);
                            builder.create();
                            final AlertDialog qrcodeDialog=builder.show();


                            hotSpot_connection(new SuccessCallBack() {
                                @Override
                                public void onSuccess() {
                                    if (qrcodeDialog.isShowing()) {
                                        qrcodeDialog.dismiss();
                                    }
                                    progressDialog = new ProgressDialog(context);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                                    progressDialog.setCancelable(false);//不允许退出
                                    progressDialog.setMessage(hotSpotImp.getFileName());
                                    progressDialog.setMax((int) (hotSpotImp.getlength() / Util.BLOCK_SIZE));
                                    progressDialog.show();

                                    updateProgressBarWhenAcceptByHotSpot();//显示progressBar的时候更新progressbar的进度条
                                    acceptFileByHotSpot(new SuccessCallBack() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(AllFilesActivity.this, "接收文件成功", Toast.LENGTH_SHORT).show();
                                            hotSpotImp.disconnect();
                                            progressDialog.hide();
                                            progressDialog = null;
                                        }
                                    }, new FailCallBack() {
                                        @Override
                                        public void onFail() {
                                            Toast.makeText(AllFilesActivity.this, "接收文件失败", Toast.LENGTH_SHORT).show();
                                            hotSpotImp.disconnect();
                                            progressDialog.hide();
                                            progressDialog = null;
                                        }
                                    });
                                }
                            }, new FailCallBack() {
                                @Override
                                public void onFail() {

                                    Toast.makeText(AllFilesActivity.this, "未能成功连接", Toast.LENGTH_SHORT).show();
                                    if (qrcodeDialog.isShowing()) {
                                        qrcodeDialog.dismiss();
                                    }
                                    hotSpotImp.disconnect();
                                    System.out.println("未能连接成功");
                                }
                            });


                        }else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络接收文件
                        }else if(select_item.equals(getString(R.string.fileAccept_wifidiect))){
                            //TODO --------->WIFIDirect 接收文件
                        }
                    }
                });
                menuDialog=builder.show();
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
                }else{}
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 通过热点接收文件时更新progressbar
     */
    private void updateProgressBarWhenAcceptByHotSpot(){
        final Handler handlerForUpdateProgressbar_hotSpotImgRec = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 0) {
                    progressDialog.setProgress(msg.what);
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                int progress=0;
                do {
                    progress = (int) (Util.getRcvIndex() * 2);
                    Message message = Message.obtain();
                    message.what = progress;
                    handlerForUpdateProgressbar_hotSpotImgRec.sendMessage(message);
                }while (progress<hotSpotImp.getlength());
            }
        }.start();
    }

    /**
     * 监听是否已经开始接收文件
     * @param successCallBack
     * @param failCallBack
     */
    private void hotSpot_connection(final SuccessCallBack successCallBack, final FailCallBack failCallBack){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch ((int)msg.what){
                    case 0:
                        if (failCallBack != null) {
                            failCallBack.onFail();
                        }
                        break;
                    case 1:
                        if (successCallBack != null) {
                            successCallBack.onSuccess();
                        }
                        break;
                }
            }
        };
        new Thread(){
            @Override
            public void run() {
                super.run();
                if (hotSpotImp.connenct()==TransBasic.CONNECT_OK) {
                    Message message=Message.obtain();
                    message.what=1;
                    handler.sendMessage(message);
                }else {
                    Message message=Message.obtain();
                    message.what=0;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    /**
     * 通过热点接收文件
     * @param successCallBack 接收文件成功的接口
     * @param failCallBack 接收文件失败的接口
     */
    private void acceptFileByHotSpot(final SuccessCallBack successCallBack, final FailCallBack failCallBack){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch ((int)msg.what){
                    case 0:
                        if (failCallBack != null) {
                            failCallBack.onFail();
                        }
                        break;
                    case 1:
                        if (successCallBack != null) {
                            successCallBack.onSuccess();
                        }
                        break;
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (hotSpotImp.receiFile() == TransBasic.RECI_OK) {
                    Message message=Message.obtain();
                    message.what=1;
                    handler.sendMessage(message);
                }
                else {
                    Message message=Message.obtain();
                    message.what=0;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    public static interface SuccessCallBack{
        void onSuccess();
    }
    public static interface FailCallBack{
        void onFail();
    }

    /**
     * 发送文件的发送方式选择菜单
     */
    private void menu_sendModes(){
        items=new CharSequence[4];
        items[0]=getString(R.string.fileSend_bluetooth);
        items[1]=getString(R.string.fileSend_hotspot);
        items[2]=getString(R.string.fileSend_network);
        items[3]=getString(R.string.fileSend_WIFIDirect);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                AllFilesActivity.this);
        builder.setTitle(R.string.sendFile_title).setIcon(R.drawable.file_send_menu_icon);
        //items使用全局的finalCharSequenece数组声明
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String select_item = items[which].toString();
                if (select_item.equals(getString(R.string.fileSend_bluetooth))) {
                    //TODO -------->通过蓝牙发送文件
                    Intent intent=new Intent(AllFilesActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, 0);
                    resultTypeOfScan=0;

                }else if (select_item.equals(getString(R.string.fileSend_hotspot))) {
                    //TODO---------->通过开热点发送文件
                    Intent intent1=new Intent(AllFilesActivity.this, CaptureActivity.class);
                    startActivityForResult(intent1, 0);
                    resultTypeOfScan=1;

                }else if (select_item.equals(getString(R.string.fileSend_network))) {
                    //TODO--------->通过网络发送文件
                    Intent intent2=new Intent(AllFilesActivity.this, CaptureActivity.class);
                    startActivityForResult(intent2, 0);
                    resultTypeOfScan=2;
                }else if (select_item.equals(getString(R.string.fileSend_WIFIDirect))){
                    //TODO---------->WIFIDirect 发送文件
                    Intent intent3=new Intent(AllFilesActivity.this, CaptureActivity.class);
                    startActivityForResult(intent3, 0);
                    resultTypeOfScan=3;

                }
            }
        });
        builder.show();
    }

    /**
     * 编辑文件的选择菜单
     */
    private void menu_editModes(){
        items=new CharSequence[4];
        items[0]=getString(R.string.copy);
        items[1]=getString(R.string.move);
        items[2]=getString(R.string.delete);
        items[3]=getString(R.string.rename);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(
                AllFilesActivity.this);
        builder2.setTitle(R.string.editFile_title).setIcon(R.drawable.file_settings_menu_icon);
        //items使用全局的finalCharSequenece数组声明
        builder2.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String select_item = items[which].toString();
                if (select_item.equals(getString(R.string.copy))) {
                    srcFilePath=fileUsedInContextMenu.getAbsolutePath();
                    System.out.println("srcFilePath:"+srcFilePath);
                    if (srcFilePath!=null) {
                        Toast.makeText(context, R.string.fileCopy_success, Toast.LENGTH_SHORT).show();
                    }
                }else if (select_item.equals(getString(R.string.move))) {
                    tv_title.setText(R.string.tv_text_choseTargetDictionary);
                    srcFilePath=fileUsedInContextMenu.getAbsolutePath();
                    fab_confirm.show();
                    fab_cancel.show();
                    floatingActionButtonClickListener();
                }else if (select_item.equals(getString(R.string.delete))) {
                    boolean fileDeleteReturn;
                    fileDeleteReturn=fileOper.delete(fileUsedInContextMenu.getAbsolutePath());
                    viewUpdate();
                    asynLoadImg.isAllow=true;
                    if (fileDeleteReturn) {
                        Toast.makeText(context, R.string.fileDelete_success, Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, R.string.fileDelete_fail, Toast.LENGTH_SHORT).show();
                    }
                }else if (select_item.equals(getString(R.string.rename))){
                    AlertDialog.Builder dialog2=new AlertDialog.Builder(context);
                    dialog2.setTitle(R.string.rename);
                    editText=new EditText(context);
                    editText.setHint(R.string.input_newname);
                    editText.setText(fileUsedInContextMenu.getName());
                    editText.selectAll();
                    dialog2.setView(editText);
                    dialogTag=0;
                    dialog2.setPositiveButton(R.string.rename_sure, new dialogOnConfirmClickListener());
                    dialog2.setNegativeButton(R.string.rename_undo, new dialogOnCancelClickListener());
                    dialog2.show();
                }
            }
        });
        builder2.show();
    }
    /**
     * ContextMenu功能菜单的点击事件的处理
     * @param item 获得被点击的菜单
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position=menuInfo.position;
        fileUsedInContextMenu=listfiles[position];
        switch (item.getItemId())
        {
            case R.id.menu_file_send:
                menu_sendModes();
                break;
            case R.id.menu_edit:
                menu_editModes();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 通过热点发送文件
     * @param result
     * @param zipFilePath_WillBeSend
     * @param zip_file
     * @param successCallBack
     * @param failCallBack
     */
    private void connection_sendByHotSpot(final String result,final String zipFilePath_WillBeSend,final File zip_file, final SuccessCallBack successCallBack, final FailCallBack failCallBack){
        final Handler handlerForShowProgressbar = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == TransBasic.CONNECT_OK) {
                    if (successCallBack!=null) {
                        successCallBack.onSuccess();
                    }

                }else {
                    if (failCallBack != null) {
                        failCallBack.onFail();
                    }

                }
            }
        };
        //开启线程用于连接
        new Thread() {
            @Override
            public void run() {
                super.run();
                int connectResult = hotSpotImp.connect(result);
                Message message = Message.obtain();
                message.what = connectResult;
                handlerForShowProgressbar.sendMessage(message);


            }
        }.start();
    }

    /**
     * 通过热点发送文件
     * @param zip_file 要发送的文件
     */
    private void sendFileByHotSpot(final File zip_file, final SuccessCallBack successCallBack, final FailCallBack failCallBack){
        final Handler handlerForGetTransFilesResult = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if ((int) (msg.obj) == TransBasic.TRANS_OK) {
                    if (successCallBack != null) {
                        successCallBack.onSuccess();
                    }
                }else {
                    if (failCallBack != null) {
                        failCallBack.onFail();
                    }
                }
            }
        };
        //开启线程传送文件
        new Thread() {
            @Override
            public void run() {
                super.run();
                int transResult = hotSpotImp.transFile(zip_file);
                Message message = Message.obtain();
                message.obj = transResult;
                handlerForGetTransFilesResult.sendMessage(message);
            }
        }.start();

    }

    /**
     * 发送文件的时候更新进度条
     */
    private void updateProgressBarWhenSendByHotSpot(){
        final Handler handlerFroUpdateProgressBar = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(progressDialog!=null)progressDialog.setProgress(msg.what);
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                int progress;
                do {
                    progress = (int) (Util.getSendIndex() * 2);
                    Message message = Message.obtain();
                    message.what = progress;
                    handlerFroUpdateProgressBar.sendMessage(message);
                } while (progress<hotSpotImp.getlength());
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            final String result = data.getExtras().getString("result");
            Toast.makeText(AllFilesActivity.this, result, Toast.LENGTH_SHORT).show();
            switch (resultTypeOfScan) {
                case 0:
                    resultTypeOfScan = -1;
                    break;
                case 1: //TODO 通过热点发送文件
//                    srcFilePath = fileUsedInContextMenu.getAbsolutePath();
                    //压缩文件
//                    ZipUtil zipUtil = new ZipUtil();
//                    String zipedFilePath = srcFilePath.substring(0, srcFilePath.lastIndexOf(".")) + ".zip";
//                    final String zipFilePath_WillBeSend = zipUtil.getZipedFile(srcFilePath, zipedFilePath);
//                    final File zip_file = new File(zipFilePath_WillBeSend);
                    final String zipFilePath_WillBeSend = fileUsedInContextMenu.getAbsolutePath();
                    final File zip_file=new File(zipFilePath_WillBeSend);
                    rl_waiting.setVisibility(View.VISIBLE);

                    connection_sendByHotSpot(result, zipFilePath_WillBeSend, zip_file, new SuccessCallBack() {
                        @Override
                        public void onSuccess() {
                            rl_waiting.setVisibility(View.GONE);
                            if (zipFilePath_WillBeSend != null) {
                                progressDialog = new ProgressDialog(AllFilesActivity.this);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                                progressDialog.setCancelable(false);//不允许退出
                                progressDialog.setMessage(zip_file.getName());
                                progressDialog.setMax((int) (zip_file.length() / Util.BLOCK_SIZE));
                                progressDialog.show();

                                sendFileByHotSpot(zip_file, new SuccessCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(AllFilesActivity.this, "热点传输文件成功", Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        progressDialog = null;
                                        hotSpotImp.disconnect();
                                    }
                                }, new FailCallBack() {
                                    @Override
                                    public void onFail() {
                                        Toast.makeText(AllFilesActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
                                        System.out.println("发送失败");
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        progressDialog = null;
                                        hotSpotImp.disconnect();
                                    }
                                });
                                updateProgressBarWhenSendByHotSpot();
                            }
                        }
                    }, new FailCallBack() {
                        @Override
                        public void onFail() {
                            rl_waiting.setVisibility(View.GONE);
                            Toast.makeText(AllFilesActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                            System.out.println("连接失败");
                        }
                    });



                    resultTypeOfScan = -1;
                    break;
                case 2://TODO 通过网络传输文件
                    resultTypeOfScan = -1;
                    break;
                case 3:// TODO 通过WIFIDIRECT 发送文件
                    resultTypeOfScan = -1;
                    break;
            }
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
                System.out.println("srcFilePath:" + srcFilePath + "\n" + "currentFilePath:" + currentFilePath);
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
     * dialog输入框“确定”按钮的点击事件:新建文件夹，重命名
     */
    public class dialogOnConfirmClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String name = editText.getText().toString();
            if (editText != null && !"".equals(name)) {
                switch (dialogTag)
                {
                    case 0:
                        String path=fileUsedInContextMenu.getPath();
                        int indexof=path.lastIndexOf("/");
                        path=path.substring(0,indexof);
                        System.out.println("reName:"+path+"name:"+name);
                        fileOper.modify(path,fileUsedInContextMenu.getName(),name);
                        viewUpdate();
                        dialogTag=-1;
                        break;
                    case 1:
                        fileOper.touch(currentFilePath,name,true);
                        dialogTag=-1;
                        viewUpdate();
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
     * 设置先做滑动，发送的按钮
     */
    public void createSwipeMenu(){
        SwipeMenuCreator swipeMenuCreator=new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem sendItem=new SwipeMenuItem(getApplicationContext());
                sendItem.setBackground(R.color.colorTitleBackground);
                sendItem.setWidth(dp2px(90));
                sendItem.setTitle("发送");
                sendItem.setTitleSize(18);
                sendItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(sendItem);
            }
        };
        file_list.setMenuCreator(swipeMenuCreator);
        file_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {//TODO 点击发送按钮选择发送方式
                    case 0:
                        System.out.println(position);
                        fileUsedInContextMenu=listfiles[position];
                        menu_sendModes();
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
