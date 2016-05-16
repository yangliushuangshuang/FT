package com.jcx.view.task;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jcx.util.Util;

/**
 * you must override doInBackground()
 * Created by churongShaw on 2016/5/11.
 */
public abstract class MyTask extends AsyncTask<String,Integer,String> {
    protected ProgressDialog progressDialog=null;
    public MyTask(ProgressDialog progressDialog){
        this.progressDialog = progressDialog;
    }
    @Override
    protected void onPreExecute(){
        if(!progressDialog.isShowing())
            progressDialog.show();
    }
    @Override
    protected void onPostExecute(String b){
        progressDialog.setTitle(b);
        progressDialog.show();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }
    @Override
    protected void onProgressUpdate(Integer... param){
        if(param[0]<0){
            switch (param[0]){
                case -1 :progressDialog.setTitle("连接成功！等待响应");break;
                case -2 :progressDialog.setTitle("开始发送...");break;
                case -3 :progressDialog.setTitle("开始接收文件");progressDialog.setMax(param[1]);break;
            }
            progressDialog.show();
        }else progressDialog.setProgress(param[0]);
    }
}
