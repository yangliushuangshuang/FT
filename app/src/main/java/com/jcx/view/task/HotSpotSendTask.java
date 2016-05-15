package com.jcx.view.task;

import android.app.Activity;
import android.app.ProgressDialog;

import com.jcx.communication.HotSpotImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

import java.io.File;

/**
 * Created by lenovo on 2016/5/11.
 */
public class HotSpotSendTask extends MyTask{
    private HotSpotImp hotSpotImp;
    /**
     * 传进来的progressDiaglog不需要调用show()
     * @param progressDialog
     * @param activity
     */
    public HotSpotSendTask(ProgressDialog progressDialog,Activity activity) {
        super(progressDialog);
        hotSpotImp = new HotSpotImp(activity);
    }

    /**
     * 热点方式 发送方的异步操作。需要两个字符串参数，第一个是二维码的结果。第二个是文件路劲。
     * @param params 第一个是二维码的结果。第二个是文件路劲。
     * @return 传输是否成功。
     */
    @Override
    protected String doInBackground(String... params) {
        final String fileName = params[1];
        final String content = params[0];
        final int[] transRes = new int[1];
        Thread connect = new Thread(){
            @Override
            public void run(){
                transRes[0] = hotSpotImp.connect(content);
            }
        };
        connect.start();
        try {
            connect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            transRes[0] = TransBasic.CONNECT_FAIL;
        }
        if(transRes[0] == TransBasic.CONNECT_FAIL)return "连接失败";

        Thread thread = new Thread(){
            @Override
            public void run(){
                transRes[0] = hotSpotImp.transFile(new File(fileName));
            }
        };
        thread.start();
        int currentIndex;
        do {
            currentIndex = hotSpotImp.sendIndex;
            publishProgress(currentIndex);
        }while (currentIndex<progressDialog.getMax());

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hotSpotImp.disconnect();
        return transRes[0]==TransBasic.TRANS_OK?"发送成功":"发送失败";
    }


}
