package com.jcx.view.task;

import android.app.Activity;
import android.app.ProgressDialog;

import com.jcx.communication.HotSpotImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

/**
 * Created by lenovo on 2016/5/15.
 */
public class HotSpotRcvPCTask extends MyTask{
    private HotSpotImp hotSpotImp;
    private int max;
    public HotSpotRcvPCTask(ProgressDialog progressDialog,Activity activity) {
        super(progressDialog);
        hotSpotImp = new HotSpotImp(activity);
        max = progressDialog.getMax();
    }
    /**
     * 热点方式 接收方的异步操作。需要一个字符串参数，二维码的结果
     * @param params 二维码的结果
     * @return 传输是否成功。
     */
    @Override
    protected String doInBackground(String... params) {
        final int[] rcvRes = new int[1];
        final String content = params[0];
        Thread connect = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = hotSpotImp.connect(content);
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
        else if(rcvRes[0]==TransBasic.CONNECT_OK)publishProgress(-1,(int)Math.ceil(hotSpotImp.getlength() / Util.BLOCK_SIZE));

        Thread thread = new Thread(){
            @Override
            public void run(){
                rcvRes[0] = hotSpotImp.receiFile();
            }
        };
        thread.start();

        int rcvIndex;
        do{
            rcvIndex = hotSpotImp.rcvIndex;
            publishProgress(rcvIndex);
        }while (rcvIndex<max);

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hotSpotImp.disconnect();
        return rcvRes[0]==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
}
