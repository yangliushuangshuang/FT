package com.jcx.view.task;

import android.app.Activity;
import android.app.AlertDialog;
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
    private Activity activity;
    /**
     * 传进来的progressDiaglog不需要调用show()
     * @param progressDialog
     * @param activity
     */
    public HotSpotSendTask(final ProgressDialog progressDialog,Activity activity) {
        super(progressDialog);
        this.activity = activity;
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
                publishProgress(-2);
            }

            @Override
            public void onRcvBegin(String fileName, long length) {
            }
        };
        if(hotSpotImp.connect(content) == TransBasic.CONNECT_FAIL)return "连接失败";

        int res = hotSpotImp.transFile(new File(fileName));

        hotSpotImp.disconnect();
        return res==TransBasic.TRANS_OK?"发送成功":"发送失败";
    }


}
