package com.jcx.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import com.google.zxing.qrcode.encoder.QRCode;
import com.jcx.hotspot.WifiAdmin;
import com.jcx.hotspot.WifiApAdmin;
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

public class HotSpotImp implements HotSpot {
	private Activity context;
	private String wifiName;
	private String psw;
	private WifiAdmin wifiAdmin;
	private String rmAddr;
	private String addr;
	private int port;
	private int rmPort;
	public HotSpotImp(Activity context){
		this.context = context;
		wifiName = "TP";
		port = new Configuration().getP2PPort();
	}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		if(!Util.sendInfo(rmAddr,rmPort,file.getName()+Util.SPLITER+file.getTotalSpace()))return TRANS_FAIL;
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(rmAddr,rmPort),Util.SOCKET_TIMEOUT);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			if(Util.copyFile(br,bw))return TRANS_OK;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return TRANS_FAIL;
	}

	@Override
	public int receiFile() {
		try {
			String[] fileInfo = Util.receiveInfo(port).split(Util.SPLITER);
			String name = fileInfo[0];
			//TODO
			long length = Long.parseLong(fileInfo[1]);
			ServerSocket socket = new ServerSocket(port);
			Socket client = socket.accept();
			Log.d("reciFile","通过了阻塞，即socket通信开始");
			File file = new File(Util.RECEIVE_DIR+File.separator+name);
			if(!file.getParentFile().exists())if(!file.getParentFile().mkdirs())return RECI_FAIL;
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			if(!Util.copyFile(br,bw))return RECI_FAIL;
			client.close();
			socket.close();
			if(file.exists())return RECI_OK;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			//获取文件信息失败
			e.printStackTrace();
		}
		return RECI_FAIL;
	}

	@Override
	public int connect(Drawable qrCode) {
		return CONNECT_FAIL;
	}

	/**
	 * 扫描端（发送文件）二维码端调用。
	 * @param content
	 * @return
	 */
	public int connect(final String content){
		String[] info = content.split(Util.SPLITER);
		String wifiName = info[0],psw=info[1];
		rmAddr = info[2];
		rmPort = Integer.parseInt(info[3]);
		wifiAdmin = new WifiAdmin(context) {
			@Override
			public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {
				context.registerReceiver(receiver,filter);
				return null;
			}

			@Override
			public void myUnregisterReceiver(BroadcastReceiver receiver) {
				context.unregisterReceiver(receiver);
			}

			@Override
			public void onNotifyWifiConnected() {
				Log.v("HotSpot", "have connected success!");
				Log.v("HotSpot", "###############################");
			}

			@Override
			public void onNotifyWifiConnectFailed() {
				Log.v("HotSpot", "have connected fail!");
				Log.v("HotSpot", "###############################");
			}
		};
		wifiAdmin.openWifi();
		WifiConfiguration conf = wifiAdmin.createWifiInfo(wifiName,psw,WifiAdmin.TYPE_WPA);
		wifiAdmin.addNetwork(conf);
		if(wifiAdmin.isWifiContected(context)==WifiAdmin.WIFI_CONNECTED)return CONNECT_OK;
		return CONNECT_FAIL;
	}

	/**
	 * 接收文件端调用
	 * @return 二维码
	 */
	@Override
	public Bitmap getQRCode() {
		WifiApAdmin wifiApAdmin = new WifiApAdmin(context);
		psw = Util.randPsw(10);
		wifiApAdmin.startWifiAp("\""+wifiName+"\"",psw);
		addr = NetworkDetect.getLocalIpAddress();
		String content = wifiName+Util.SPLITER+psw+Util.SPLITER+addr+Util.SPLITER+port;
		return QRcodeUtil.encode(content,300,300);
	}

}
