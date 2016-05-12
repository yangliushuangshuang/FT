package com.jcx.view.task;

import android.app.ProgressDialog;

import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

/**
 * Created by lenovo on 2016/5/11.
 */
public class UdpRcvTask extends MyTask{
    String localAddr;
    InetUDPImp inetUDPImp;
    public UdpRcvTask(ProgressDialog progressDialog,String localAddr) {
        super(progressDialog);
        this.localAddr = localAddr;
    }

    /**
     * rudp方式，接受文件。
     * @param params 空
     * @return 接收文件是否成功
     */
    @Override
    protected String doInBackground(String... params) {
        inetUDPImp = new InetUDPImp(localAddr);
        final int[] rcvRes = new int[1];
        Thread connect = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = inetUDPImp.connect();
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
        else if(rcvRes[0]==TransBasic.CONNECT_OK)publishProgress(-1);


        Thread thread = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = inetUDPImp.receiFile();
            }
        };
        thread.start();

        int rcvIndex;
        do{
            rcvIndex = (int) Util.rcvIndex;
            publishProgress(rcvIndex);
        }while (rcvIndex>progressDialog.getMax());

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rcvRes[0]==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
    @Override
    protected void onPreExecute(){
        Util.sendIndex=0;
        Util.rcvIndex=0;
    }
}
