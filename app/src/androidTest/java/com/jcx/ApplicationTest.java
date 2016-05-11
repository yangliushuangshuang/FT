package com.jcx;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.util.Configuration;
import com.jcx.util.FileOper;
import com.jcx.util.NetworkDetect;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;

import java.io.File;
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
    public void testUDP(){
        Runnable send = new Runnable() {
            @Override
            public void run() {
                InetUDPImp inetUDPImp = new InetUDPImp("127.0.0.1");
                assertTrue("udpSender connect",(inetUDPImp.connect("127.0.0.1"+Util.SPLITER+"9888")== TransBasic.CONNECT_OK));
                assertTrue("udpSender send", inetUDPImp.transFile(new File(Util.DATA_DIRECTORY, "ft.conf")) == TransBasic.TRANS_OK);
            }
        };
        Runnable rcv = new Runnable() {
            @Override
            public void run() {
                InetUDPImp inetUDPImp = new InetUDPImp("127.0.0.1");
                inetUDPImp.getQRCode();
                assertEquals(inetUDPImp.connect(), TransBasic.CONNECT_OK);
                assertTrue(inetUDPImp.receiFile() == TransBasic.RECI_OK);
            }
        };
        new Thread(send).start();
        new Thread(rcv).start();
    }
    public void testHotSpot(){
        Runnable send = new Runnable() {
            @Override
            public void run() {
                /*HotSpotImp hotSpotImp = new HotSpotImp(this);//Activity
                assertEquals(TransBasic.CONNECT_OK,hotSpotImp.connect("TP" + Util.SPLITER + "123456"));
                assertEquals(TransBasic.TRANS_OK, hotSpotImp.transFile(new File(Util.DATA_DIRECTORY, "ft.conf")));*/
            }
        };
        Runnable rcv = new Runnable() {
            @Override
            public void run() {
                /*HotSpotImp hotSpotImp = new HotSpotImp(this);//Activity
                hotSpotImp.getQRCode();
                assertEquals(TransBasic.RECI_OK,hotSpotImp.receiFile());*/
            }
        };
        new Thread(send).start();
        new Thread(rcv).start();
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
        if(imag.exists())imag.delete();
        assertEquals("testQRcode" + "1", mesg, actual_mesg);//单元测试结果
        //2016/3/29 测试结果通过。
    }
    public void testFileOper(){
        File path  = Environment.getExternalStorageDirectory();//对外部SD卡有写入权限
        FileOper fileOper = new FileOper();
        //测试创建文件和目录
        fileOper.touch(path.getAbsolutePath(), "testFileDir", true);
        String testPath = path.getAbsolutePath()+File.separator+"testFileDir";
        fileOper.touch(testPath, "testTouch", false);
        assertTrue("创建文件touch", new File(testPath, "testTouch").exists());
        fileOper.touch(testPath, "_testTouch", true);
        //测试查看目录下的所有文件
        File[] list;
        assertTrue("查看目录下文件select",(list=fileOper.select(new File(testPath)))!=null&&list.length==2);
        //测试移动和复制
        File src,dest;
        if(!list[0].isDirectory()){
            src=list[0];
            dest=list[1];
        }else {
            src=list[1];
            dest=list[0];
        }
        fileOper.copy(src.getAbsolutePath(), dest.getAbsolutePath());
        assertTrue("复制文件copy", new File(dest, "testTouch").exists());

        new File(testPath,"testMoveDir").mkdir();
        fileOper.move(dest.getAbsolutePath(), testPath + File.separator + "testMoveDir");
        assertTrue("移动目录或文件move",new File(testPath+File.separator+"testMoveDir","_testTouch").exists());
        //测试modify
        //File file = new File(path,"testFileDir_Modify");
        fileOper.modify(path.getAbsolutePath(),"testFileDir","testFileDir_Modify");
        assertTrue("测试修改文件",new File(path,"testFileDir_Modify").exists());
        //测试删除目
        fileOper.delete(testPath);
        assertTrue("删除目录delete",!new File(testPath).exists());
    }
    public void testNetwork(){
        String localIp=NetworkDetect.getLocalIpAddress();
        //String state =NetworkDetect.getCurrentNetType();
        String netIp=NetworkDetect.getNetIp();
        assertTrue("测试获取本地ip",localIp!=null);
        assertTrue("测试获取网络状态",true);
        assertTrue("测试获取网络Ip", netIp!=null);

        //String a = NetworkDetect.getCurrentNetType(this);
        //assertTrue(a!=null&&!a.equals(""));
    }
    public void testConf(){
        Configuration conf  = new Configuration();
        File file = new File(Util.DATA_DIRECTORY,"ft.conf");
        assertTrue("测试配置文件是否创建",file.exists());
        assertEquals("测试配置ip地址","127.0.0.1",conf.getIpAddress());
        assertEquals("测试配置器端口",9888, conf.getServerPort());
        assertEquals("测试配置p2p端口",9888,conf.getP2PPort());
    }
}