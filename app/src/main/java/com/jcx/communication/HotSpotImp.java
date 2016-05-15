package com.jcx.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.jcx.hotspot.WifiManageUtils;
import com.jcx.util.Configuration;
import com.jcx.util.NetworkDetect;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HotSpotImp extends Transmission {
	private Activity context;
	private String wifiName;
	private String psw;
	private String rmAddr;
	private String addr;
	private int port;
	private int rmPort;
	private WifiManageUtils wifiManageUtils;
	private String fileName="";
	private long length;
	private final static String HAND_SHAKE= "handshake";
	private ServerSocket socket;
	public int sendIndex;
	public int rcvIndex;
	public HotSpotImp(Activity context){
		this.context = context;
		wifiManageUtils = new WifiManageUtils(context);
		wifiName = "TP";
		port = new Configuration().getP2PPort();
	}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		length = file.length();
		if(!Util.sendInfo(rmAddr,rmPort,file.getName()+Util.SPLITER+length))return TRANS_FAIL;
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(rmAddr,rmPort),Util.SOCKET_TIMEOUT);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			if(Util.copyFile(br,bw,false,this)){
				socket.close();
				return TRANS_OK;
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return TRANS_FAIL;
	}

	@Override
	public int receiFile() {
		try {
			String fileInfo = Util.receiveInfo(socket);
			if(fileInfo.equals(""))return RECI_FAIL;
			String[] fileInfos = fileInfo.split(Util.SPLITER);
			fileName = fileInfos[0];
			Log.d("rcv","接收到文件信息"+length);
			length = Long.parseLong(fileInfos[1]);
			Socket client = socket.accept();
			Log.d("reciFile","通过了阻塞，即socket通信开始");
			File file = new File(Util.RECEIVE_DIR+File.separator+fileName);
			if(!file.getParentFile().exists())if(!file.getParentFile().mkdirs())return RECI_FAIL;
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Util.copyFile(br, bw, true,this);
			client.close();
			if(file.exists())return RECI_OK;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return RECI_FAIL;
	}
	public int connect(){
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] info = Util.receiveInfo(socket).split(Util.SPLITER);
		if(info.length>0){
			rmAddr = info[1];
			rmPort = Integer.parseInt(info[2]);
		}
		return info[0].equals(HAND_SHAKE)?CONNECT_OK:CONNECT_FAIL;
	}
	@Override
	public int connect(Drawable qrCode) {
		return CONNECT_FAIL;
	}

	/**
	 * 扫描端（发送文件）二维码端调用。
	 * @param content 扫描二维码结果
	 * @return 返回连接结果。
	 */
	public int connect(final String content){
		String[] info = content.split(Util.SPLITER);
		String wifiName = info[0],psw=info[1];
		rmPort = Integer.parseInt(info[2]);
		ConnectRunnable connect = new ConnectRunnable(wifiName,psw,3);
		Thread connectThread = new Thread(connect);
		connectThread.start();
		try {
			connectThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(!wifiManageUtils.isConnected(wifiName))return CONNECT_FAIL;
		addr = NetworkDetect.getLocalIpAddress();
		boolean iptoready =false;
		while (!iptoready)
		{
			try
			{
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			}
			catch (InterruptedException ie)
			{
				ie.printStackTrace();
				return CONNECT_FAIL;
			}

			DhcpInfo dhcp = new WifiManageUtils(context).getDhcpInfo();
			int ipInt = dhcp.gateway;
			if (ipInt != 0)
			{
				rmAddr = Util.intToIp(ipInt);
				iptoready = true;
			}
		}
		Util.sendInfo(rmAddr,rmPort,HAND_SHAKE+Util.SPLITER+port);
		return CONNECT_OK;
	}
	class ConnectRunnable implements Runnable{
		private String wifiName;
		private String psw;
		private int type;
		public ConnectRunnable(String wifiName,String psw,int type){
			this.psw = psw;
			this.type = type;
			this.wifiName = wifiName;
		}
		@Override
		public void run() {
			wifiManageUtils.openWifi();
			WifiConfiguration config = wifiManageUtils.isExist(wifiName);
			if(config!=null)wifiManageUtils.removeNetwork(config.networkId);
			while(wifiManageUtils.getWifiState()== WifiManager.WIFI_STATE_ENABLING){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			wifiManageUtils.startscan();
			WifiConfiguration netConfig = wifiManageUtils.getCustomeWifiClientConfiguration(wifiName, psw, type);
			for(int i=0;i<100&&!wifiManageUtils.isConnected(wifiName);i++){
				wifiManageUtils.startscan();
				wifiManageUtils.addNetwork(netConfig);
			}
		}
	}
	/**
	 * 接收文件端调用
	 * @return 二维码
	 */
	@Override
	public Bitmap getQRCode(int heigth,int width) {
		WifiManageUtils wifiManageUtils = new WifiManageUtils(context);
		psw = Util.randPsw(10);
		//psw="123456789";
		wifiManageUtils.closeWifi();
		wifiManageUtils.stratWifiAp(wifiName, psw,3);
		String content = wifiName+Util.SPLITER+psw+Util.SPLITER+port;
		System.out.println("hotSpotImg -------->psw:"+psw);
		return QRcodeUtil.encode(content,heigth,width);
	}

	@Override
	public void disconnect() {
		if(wifiManageUtils!=null){
			wifiManageUtils.closeWifiAp();
			wifiManageUtils.closeWifi();
			WifiConfiguration config = wifiManageUtils.isExist(wifiName);
			if(config!=null)wifiManageUtils.removeNetwork(config.networkId);
		}
		if(socket!=null) try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getFileName(){
		return fileName;
	}
	public long getlength(){
		return length;
	}
}
