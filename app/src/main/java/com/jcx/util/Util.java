package com.jcx.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Random;

/**
 * Created by churongShaw on 2016/3/29.
 */
public class Util {
    //private final static String FILES="ftFiles";//数据文件夹名
    public final static String DATA_DIRECTORY= Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"ftFiles";
    public final static String SPLITER="/_/";
    public final static String RECEIVE_DIR=DATA_DIRECTORY+File.separator+"FileRec";
    public static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
    public static byte[] ipToBytes(String addr){
        byte[] res = new byte[4];
        String[] addrs = addr.split(".");
        for(int i=0;i<4;i++){
            int aInt = Integer.parseInt(addrs[i]);
            res[i]=(byte)aInt;
        }
        return res;
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

    /**
     * Reader到Writer
     * @param reader 输入字符流
     * @param writer 输出字节流
     * @return 是否成功
     */
    public static boolean copyFile(Reader reader,Writer writer){
        char buf[] = new char[1024];
        int len;
        try {
            while ((len = reader.read(buf)) != -1) {
                writer.write(buf, 0, len);
            }
            writer.flush();
            reader.close();
            writer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
