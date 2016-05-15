package com.jcx.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jcx.R;
import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.view.task.HotSpotRcvTask;
import com.jcx.view.task.UdpRcvTask;

import java.util.jar.Manifest;

/**
 * Created by Cui on 16-4-26.
 */
public class MainActivity extends AppCompatActivity {
    private HotSpotImp hotSpotImp;
    private InetUDPImp inetUDPImp;

    private RelativeLayout rl_allFiles;
    private LinearLayout ll_main_waiting;
    private ImageButton ib_allApks,ib_allDocuments,ib_allPictures,ib_allVideo,ib_allMusic,ib_allZipFiles,ib_others,ib_allOffineWebPages;
    private String FLAG="flag";
    private String flag=null;//mainActivity与CreatQRCodeActivity通信的标志
    private CharSequence[] items;//发送选择菜单和编辑方式选择菜单的列表集合
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setTitle(R.string.app_name);

        hotSpotImp=new HotSpotImp(this);

        initView();
    }

    private void initView() {
        ll_main_waiting= (LinearLayout) findViewById(R.id.ll_main_progressbar_waiting);
        rl_allFiles= (RelativeLayout) findViewById(R.id.rl_allFiles);
        rl_allFiles.setOnClickListener(new onClickListener());
        ib_allApks= (ImageButton) findViewById(R.id.iv_app);
        ib_allApks.setOnClickListener(new onClickListener());
        ib_allDocuments= (ImageButton) findViewById(R.id.iv_document);
        ib_allDocuments.setOnClickListener(new onClickListener());
        ib_allPictures= (ImageButton) findViewById(R.id.iv_picture);
        ib_allPictures.setOnClickListener(new onClickListener());
        ib_allVideo= (ImageButton) findViewById(R.id.iv_video);
        ib_allVideo.setOnClickListener(new onClickListener());
        ib_allMusic= (ImageButton) findViewById(R.id.iv_music);
        ib_allMusic.setOnClickListener(new onClickListener());
        ib_allZipFiles= (ImageButton) findViewById(R.id.iv_zip);
        ib_allZipFiles.setOnClickListener(new onClickListener());
        ib_others= (ImageButton) findViewById(R.id.iv_others);
        ib_others.setOnClickListener(new onClickListener());
        ib_allOffineWebPages= (ImageButton) findViewById(R.id.iv_offine_webPage);
        ib_allOffineWebPages.setOnClickListener(new onClickListener());
    }

    private class onClickListener implements OnClickListener{

        int index=0;
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_allFiles:
                    Intent intent1=new Intent(MainActivity.this,AllFilesActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.iv_app:
                    index=1;
                    break;
                case R.id.iv_document:
                    index=2;
                    break;
                case R.id.iv_picture:
                    index=3;
                    break;
                case R.id.iv_video:
                    index=4;
                    break;
                case R.id.iv_music:
                    index=5;
                    break;
                case R.id.iv_zip:
                    index=6;
                    break;
                case R.id.iv_offine_webPage:
                    index=7;
                    break;
                case R.id.iv_others:
                    index=8;
                    break;
            }
            if (index!=0&&index<=8) {
                Intent intent=new Intent(MainActivity.this,ClassifiedFileActivity.class);
                intent.putExtra(FLAG,index);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classificed_files_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionbar_fileAccept_pc_ph://TODO 从电脑接收文件
                items=new CharSequence[2];
                items[0]=getString(R.string.fileAccept_hotspot);
                items[1]=getString(R.string.fileAccept_network);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        MainActivity.this);
                builder1.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder1.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过热点从电脑接收文件
                            View  view=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_view,null);
                            AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                            ImageView iv_qrcode= (ImageView) view.findViewById(R.id.dialog_QRCode_image);
                            iv_qrcode.setImageBitmap(hotSpotImp.getQRCode(500,500));
                            builder.setView(view);
                            builder.create();
                            final AlertDialog qrcodeDialog=builder.show();

                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                            }
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(true);//不允许退出

                            new HotSpotRcvTask(progressDialog,MainActivity.this).execute();

                        } else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络从电脑接收文件
                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                            }
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(true);//不允许退出
                            new UdpRcvTask(progressDialog,inetUDPImp.getLoaclAddr()).execute();
                        }
                    }
                });
                builder1.show();
                break;
            case R.id.actionbar_fileAccept://TODO 从手机接收文件
                items=new CharSequence[2];
                items[0]=getString(R.string.fileAccept_hotspot);
                items[1]=getString(R.string.fileAccept_network);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle(R.string.acceptFile_title).setIcon(R.drawable.file_accept_menu_icon);
                //items使用全局的finalCharSequenece数组声明
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select_item = items[which].toString();
                        if (select_item.equals(getString(R.string.fileAccept_hotspot))) {
                            //TODO---------->通过热点从手机接收文件
                            View  view=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_view,null);
                            AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                            ImageView iv_qrcode= (ImageView) view.findViewById(R.id.dialog_QRCode_image);
                            iv_qrcode.setImageBitmap(hotSpotImp.getQRCode(500,500));
                            builder.setView(view);
                            builder.create();
                            final AlertDialog qrcodeDialog=builder.show();

                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                            }
                            progressDialog.setTitle(getString(R.string.accepting_dialog_title));
                            progressDialog.setCancelable(true);//不允许退出

                            new HotSpotRcvTask(progressDialog,MainActivity.this).execute();

                        } else if (select_item.equals(getString(R.string.fileAccept_network))) {
                            //TODO--------->通过网络从手机接收文件
                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
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
}
