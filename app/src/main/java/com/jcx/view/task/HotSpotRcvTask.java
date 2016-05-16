package com.jcx.view.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import com.jcx.communication.HotSpotImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Util;

import java.net.ServerSocket;

/**
 * Created by churongShaw on 2016/5/11.
 */
public class HotSpotRcvTask extends MyTask {
    private HotSpotImp hotSpotImp;
    private int max;
    private Activity activity;
    private AlertDialog alertDialog;
    public HotSpotRcvTask(ProgressDialog progressDialog,Activity activity,AlertDialog alertDialog) {
        super(progressDialog);
        this.activity = activity;
        this.alertDialog = alertDialog;
        max = progressDialog.getMax();
    }

    /**
     * 热点方式，接受文件。参数为null
     * @param params 空
     * @return 接受文件是否成功
     */
    @Override
    protected String doInBackground(String... params) {
        hotSpotImp = new HotSpotImp(activity) {
            @Override
            public void onConnect() {
                publishProgress(-1);
            }

            @Override
            public void onUpdate(int index) {
                publishProgress(index);
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
        };

        if(hotSpotImp.connect()== TransBasic.CONNECT_FAIL)return "连接失败";

        int res=hotSpotImp.receiFile();
        hotSpotImp.disconnect();
        return res==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
    @Override
    public void onPreExecute(){

    }
    @Override
    protected void onPostExecute(String b) {
        super.onPostExecute(b);
        alertDialog.dismiss();
    }
}
