package com.jcx.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by churongShaw on 2016/3/29.
 */
public class Util {
    //private final static String FILES="ftFiles";//数据文件夹名
    public final static String DATA_DIRECTORY= Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"ftFiles";
    public final static String SPLITER="/_/";
    public final static String RECEIVE_DIR=DATA_DIRECTORY+File.separator+"FileRec";
    public final static int SOCKET_TIMEOUT=5000;
    public final static int BLOCK_SIZE=1024*10;
    public final static int HELLOSHAKE_SIZE=64;
    public static long rcvIndex;
    public static long sendIndex;
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
        StringBuilder str = new StringBuilder();
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
    public static boolean copyFile(Reader reader,Writer writer,boolean isIn){
        char buf[] = new char[1024];
        int len;
        try {
            sendIndex = 0;
            rcvIndex = 0;
            while ((len = reader.read(buf)) != -1) {
                int offset=0;
                if(isIn){
                    byte[] a = new byte[8];
                    for(int i=0;i<8;i++)a[i]=(byte)buf[i];
                    rcvIndex = bytes2long(a);
                    offset = a.length;
                }
                else {
                    byte[] a = long2bytes(sendIndex);
                    for(int i=0;i<a.length;i++)buf[i] = (char) a[i];
                    sendIndex++;
                }
                writer.write(buf, offset, len);
            }
            writer.flush();
            reader.close();
            writer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public static long getSendIndex(){
        return sendIndex;
    }
    public static long getRcvIndex(){
        return rcvIndex;
    }
    public static byte[] long2bytes(long num) {
        byte[] b = new byte[8];
        for (int i=0;i<8;i++) {
            b[i] = (byte)(num>>>(56-(i*8)));
        }
        return b;
    }
    public static long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i=0;i<8;i++) {
            res <<= 8;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }
    /**
     * 接收文件基本信息和传输过程中的信息
     * @param port socket的端口
     * @return 返回收到的内容
     */
    public static String receiveInfo(int port){
        try {
            ServerSocket socket = new ServerSocket(port);
            Socket client = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"));
            StringBuilder builder = new StringBuilder();
            char[] buf = new char[HELLOSHAKE_SIZE];
            int len;
            while((len=reader.read(buf,0,buf.length))!=-1)builder.append(buf,0,len);
            client.close();
            socket.close();
            return builder.toString();
            /*DatagramSocket socket = new DatagramSocket(port);
            byte[] buf = new byte[Util.HELLOSHAKE_SIZE];
            DatagramPacket packet = new DatagramPacket(buf,buf.length);
            socket.receive(packet);
            socket.close();
            return new String(packet.getData());*/
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送要传输文件的字节数和文件名  tcp协议
     * @param ip 对方ip地址，点分十进制
     * @param port 端口
     * @return 返回发送结果，true成功，false失败,失败原因可
     */
    public static boolean sendInfo(String ip,int port,String info){
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), SOCKET_TIMEOUT);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            writer.write(info);
            writer.flush();
            socket.close();
           /* byte[] data = info.getBytes();
            InetAddress address = InetAddress.getByAddress(Util.ipToBytes(ip));
            DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();*/
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
