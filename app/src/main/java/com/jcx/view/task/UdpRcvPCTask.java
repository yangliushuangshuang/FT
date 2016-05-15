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
        inetUDPImp = new InetUDPImp(localAddr);
        final int[] rcvRes = new int[1];
        final String content = params[0];
        Thread connect = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = inetUDPImp.connect(content);
            }
        };
        connect.start();
        try {
            connect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            rcvRes[0] = TransBasic.CONNECT_FAIL;
        }
        if(rcvRes[0]== TransBasic.CONNECT_FAIL)return "连接失败";
        else if(rcvRes[0]==TransBasic.CONNECT_OK)publishProgress(-1,(int)Math.ceil(inetUDPImp.getLength() / Util.BLOCK_SIZE));


        Thread thread = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = inetUDPImp.receiFile();
            }
        };
        thread.start();

        int rcvIndex;
        do{
            rcvIndex = inetUDPImp.rcvIndex;
            publishProgress(rcvIndex);
        }while (rcvIndex<max);

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rcvRes[0]==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
    @Override
    public void onPreExecute(){

    }
}
