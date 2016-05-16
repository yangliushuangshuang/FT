package com.jcx.rudp;


import com.jcx.communication.InetUDPImp;
import com.jcx.communication.Transmission;
import com.jcx.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by lenovo on 2016/4/6.
 */
public class DatagramReceive {

    private SocketAddress localAddr;
    private DatagramSocket dSender;
    private File file;
    private InetUDPImp inetUDPImp;
    public DatagramReceive(File file, String localIp, int port,InetUDPImp inetUDPImp) throws Exception{
        localAddr=new InetSocketAddress(localIp,port);
        dSender=new DatagramSocket(localAddr);
        this.file = file;
        this.inetUDPImp = inetUDPImp;
        //启动接收线程
        startRecvThread();
    }


    public void startRecvThread(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try{
                    recvMsg();

                }catch(Exception e){
                    e.printStackTrace();

                }

            }}).start();
    }


    public void recvMsg() throws Exception{
        System.out.println("接收线程启动");
        while(true){
            byte[] recvData=new byte[100];
            DatagramPacket recvPacket=new DatagramPacket(recvData,recvData.length);
            dSender.receive(recvPacket);
            FileOutputStream out = new FileOutputStream(file,true);
            NetJavaMsg recvMsg=new NetJavaMsg(recvPacket.getData());
            out.write(recvMsg.getData());
            NetJavaRespMsg resp=new NetJavaRespMsg(recvMsg.getId(),(byte)0,System.currentTimeMillis());

            inetUDPImp.onUpdate(recvMsg.getId());
            byte[] data=resp.toByte();
            DatagramPacket dp=new DatagramPacket(data,data.length,recvPacket.getSocketAddress());
            dSender.send(dp);

            System.out.println("接收端-已发送应答"+resp);
        }
    }
}
