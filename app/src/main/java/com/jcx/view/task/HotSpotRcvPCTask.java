package com.jcx.view.task;

import android.app.Activity;
import android.app.AlertDialog;
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
    Activity activity;
    public HotSpotRcvPCTask(ProgressDialog progressDialog,Activity activity) {
        super(progressDialog);
        this.activity = activity;

        max = progressDialog.getMax();
    }
    /**
     * 热点方式 接收方的异步操作。需要一个字符串参数，二维码的结果
     * @param params 二维码的结果
     * @return 传输是否成功。
     */
    @Override
    protected String doInBackground(String... params) {
        final String content = params[0];
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
                publishProgress(-3, max);
            }
        };
        if(hotSpotImp.connect(content) == TransBasic.CONNECT_FAIL)return "连接失败";

        int res = hotSpotImp.receiFile();

        hotSpotImp.disconnect();
        return res ==TransBasic.RECI_OK?"发送成功":"发送失败";
    }
    @Override
    public void onPreExecute(){

    }
}
