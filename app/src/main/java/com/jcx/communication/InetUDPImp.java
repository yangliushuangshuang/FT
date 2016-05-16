package com.jcx.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jcx.rudp.DatagramReceive;
import com.jcx.rudp.DatagramSend;
import com.jcx.util.Configuration;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;


public abstract class InetUDPImp extends Transmission {
	private String inetAddr;
	private String rmInetAddr;
	private int rmPort;
	private long length;
	private String name;
	private int port = new Configuration().getP2PPort();
	private final static String HAND_SHAKE="hello";
	/**
	 *
	 * @param inetString 本机IP地址
	 */
	public InetUDPImp(String inetString) {
		inetAddr = inetString;
	}
	public String getLoaclAddr(){return inetAddr;}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		if(!Util.sendInfo(rmInetAddr,rmPort,file.getName()+Util.SPLITER+file.getTotalSpace()))return TRANS_FAIL;
		try {
			new DatagramSend(file,inetAddr,rmInetAddr,port,rmPort,this);
		} catch (Exception e) {
			e.printStackTrace();
			return TRANS_FAIL;
		}
		return TRANS_OK;
	}

	@Override
	public int receiFile() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String info=Util.receiveInfo(socket);
		if(info.equals(""))return RECI_FAIL;
		String[] fileInfo = info.split(Util.SPLITER);
		name = fileInfo[0];
		length = Long.parseLong(fileInfo[1]);
		try {
			File file = new File(Util.RECEIVE_DIR,name);
			if(file.exists())file.delete();
			file.createNewFile();
			new DatagramReceive(file,inetAddr,port,this);
			return RECI_OK;
		} catch (Exception e) {
			e.printStackTrace();
			return RECI_FAIL;
		}
	}

	public static Bitmap getQRCode(int heigth , int width,String localAddr) {
		String content = localAddr + Util.SPLITER + new Configuration().getP2PPort();
		return QRcodeUtil.encode(content,width, heigth);
	}

	@Override
	public void disconnect() {

	}

	@Override
	public int connect(Drawable qrCode) {
		String a = QRcodeUtil.decode(((BitmapDrawable) qrCode).getBitmap());
		return connect(a);
	}

	/**
	 * 从设备调用该方法，等待主设备发来的udp包，进行握手。
	 * @return CONNECT_OK表示成功，反之则CONNECT_FAIL
	 */
	public int connect(){
		try {
			Log.d("Shake","开始");
			DatagramSocket datagramSocket = new DatagramSocket(port);
			byte[] hello =new byte[Util.HELLOSHAKE_SIZE];
			DatagramPacket receive = new DatagramPacket(hello,hello.length);
			datagramSocket.receive(receive);
			if(new String(receive.getData()).lastIndexOf(HAND_SHAKE)>-1){
				InetAddress addr = receive.getAddress();
				int rmPort = receive.getPort();
				byte[] sure = "sure".getBytes();
				DatagramPacket send = new DatagramPacket(sure,sure.length,addr,rmPort);
				datagramSocket.send(send);
				return CONNECT_OK;
			}else return CONNECT_FAIL;
		} catch (SocketException e) {
			Log.d("Shake","udp socket错误");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return CONNECT_FAIL;
	}

	/**
	 * 主设备调用
	 * @param content 解析二维码得到的内容
	 * @return CONNECT_OK表示成功，反之则CONNECT_FAIL
	 */
	public int connect(String content) {
		String[] a = content.split(Util.SPLITER);
		rmInetAddr = a[0];
		rmPort = Integer.parseInt(a[1]);
		//Socket socket = new Socket();
		//InetSocketAddress inetSocketAddress = new InetSocketAddress(addr,port);
		try {
			DatagramSocket datagramSocket = new DatagramSocket(rmPort);
			InetAddress address = InetAddress.getByAddress(Util.ipToBytes(rmInetAddr));
			byte[] hello =(HAND_SHAKE+Util.SPLITER+port).getBytes();
			DatagramPacket send = new DatagramPacket(hello,hello.length,address,rmPort);
			datagramSocket.send(send);
			Log.d("InetUDPImp", "连接开始");

			byte[] shake=new byte[Util.HELLOSHAKE_SIZE];
			DatagramPacket receive = new DatagramPacket(shake,shake.length);
			datagramSocket.receive(receive);
			if(new String(receive.getData()).equals("sure"))return CONNECT_OK;
			else return CONNECT_FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("InetUDPImp", "连接失败");
		}
		return CONNECT_FAIL;
	}
	public String getFileName(){
		return name;
	}
	public long getLength(){
		return length;
	}
	public abstract void onConnect();
	public abstract void onSendBegin();
	public abstract void onRcvBegin(String fileName,long length);
	public abstract void onUpdate(int index);
}