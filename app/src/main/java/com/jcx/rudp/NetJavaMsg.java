package com.jcx.rudp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.SocketAddress;

/**
 * Created by lenovo on 2016/4/6.
 */
public class NetJavaMsg {

    private int totalLen; //数据的长度
    private int id;  //唯一的ID
    private byte[] data; //消息内容

    //本地参数，为简化起见，不发送
    private SocketAddress recvRespAdd;  //发送者接收应答的地址
    private SocketAddress destAdd;  //接收者地址

    private int sendCount=0; //发送次数
    private long lastSendTime; //最后一次发送的时间

    /**
     *
     * @param id  //唯一的序号
     * @param data  //数据内容
     */
    public NetJavaMsg(int id,byte[] data){
        this.id=id;
        this.data=data;
        totalLen=4+4+data.length;
    }
    public NetJavaMsg(int id,byte[] data,int offset, int length){
        this.id=id;
        byte[] temp = new byte[length];
        for(int i=0;i<length;i++)temp[i]=data[offset+i];
        this.data=temp;
        totalLen=4+4+length;
    }

    /**
     *
     * @param udpData  //将受到的udp数据解析为NetJavaMsg对象
     */
    public NetJavaMsg(byte[] udpData){
        try{
            ByteArrayInputStream bins=new ByteArrayInputStream(udpData);
            DataInputStream dins=new DataInputStream(bins);

            this.totalLen=dins.readInt();
            this.id=dins.readInt();

            this.data=new byte[totalLen-4-4];
            dins.readFully(data);


        }catch(Exception e){
            e.printStackTrace();
        }

    }


    public byte[] toByte(){
        try{
            ByteArrayOutputStream bous=new ByteArrayOutputStream();
            DataOutputStream dous=new DataOutputStream(bous);
            dous.writeInt(totalLen);
            dous.writeInt(id);
            dous.write(data);
            dous.flush();

            return bous.toByteArray();

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "id:"+id+"  content"+new String(data)+"  totalLen"+totalLen+" sengerAdd:"+recvRespAdd+"  destAdd:"+destAdd;
    }


    public int getTotalLen() {
        return totalLen;
    }

    public void setTotalLen(int totalLen) {
        this.totalLen = totalLen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public SocketAddress getRecvRespAdd() {
        return recvRespAdd;
    }

    public void setRecvRespAdd(SocketAddress recvRespAdd) {
        this.recvRespAdd = recvRespAdd;
    }

    public SocketAddress getDestAdd() {
        return destAdd;
    }

    public void setDestAdd(SocketAddress destAdd) {
        this.destAdd = destAdd;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public long getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(long lastSendTime) {
        this.lastSendTime = lastSendTime;
    }
}

