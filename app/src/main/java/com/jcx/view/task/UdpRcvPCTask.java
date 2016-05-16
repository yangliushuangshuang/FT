package com.jcx.view.task;

import android.app.ProgressDialog;

import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

/**
 * Created by lenovo on 2016/5/15.
 */
public class UdpRcvPCTask extends  MyTask{
    String localAddr;
    InetUDPImp inetUDPImp;
    int max;
    public UdpRcvPCTask(ProgressDialog progressDialog,String localAddr) {
        super(progressDialog);
        this.localAddr = localAddr;
        max = progressDialog.getMax();
    }
    /**
     * rudp方式，接受文件。
     * @param params 二维码的结果
     * @return 接收文件是否成功
     */
    @Override
    protected String doInBackground(String... params) {
        inetUDPImp = new InetUDPImp(localAddr) {
            @Override
            public void onConnect() {
                publishProgress(-1);
            }

            @Override
            public void onSendBegin() {

            }

            @Override
            public void onRcvBegin(String fileName, long length) {
                int max;
                if(length<=Util.BLOCK_SIZE)max = 1;
                else max = (int)Math.ceil(length/Util.BLOCK_SIZE);
                publishProgress(-3,max);
            }

            @Override
            public void onUpdate(int index) {
                publishProgress(index);
            }
        };
        final int[] rcvRes = new int[1];
        final String content = params[0];

        if(inetUDPImp.connect(content) == TransBasic.CONNECT_FAIL)return "连接失败";


        Thread thread = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = inetUDPImp.receiFile();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rcvRes[0]==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
}
