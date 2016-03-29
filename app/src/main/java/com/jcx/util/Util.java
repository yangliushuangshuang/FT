package com.jcx.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by lenovo on 2016/3/29.
 */
public class Util {
    public final static String FILES="ftFiles";//数据文件夹名
    public final static File DATA_DIRECTORY= Environment.getDataDirectory();
    public static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
