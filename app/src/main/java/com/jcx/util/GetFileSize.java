package com.jcx.util;

import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by Cui on 16-4-26.
 */
public class GetFileSize {
    private LruCache<String,String> mCache;

    public GetFileSize(){
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheMemorySize=maxMemory/4;
        mCache=new LruCache<String,String>(cacheMemorySize){
            @Override
            protected int sizeOf(String key, String value) {
                return value.length();
            }
        };
    }

    /**
     * 将文件的大小以键值对的形式存储在lrucache中
     * @param path 键，文件的路径
     * @param file_size 值，文件的大小
     */
    private void addSizeToCache(String path,String file_size){
        if (getSizeFromCache(path) == null) {
            mCache.put(path, file_size);
        }
    }

    /**
     * 从lrucache中去除对应文件的大小
     * @param path 要去文件的路径
     * @return 文件的大小
     */
    private String getSizeFromCache(String path){
        return mCache.get(path);
    }

    /**
     * 异步将文件的大小显示在listview中
     * @param tv_file_size textView
     * @param path 文件的路径
     */
    public void asyncLoadFileSize(final TextView tv_file_size, final String path){
        if (getSizeFromCache(path) == null) {
            new AsyncTask<Void, Void, String>() {
                String size=null;
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        size=FormetFileSize(getFolderOrFileSize(path));
                        addSizeToCache(path,size);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return size;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (tv_file_size .getTag()==path) {
                        System.out.println("GetFileSize s : "+s);
                        tv_file_size.setText(s);
                    }
                }
            }.execute();
        }else {
            if (tv_file_size.getTag().equals(path)) {
                tv_file_size.setText(mCache.get(path));
                System.out.println("GetFileSize mCache.get(path) : " + mCache.get(path));
            }
        }
    }

    /**
     * 获得文件的大小
     * @param file_path 文件的绝对路径
     * @return int类型的文件大小
     */
    public int getFileSize(String file_path){
        File file=new File(file_path);
        FileInputStream fis;
        int fileLen=0;
        try {
            fis = new FileInputStream(file);
            fileLen = fis.available();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLen;
    }

    /**
     * 获得文件或者文件夹的大小
     * @param file_path
     * @return
     */
    public int getFolderOrFileSize(String file_path){
        File file=new File(file_path);
        int fileSize=0;
        if (file.isDirectory()) {
            File file_list[]=file.listFiles();
            for (File temp_file:file_list) {
                if (temp_file.isDirectory()) {
                    fileSize=fileSize+getFolderOrFileSize(temp_file.getAbsolutePath());
                }else {
                    fileSize=fileSize+getFileSize(temp_file.getAbsolutePath());
                }
            }
        }else {
            fileSize=getFileSize(file_path);
        }
        return fileSize;
    }

    /**
     * 转换文件大小的格式
     * @param fileS 长整型的文件大小
     * @return
     */
    public String FormetFileSize(long fileS)
    {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024)
        {
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576)
        {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        }
        else if (fileS < 1073741824)
        {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        }
        else
        {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        if (fileS == 0) {
            fileSizeString="0B";
        }
        return fileSizeString;
    }

}
