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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcx.R;
import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.FileClassification;
import com.jcx.util.NetworkDetect;
import com.jcx.util.Util;
import com.jcx.util.ZipUtil;
import com.jcx.view.adapter.ClassifiedFileAdapter;
import com.jcx.view.myListView.SwipeMenu;
import com.jcx.view.myListView.SwipeMenuCreator;
import com.jcx.view.myListView.SwipeMenuItem;
import com.jcx.view.myListView.SwipeMenuListView;
import com.jcx.view.task.HotSpotRcvTask;
import com.jcx.view.task.UdpRcvTask;
import com.zxing.activity.CaptureActivity;

import java.io.File;
import java.util.List;

/**
 * Created by Cui on 16-4-28.
 */
public class ClassifiedFileActivity extends AppCompatActivity {
    private List<FileClassification.MyFile> filesList;

    private ClassifiedFileAdapter adapter;
    private HotSpotImp hotSpotImp;
    private InetUDPImp inetUDPImp;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classificed_file_main);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        FLAG=bundle.getInt("flag");
        String netType = NetworkDetect.getCurrentNetType(this);
        String localAddr = netType=="2g"||netType=="3g"||netType=="4g"?NetworkDetect.getLocalIpAddress():NetworkDetect.getNetIp();
        hotSpotImp=new HotSpotImp(this);
        inetUDPImp=new InetUDPImp(localAddr);

        initCustomActionBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            case R.id.actionbar_fileAccept_pc_ph://TODO 从电脑接收文件
                items=new CharSequence[2];
                items[0]=getString(R.string.fileAccept_hotspot);
                items[1]=getString(R.string.fileAccept_network);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        ClassifiedFileActivity.this);
                builder1.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder1.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点接收文件

                        } else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络接收文件
                        }
                    }
                });
                builder1.show();
                break;
            case R.id.actionbar_fileAccept://TODO 从手机端接收文件
                items=new CharSequence[2];
                items[0]=getString(R.string.fileAccept_hotspot);
                items[1]=getString(R.string.fileAccept_network);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ClassifiedFileActivity.this);
                builder.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过开热点接收文件
                            View  view=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_view,null);
                            AlertDialog.Builder builder =new AlertDialog.Builder(ClassifiedFileActivity.this);
                            ImageView iv_qrcode= (ImageView) view.findViewById(R.id.dialog_QRCode_image);
                            iv_qrcode.setImageBitmap(hotSpotImp.getQRCode(500,500));
                            builder.setView(view);
                            builder.create();
                            final AlertDialog qrcodeDialog=builder.show();

                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(ClassifiedFileActivity.this);
                            }
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(true);//不允许退出

                            new HotSpotRcvTask(progressDialog,ClassifiedFileActivity.this).execute();
                        } else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络接收文件
                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(ClassifiedFileActivity.this);
                            }
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(true);//不允许退出
                            new UdpRcvTask(progressDialog,inetUDPImp.getLoaclAddr()).execute();
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
        items=new CharSequence[2];
        items[0]=getString(R.string.fileSend_hotspot);
        items[1]=getString(R.string.fileSend_network);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                ClassifiedFileActivity.this);
        builder.setTitle(R.string.sendFile_title).setIcon(R.drawable.file_send_menu_icon);
        //items使用全局的finalCharSequenece数组声明
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String select_item = items[which].toString();
                if (select_item.equals(getString(R.string.fileSend_hotspot))) {
                    //TODO---------->通过开热点发送文件
                    Intent intent1=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent1, 0);
                    resultTypeOfScan=1;

                }else if (select_item.equals(getString(R.string.fileSend_network))) {
                    //TODO--------->通过网络发送文件
                    Intent intent2=new Intent(ClassifiedFileActivity.this, CaptureActivity.class);
                    startActivityForResult(intent2, 0);
                    resultTypeOfScan=2;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            final String result = data.getExtras().getString("result");
            Toast.makeText(ClassifiedFileActivity.this, result, Toast.LENGTH_SHORT).show();
            switch (resultTypeOfScan) {
                case 0:
                    resultTypeOfScan = -1;
                    break;
                case 1: //TODO 通过热点发送文件
                    resultTypeOfScan = -1;
                    break;
                case 2:
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) { //TODO 通过网络传输文件
                            if (inetUDPImp.connect(result) == TransBasic.CONNECT_OK) {
                                if (filePath_WillBeSend != null) {
                                    File file = new File(filePath_WillBeSend);
                                    if (inetUDPImp.transFile(file) == TransBasic.TRANS_OK) {
                                        Toast.makeText(ClassifiedFileActivity.this, "网络传输文件成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            return null;
                        }
                    }.execute();
                    resultTypeOfScan = -1;
                    break;
                case 3:
                    resultTypeOfScan = -1;
                    break;
            }
        }
    }
}
