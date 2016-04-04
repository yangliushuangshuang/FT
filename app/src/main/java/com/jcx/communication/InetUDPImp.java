package com.jcx.communication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jcx.util.Configuration;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
public class InetUDPImp implements InetUDP {
	String inetAddr;

	public InetUDPImp(String inetString) {
		inetAddr = inetString;
	}

	@Override
	public int transFile(File file) {
		// TODO Auto-generated method stub


		return 0;
	}

	@Override
	public int receiFile() {
		//TODO
		return 0;
	}

	@Override
	public Bitmap getQRCode() {
		String content = inetAddr + Util.SPLITER + new Configuration().getP2PPort();
		return QRcodeUtil.encode(content, 300, 300);
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
		int port =Integer.parseInt(new Configuration().getP2PPort());
		try {
			Log.d("Shake","开始");
			DatagramSocket datagramSocket = new DatagramSocket(port);
			byte[] hello =new byte[1024];
			DatagramPacket receive = new DatagramPacket(hello,hello.length);
			datagramSocket.receive(receive);
			if(receive.getData().toString().equals("hello")){
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
	public int connect(String content) {
		String[] a = content.split(Util.SPLITER);
		String addr = a[0];
		int port = Integer.parseInt(a[1]);
		//Socket socket = new Socket();
		//InetSocketAddress inetSocketAddress = new InetSocketAddress(addr,port);
		try {
			DatagramSocket datagramSocket = new DatagramSocket(port);
			InetAddress address = InetAddress.getByAddress(Util.ipToBytes(addr));
			byte[] hello ="hello".getBytes();
			DatagramPacket send = new DatagramPacket(hello,hello.length,address,port);
			datagramSocket.send(send);
			Log.d("InetUDP", "连接开始");

			byte[] shake=new byte[1024];
			DatagramPacket receive = new DatagramPacket(shake,shake.length);
			datagramSocket.receive(receive);
			if(receive.getData().toString().equals("sure"))return CONNECT_OK;
			else return CONNECT_FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("InetUDP", "连接失败");
		}
		return CONNECT_FAIL;
	}
}