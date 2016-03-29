package com.jcx;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.jcx.util.QRcodeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
    @Override
    public void setUp() throws Exception{
        super.setUp();
    }

    /**
     * 二维码单元测试方法，将字符串生成二维码图像后，再对图像进行解析，得到结果与初始化字符串对比。
     * @throws Exception
     */
    public void testQRCode() throws Exception{
        //String mesg="123456";
        String mesg = "1a2b3c4d5efqwe./";
        File imagPath= Environment.getExternalStorageDirectory();//对外部SD卡有写入权限
        Log.d("testQRcode", "testQRCode");
        Bitmap bitmap = QRcodeUtil.encode(mesg, 300, 300);//生成内容为123456的二维码的位图
        File imag = new File(imagPath,"code.png");
        if(!imag.exists())imag.createNewFile();
        FileOutputStream out = new FileOutputStream(imag);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);//位图保存到本地
        String actual_mesg = QRcodeUtil.decode(imag.getAbsolutePath());//二维码解码过程
       imag.delete();
        assertEquals("testQRcode" + "1", mesg, actual_mesg);//单元测试结果
        //2016/3/29 测试结果通过。
    }
    public void testFileOper(){
        File path  = Environment.getExternalStorageDirectory();//对外部SD卡有写入权限

    }
}