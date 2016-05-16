package com.jcx.util;

import android.os.Environment;

import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.communication.Transmission;

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
import java.net.SocketTimeoutException;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by churongShaw on 2016/3/29.
 */
public class Util {
    public final static String DATA_DIRECTORY= Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"ftFiles";
    public final static String SPLITER="/_/";
    public final static String RECEIVE_DIR=DATA_DIRECTORY+File.separator+"FileRec";
    public final static int SOCKET_TIMEOUT=12000;
    public final static int BLOCK_SIZE=1024*10;
    public final static int HELLOSHAKE_SIZE=64;

    public final static int HEAD_LEN = Long.SIZE/8;
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

    public static byte[] long2bytes(long num) {
        byte[] b = new byte[HEAD_LEN];
        for (int i=0;i<HEAD_LEN;i++) {
            b[i] = (byte)(num>>>(56-(i*HEAD_LEN)));
        }
        return b;
    }
    public static long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i=0;i<HEAD_LEN;i++) {
            res <<= HEAD_LEN;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }
    /**
     * 接收文件基本信息和传输过程中的信息
     * @param socket socket
     * @return 返回收到的内容
     */
    public static String receiveInfo(ServerSocket socket){
        Socket client=null;
        StringBuilder builder = new StringBuilder();
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT*20);
            client = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"));
            char[] buf = new char[HELLOSHAKE_SIZE];
            int len;
            while((len=reader.read(buf,0,buf.length))!=-1)builder.append(buf,0,len);
            builder.append(SPLITER+client.getLocalAddress().getHostAddress());
            reader.close();
            client.close();
            /*DatagramSocket socket = new DatagramSocket(port);
            byte[] buf = new byte[Util.HELLOSHAKE_SIZE];
            DatagramPacket packet = new DatagramPacket(buf,buf.length);
            socket.receive(packet);
            socket.close();
            return new String(packet.getData());*/
        } catch(SocketTimeoutException e1){
            e1.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return builder.toString();
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
