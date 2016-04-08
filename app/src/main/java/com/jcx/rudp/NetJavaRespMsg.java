package com.jcx.rudp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by lenovo on 2016/4/6.
 */
public class NetJavaRespMsg {



    private int totalLen;

    private int repId;  //回复对应接收到的消息ID

    private byte state=0; //状态 0：正确接收          其它：错误

    private long resTime; //应答方的发送时间



    public NetJavaRespMsg(int repId,byte state,long resTime){

        this.repId=repId;

        this.state=state;

        this.resTime=resTime;

        totalLen=4+4+1+8;



    }



    public NetJavaRespMsg(byte[] udpData){

        try{

            ByteArrayInputStream bins=new ByteArrayInputStream(udpData);

            DataInputStream dins=new DataInputStream(bins);





            this.totalLen=dins.readInt();

            this.repId=dins.readInt();

            this.state=dins.readByte();

            this.resTime=dins.readLong();







        }catch(Exception e){

            e.printStackTrace();

        }

    }





    public byte[] toByte(){

        try{

            ByteArrayOutputStream bous=new ByteArrayOutputStream();

            DataOutputStream dous=new DataOutputStream(bous);

            dous.writeInt(this.totalLen);

            dous.writeInt(this.repId);

            dous.writeByte(this.state);

            dous.writeLong(this.resTime);

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

        return "totalLen:"+this.totalLen+"  respID"+this.repId+"  state"+this.state+"  resTime"+resTime;



    }

    public int getTotalLen() {

        return totalLen;

    }

    public void setTotalLen(int totalLen) {

        this.totalLen = totalLen;

    }

    public int getRepId() {

        return repId;

    }

    public void setRepId(int repId) {

        this.repId = repId;

    }

    public byte getState() {

        return state;

    }

    public void setState(byte state) {

        this.state = state;

    }

    public long getResTime() {

        return resTime;

    }

    public void setResTime(long resTime) {

        this.resTime = resTime;

    }
}