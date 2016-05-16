package com.jcx.view.task;

import android.app.ProgressDialog;

import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

import java.io.File;

/**
 * Created by lenovo on 2016/5/11.
 */
public class UdpSendTask extends MyTask{
    String localAddr;
    InetUDPImp inetUDPImp;
    public UdpSendTask(ProgressDialog progressDialog,String localAddr) {
        super(progressDialog);
        this.localAddr = localAddr;
    }

    /**
     * rudp方式，发送文件。需要两个字符串参数，第一个是二维码的结果。第二个是文件路劲。
     * @param params 空
     * @return 传输文件是否成功
     */
    @Override
    protected String doInBackground(String... params) {
        final String fileName = params[1];
        final String content = params[0];
        final int[] transRes = new int[1];

        inetUDPImp = new InetUDPImp(localAddr) {
            @Override
            public void onConnect() {
                publishProgress(-1);
            }

            @Override
            public void onSendBegin() {
                publishProgress(-2);
            }

            @Override
            public void onRcvBegin(String fileName, long length) {
            }

            @Override
            public void onUpdate(int index) {
                publishProgress(index);
            }
        };

        if(inetUDPImp.connect(content) == TransBasic.CONNECT_FAIL)return "连接失败";

        Thread thread = new Thread(){
            @Override
            public void run(){
                transRes[0] = inetUDPImp.transFile(new File(fileName));
            }
        };
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        inetUDPImp.disconnect();
        return transRes[0]==TransBasic.TRANS_OK?"发送成功":"发送失败";
    }
}
