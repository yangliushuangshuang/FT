package com.jcx.util;

import android.os.Environment;

import java.io.File;
import java.util.Random;

/**
 * Created by churongShaw on 2016/3/29.
 */
public class Util {
    //private final static String FILES="ftFiles";//数据文件夹名
    public final static String DATA_DIRECTORY= Environment.getDataDirectory().getAbsolutePath()+File.separator+"ftFiles";
    public final static String SPLITER="/_/";
    public static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    /**
     * 生成指定位数的随机密码
     * @param nums 生成随机密码的位数
     * @return 生成的密码
     */
    public static String randPsw(int nums){
        Random random = new Random();
        StringBuffer str = new StringBuffer();
        for(int i=0;i<nums;i++){
            str.append(random.nextInt(10));
        }
        return str.toString();
    }
}
