package com.jcx.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcx.R;
import com.jcx.communication.BlueToothImp;
import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.communication.WifiDirectImp;
import com.jcx.util.FileClassification;
import com.jcx.util.Util;
import com.jcx.util.ZipUtil;
import com.jcx.view.adapter.ClassifiedFileAdapter;
import com.jcx.view.myListView.SwipeMenu;
import com.jcx.view.myListView.SwipeMenuCreator;
import com.jcx.view.myListView.SwipeMenuItem;
import com.jcx.view.myListView.SwipeMenuListView;
import com.zxing.activity.CaptureActivity;

import java.io.File;
import java.util.List;

/**
 * Created by Cui on 16-4-28.
 */
public class ClassifiedFileActivity extends AppCompatActivity {
    private List<FileClassification.MyFile> filesList;

    private ClassifiedFileAdapter adapter;
    private BlueToothImp blueToothImp;
    private HotSpotImp hotSpotImp;
    private InetUDPImp inetUDPImp;
    private WifiDirectImp wifiDirectImp;

    private SwipeMenuListView myListView;
    private LinearLayout ll_waiting;
    private TextView tv_apk_package_not_found;
    private TextView tv_title;
    private LinearLayout ll_classifiedFile_waiting;

    private int FLAG=-1;
    private int resultTypeOfScan=-1;
    private String flag=null;//mainActivity与CreatQRCodeActivity通信的标志
    private CharSequence[] items;//发送选择菜单和编辑方式选择菜单的列表集合
    private String filePath_WillBeSend;
    private String srcFilePath=null;
    private String zipFilePath_WillBeSend;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classificed_file_main);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        FLAG=bundle.getInt("flag");

        blueToothImp=new BlueToothImp(this);
        hotSpotImp=new HotSpotImp(this);
        inetUDPImp=new InetUDPImp("127.0.0.1");
        wifiDirectImp=new WifiDirectImp(this);

        initCustomActionBar();
        initView();
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
     * 初始化ActionBar布局
     * @return
     */
    private boolean initCustomActionBar(){
        android.support.v7.app.ActionBar mActionBar=getSupportActionBar();
        if (mActionBar == null) {
            return false;
        }

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER | Gravity.CENTER_HORIZONTAL);

        View myView= LayoutInflater.from(this).inflate(R.layout.custom_action_bar, null);
        mActionBar.setCustomView(myView, lp);
        mActionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        tv_title= (TextView) mActionBar.getCustomView().findViewById(R.id.title);
        setTv_title();
        return true;
    }

    /**
     * 初始化界面
     */
    private void initView() {
        ll_waiting= (LinearLayout) findViewById(R.id.ll_progressbar_waiting);
        ll_waiting.setVisibility(View.VISIBLE);
        ll_classifiedFile_waiting= (LinearLayout) findViewById(R.id.ll_classificed_main_waiting);
        myListView= (SwipeMenuListView) findViewById(R.id.lv_allapks_list);
        asyncLoadList();
        createSwipeMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classificed_files_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.actionbar_fileAccept:
                items=new CharSequence[4];
                items[0]=getString(R.string.fileAccept_bluetooth);
                items[1]=getString(R.string.fileAccept_hotspot);
                items[2]=getString(R.string.fileAccept_network);
                items[3]=getString(R.string.fileAccept_wifidiect);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ClassifiedFileActivity.this);
                builder.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_bluetooth))) {
                            //TODO -------->通过蓝牙接受文件
                            flag = "BTR";
                            Intent intent2 = new Intent(ClassifiedFileActivity.this, CreateQRCodeActivity.class);
                            intent2.putExtra("flag", flag);
                            flag = null;
                            startActivityForResult(intent2, 1);

                        } else if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点接收文件
                            flag = "HSR";
                            Intent intent3 = new Intent(ClassifiedFileActivity.this, CreateQRCodeActivity.class);
                            intent3.putExtra("flag", flag);
                            flag = null;
                            startActivityForResult(intent3, 2);

                        } else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络接收文件
                            flag = "UDP";
                            Intent intent4 = new Intent(ClassifiedFileActivity.this, CreateQRCodeActivity.class);
                            intent4.putExtra("flag", flag);
                            startActivityForResult(intent4, 3);
                        } else if (select_item.equals(getString(R.string.fileAccept_wifidiect))) {
                            //TODO --------->WIFIDirect 接收文件
                            flag = "WFD";
                            Intent intent5 = new Intent(ClassifiedFileActivity.this, CreateQRCodeActivity.class);
                            intent5.putExtra("flag", flag);
                            startActivityForResult(intent5, 4);
                        }
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 异步获取文件列表
     */
    private void asyncLoadList(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getFilesList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (filesList.size()==0) {
                    ll_waiting.setVisibility(View.GONE);
                    tv_apk_package_not_found= (TextView) findViewById(R.id.tv_file_not_found);
                    tv_apk_package_not_found.setVisibility(View.VISIBLE);
                }else {
                    adapter = new ClassifiedFileAdapter(ClassifiedFileActivity.this, filesList);
                    myListView.setAdapter(adapter);
                    ll_waiting.setVisibility(View.GONE);
                }
            }
        }.execute();
    }
    private void getFilesList(){
        switch (FLAG)
        {
            case 1:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getApkFileList();
                break;
            case 2:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getDocumentFileList();
                break;
            case 3:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getPicturesFileList();
                break;
            case 4:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getVidosFileList();
                break;
            case 5:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getMusicFileList();
                break;
            case 6:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getZipFileList();
                break;
            case 7:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getOffLineWebPageFileList();
                break;
            case 8:
                filesList=FileClassification.getFileClassification(ClassifiedFileActivity.this).getOthersFileList();
                break;
            default:
                break;
        }
    }
    private void setTv_title(){
        switch (FLAG)
        {
            case 1:
                tv_title.setText(R.string.title_apk);
                break;
            case 2:
                tv_title.setText(R.string.title_documents);
                break;
            case 3:
                tv_title.setText(R.string.tv_picture);
                break;
            case 4:
                tv_title.setText(R.string.title_video);
                break;
            case 5:
                tv_title.setText(R.string.title_music);
                break;
            case 6:
                tv_title.setText(R.string.title_zip);
                break;
            case 7:
                tv_title.setText(R.string.title_offLineWebPages);
                break;
            case 8:
                tv_title.setText(R.string.title_others);
                break;
            default:
                break;
        }
    }

    /**
     * 创建向左滑动的弹出发送按钮
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
        myListView.setMenuCreator(swipeMenuCreator);
        myListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {//TODO 点击发送按钮选择发送方式
                    case 0:
                        System.out.println(position);
                        filePath_WillBeSend =filesList.get(position).getFile_path();
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
                ClassifiedFileActivity.this);
        builder.setTitle(R.string.sendFile_title).setIcon(R.drawable.file_send_menu_icon);
        //items使用全局的finalCharSequenece数组声明
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String select_item = items[which].toString();
                if (select_item.equals(getString(R.string.fileSend_bluetooth))) {
                    //TODO -------->通过蓝牙发送文件
                    Intent intent=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, 0);
                    resultTypeOfScan=0;

                }else if (select_item.equals(getString(R.string.fileSend_hotspot))) {
                    //TODO---------->通过开热点发送文件
                    Intent intent1=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent1, 0);
                    resultTypeOfScan=1;

                }else if (select_item.equals(getString(R.string.fileSend_network))) {
                    //TODO--------->通过网络发送文件
                    Intent intent2=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent2, 0);
                    resultTypeOfScan=2;
                }else if (select_item.equals(getString(R.string.fileSend_WIFIDirect))){
                    //TODO---------->WIFIDirect 发送文件
                    Intent intent3=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent3, 0);
                    resultTypeOfScan=3;

                }
            }
        });
        builder.show();
    }

    private Handler handlerForGetTransFilesResult =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ((int)(msg.obj)==TransBasic.TRANS_OK) {
                Toast.makeText(ClassifiedFileActivity.this,"热点传输文件成功",Toast.LENGTH_SHORT).show();
            }
            progressDialog.hide();
            progressDialog=null;
            hotSpotImp.disconnect();
        }
    };
    private Handler handlerFroUpdateProgressBar=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.setProgress(msg.what);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK)
        {
            final String result=data.getExtras().getString("result");
            Toast.makeText(ClassifiedFileActivity.this, result, Toast.LENGTH_SHORT).show();
            switch (resultTypeOfScan)
            {
                case 0:
                    new AsyncTask<Void, Void, Void>() {//TODO 使用蓝牙发送文件
                        @Override
                        protected Void doInBackground(Void... params) {
                            if(blueToothImp.connect(result)== TransBasic.CONNECT_OK) {
                                if (filePath_WillBeSend != null) {
                                    File file=new File(filePath_WillBeSend);
                                    if(blueToothImp.transFile(file)==TransBasic.TRANS_OK){
                                        Toast.makeText(ClassifiedFileActivity.this,"蓝牙传输文件成功",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            return null;

                        }
                    }.execute();
                    resultTypeOfScan=-1;
                    break;
                case 1: //TODO 通过热点发送文件
                    final int[] connectResult = new int[1];
                    ll_classifiedFile_waiting.setVisibility(View.VISIBLE);
                    //开启线程用于连接
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            connectResult[0] =hotSpotImp.connect(result);
                        }
                    }.start();
                    ll_classifiedFile_waiting.setVisibility(View.GONE);
                    if (connectResult[0] == TransBasic.CONNECT_OK) {
                        //获得要发送文件的路径
                        srcFilePath=filePath_WillBeSend;
                        //压缩文件
                        ZipUtil zipUtil=new ZipUtil();
                        String zipedFilePath=srcFilePath.substring(0,srcFilePath.lastIndexOf("."))+".zip";
                        zipFilePath_WillBeSend=zipUtil.getZipedFile(srcFilePath,zipedFilePath);
                        final File zip_file=new File(zipFilePath_WillBeSend);

                        if (zipFilePath_WillBeSend != null) {
                            progressDialog=new ProgressDialog(this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(false);//不允许退出
                            progressDialog.setMessage(zip_file.getName());
                            progressDialog.setMax((int) (zip_file.length()/1024));
                            progressDialog.show();
                        }
                        //开启线程监听接收文件的大小更新progressbar
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                do {
                                    int progress= (int) (Util.getRcvIndex()*1016*2);
                                    Message message=Message.obtain();
                                    message.what=progress;
                                    handlerFroUpdateProgressBar.sendMessage(message);
                                }while (hotSpotImp.getlength()<= Util.getRcvIndex()*1016*2);
                            }
                        }.start();

                        //开启线程传送文件
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                int transResult=hotSpotImp.transFile(zip_file);
                                Message message=Message.obtain();
                                message.obj=transResult;
                                handlerForGetTransFilesResult.sendMessage(message);
                            }
                        }.start();
                    }
                    resultTypeOfScan=-1;
                    break;
                case 2:
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) { //TODO 通过网络传输文件
                            if (inetUDPImp.connect(result) == TransBasic.CONNECT_OK) {
                                if (filePath_WillBeSend != null) {
                                    File file=new File(filePath_WillBeSend);
                                    if (inetUDPImp.transFile(file) == TransBasic.TRANS_OK)
                                    {
                                        Toast.makeText(ClassifiedFileActivity.this,"网络传输文件成功",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            return null;
                        }
                    }.execute();
                    resultTypeOfScan=-1;
                    break;
                case 3:
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) { // TODO 通过WIFIDIRECT 发送文件
                            if (wifiDirectImp.connect(result)==TransBasic.CONNECT_OK) {
                                if (filePath_WillBeSend != null) {
                                    File file=new File(filePath_WillBeSend);
                                    if(wifiDirectImp.transFile(file)==TransBasic.TRANS_OK){
                                        Toast.makeText(ClassifiedFileActivity.this,"WIFIDIRECT传输文件成功",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            return null;
                        }
                    }.execute();
                    resultTypeOfScan=-1;
                    break;
            }
        }else if (resultCode == 1 && data.getStringExtra("action").equals("BTR")) {//TODO 通过蓝牙接收文件
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if(blueToothImp.receiFile()==TransBasic.RECI_OK){
                        Toast.makeText(ClassifiedFileActivity.this,"蓝牙发送文件成功",Toast.LENGTH_SHORT).show();
                    }
                    return null;
                }
            }.execute();
        }else if (resultCode == 2 && data.getStringExtra("action").equals("HSR")) {//TODO 通过热点接收文件
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if(hotSpotImp.receiFile()==TransBasic.RECI_OK){
                        hotSpotImp.disconnect();
                        Toast.makeText(ClassifiedFileActivity.this,"热点接收文件成功",Toast.LENGTH_SHORT).show();
                    }
                    return null;
                }
            }.execute();
        }else if (resultCode == 3 && data.getStringExtra("action").equals("UDP")) {//TODO 通过UDP接收文件
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if(inetUDPImp.connect() == TransBasic.CONNECT_OK) {
                        if (inetUDPImp.receiFile() == TransBasic.RECI_OK) {
                            Toast.makeText(ClassifiedFileActivity.this,"网络发送文件成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                    return null;
                }
            }.execute();
        }else if (resultCode == 4 && data.getStringExtra("action").equals("WFD")) {//TODO 通过WIFIDriect接收文件
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (wifiDirectImp.receiFile() == TransBasic.RECI_OK) {//
                        Toast.makeText(ClassifiedFileActivity.this,"WIFIDIRECT发送文件成功",Toast.LENGTH_SHORT).show();
                    }
                    return null;
                }
            }.execute();
        }
    }
}
