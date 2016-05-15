package com.jcx.view.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jcx.util.Util;

/**
 * you must override doInBackground()
 * Created by churongShaw on 2016/5/11.
 */
public abstract class MyTask extends AsyncTask<String,Integer,String> {
    ProgressDialog progressDialog=null;
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
        if(!progressDialog.isShowing())progressDialog.show();
        progressDialog.setTitle(b);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }
    @Override
    protected void onProgressUpdate(Integer... param){
        if(param[0]==-1){
            progressDialog.setMax(param[1]);
            if(!progressDialog.isShowing())progressDialog.show();
        }else if(param[0]>0){
            progressDialog.setProgress(param[0]);
        }
    }
}
