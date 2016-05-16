package com.jcx.rudp;

/**
 * Created by lenovo on 2016/4/6.
 */

import com.jcx.communication.InetUDPImp;
import com.jcx.communication.Transmission;
import com.jcx.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据报发送
 * 1.发送消息线程负责发送，发送后将消息放入容器中等待应答
 * 2.接收线程接收应答，从容器中匹配后删除
 * 3.重发线程负责重发，未收到应答的消息，发送3次后移除
 * @author Administrator
 *
 */
public class DatagramSend {

    private SocketAddress localAddr; //本地要发送的地址对象
    private DatagramSocket dSender; //发送的Socket对象
    private SocketAddress destAddr; //目标地址
    private File file;
    private InetUDPImp inetUDPImp;
    //本地缓存已发送的消息Map  key为消息ID  value为消息对象本身
    Map<Integer,NetJavaMsg> msgQueue=new ConcurrentHashMap();


    public DatagramSend(File file,String localIp,String rmIp,int localPort,int rmPort,InetUDPImp inetUDPImp) throws Exception{
        localAddr=new InetSocketAddress(localIp, localPort);
        dSender=new DatagramSocket(localAddr);
        destAddr=new InetSocketAddress(rmIp,rmPort);
        this.file = file;
        this.inetUDPImp = inetUDPImp;
        //启动三个线程
        startSendThread();
        startRecvResponseThread();
        startReSendThread();


    }


    //启动发送线程
    public void startSendThread(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    send();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }}).start();

    }


    //模拟发送消息
    public void send() throws Exception{
        System.out.println("发送端-发送数据线程启动...");
        int id=0;
        FileInputStream in = new FileInputStream(file);
        while(true){
            id++;
            byte[] msgData= new byte[Util.BLOCK_SIZE];
            int len;
            if((len=in.read(msgData,0,msgData.length))==-1)break;
            //创建要发送的消息对象
            NetJavaMsg sendMsg;
            if(len!=Util.BLOCK_SIZE)sendMsg=new NetJavaMsg(id,msgData,0,len);
            else sendMsg =new NetJavaMsg(id,msgData);


            //要发送的数据：将要发送的数据转为字节数组
            byte[] buffer=sendMsg.toByte();

            //创建数据包，指定内容，指定目标地址
            DatagramPacket dp=new DatagramPacket(buffer,buffer.length,destAddr);

            dSender.send(dp);  //发送

            sendMsg.setSendCount(1);
            sendMsg.setLastSendTime(System.currentTimeMillis());
            sendMsg.setRecvRespAdd(localAddr);
            sendMsg.setDestAdd(destAddr);

            msgQueue.put(id, sendMsg);
            System.out.println("客户端-数据已发送" + sendMsg);
            inetUDPImp.onUpdate(id);
            //Thread.sleep(1000);
        }
    }


    //启动接收应答线程
    public void startRecvResponseThread(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try{
                    recvResponse();

                }catch(Exception e){
                    e.printStackTrace();
                }

            }}).start();
    }


    //接收应答消息
    public void recvResponse() throws Exception{
        System.out.println("接收端-接收应答线程启动...");
        while(true){
            byte[] recvData=new byte[Util.HELLOSHAKE_SIZE];

            //创建接收数据包对象
            DatagramPacket recvPacket=new DatagramPacket(recvData,recvData.length);

            dSender.receive(recvPacket);
            NetJavaRespMsg resp=new NetJavaRespMsg(recvPacket.getData());

            int respID=resp.getRepId();
            //inetUDPImp.onUpdate(respID);
            NetJavaMsg msg=msgQueue.get(new Integer(respID));

            if(msg!=null){
                System.out.println("接收端-已收到："+msg);
                msgQueue.remove(respID);
            }

        }
    }



    //启动重发线程
    public void startReSendThread(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try{
                    while(true){
                        resendMsg();
                        Thread.sleep(1000);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }

            }}).start();
    }


    //判断Map中的消息，如果超过3秒未收到应答，则重发
    public void resendMsg(){
        Set<Integer> keyset=msgQueue.keySet();
        Iterator<Integer> it=keyset.iterator();
        while(it.hasNext()){
            Integer key=it.next();
            NetJavaMsg msg=msgQueue.get(key);

            if(msg.getSendCount()>3){
                it.remove();
                System.out.println("***发送端---检测到丢失的消息"+msg);
            }

            long cTime=System.currentTimeMillis();
            if((cTime-msg.getLastSendTime())>3000&&msg.getSendCount()<3){
                byte[] buffer=msg.toByte();
                try{
                    DatagramPacket dp=new DatagramPacket(buffer,buffer.length,msg.getDestAdd());
                    dSender.send(dp);
                    msg.setSendCount(msg.getSendCount()+1);
                    System.out.println("客户端--重发消息:"+msg);

                }catch(Exception e){
                    e.printStackTrace();

                }
            }
        }
    }
}
