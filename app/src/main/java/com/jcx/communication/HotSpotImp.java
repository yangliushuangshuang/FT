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

public abstract class HotSpotImp extends Transmission {
	private final static String WIFINAME="TP";
	private Activity context;
	private String wifiName;
	private String rmAddr;
	private String addr;
	private int port;
	private int rmPort;
	private WifiManageUtils wifiManageUtils;

	private final static String HAND_SHAKE= "handshake";
	private ServerSocket socket;
	public HotSpotImp(Activity context){
		this.context = context;
		wifiManageUtils = new WifiManageUtils(context);
		wifiName = WIFINAME;
		port = new Configuration().getP2PPort();
	}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		SocketManager socketManager = new SocketManager(null);
		if(socketManager.sendFile(file, rmAddr, rmPort,this))return TRANS_OK;
		return TRANS_FAIL;
	}

	@Override
	public int receiFile() {
		SocketManager socketManager = new SocketManager(socket);
		if(socketManager.ReceiveFile(this))return RECI_OK;
		return RECI_FAIL;
	}
	public int connect(){
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String res = Util.receiveInfo(socket);
		if("".equals(res)){
			disconnect();
			return CONNECT_FAIL;
		}
		String[] info = res.split(Util.SPLITER);
		try{
			if(info.length>2){
				rmAddr = info[2];
				rmPort = Integer.parseInt(info[1]);
			}
		}catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			disconnect();
			return CONNECT_FAIL;
		}
		onConnect();
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

		wifiManageUtils.openWifi();
		wifiManageUtils.startscan();
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
		WifiConfiguration netConfig = wifiManageUtils.getCustomeWifiClientConfiguration(wifiName, psw, 3);
		for(int i=0;i<10&&!wifiManageUtils.isConnected(wifiName);i++){
			wifiManageUtils.startscan();
			wifiManageUtils.addNetwork(netConfig);
		}
		if(!wifiManageUtils.isConnected(wifiName))return CONNECT_FAIL;

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
		addr = NetworkDetect.getLocalIpAddress();
		Util.sendInfo(rmAddr,rmPort,HAND_SHAKE+Util.SPLITER+port);
		onConnect();
		return CONNECT_OK;
	}

	/**
	 * 接收文件端调用
	 * @return 二维码
	 */
	public static Bitmap getQRCode(int heigth,int width,Activity activity) {
		WifiManageUtils wifiManageUtils = new WifiManageUtils(activity);
		String wifiName = WIFINAME;
		String psw = Util.randPsw(10);
		//psw="123456789";
		wifiManageUtils.closeWifi();
		wifiManageUtils.stratWifiAp(wifiName, psw,3);
		String content = wifiName+Util.SPLITER+psw+Util.SPLITER+new Configuration().getP2PPort();
		System.out.println("hotSpotImg -------->psw:"+psw);
		return QRcodeUtil.encode(content,heigth,width);
	}

	@Override
	public void disconnect() {
		if(wifiManageUtils!=null){
			wifiManageUtils.closeWifiAp();
			WifiConfiguration config = wifiManageUtils.isExist(wifiName);
			if(config!=null)wifiManageUtils.removeNetwork(config.networkId);
			wifiManageUtils.closeWifi();
		}
		if(socket!=null) try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public abstract void onConnect();
	public abstract void onUpdate(int index);
	public abstract void onSendBegin();
	public abstract void onRcvBegin(String fileName,long length);
}
