package com.jcx.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.widget.Toast;

import com.jcx.hotspot.WifiManageUtils;
import com.jcx.util.Configuration;
import com.jcx.util.NetworkDetect;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;
import com.jcx.view.AllFilesActivity;
import com.jcx.view.MainActivity;

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
	private String rmAddr;
	private String addr;
	private int port;
	private int rmPort;
	private WifiManageUtils wifiManageUtils;
	private String fileName;
	private long length;
	public HotSpotImp(Activity context){
		this.context = context;
		wifiManageUtils = new WifiManageUtils(context);
		wifiName = "TP";
		port = new Configuration().getP2PPort();
	}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		length = file.getTotalSpace();
		if(!Util.sendInfo(rmAddr,rmPort,file.getName()+Util.SPLITER+length))return TRANS_FAIL;
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(rmAddr,rmPort),Util.SOCKET_TIMEOUT);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			if(Util.copyFile(br,bw,false))return TRANS_OK;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return TRANS_FAIL;
	}

	@Override
	public int receiFile() {
		try {
			String[] fileInfo = Util.receiveInfo(port).split(Util.SPLITER);
			fileName = fileInfo[0];
			//TODO
			length = Long.parseLong(fileInfo[1]);
			Toast.makeText(context,String.valueOf(length),Toast.LENGTH_SHORT).show();
			ServerSocket socket = new ServerSocket(port);
			Socket client = socket.accept();
			Log.d("reciFile","通过了阻塞，即socket通信开始");
			File file = new File(Util.RECEIVE_DIR+File.separator+fileName);
			if(!file.getParentFile().exists())if(!file.getParentFile().mkdirs())return RECI_FAIL;
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Util.copyFile(br,bw,true);
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
		//rmAddr = info[2];
		rmPort = Integer.parseInt(info[3]);
		wifiManageUtils.closeWifi();
/*		try{
			Thread.currentThread();
			Thread.sleep(2000);
		}catch (InterruptedException e){
			e.printStackTrace();
		}*/
		wifiManageUtils.openWifi();
		wifiManageUtils.startscan();
		WifiConfiguration netConfig = wifiManageUtils.getCustomeWifiClientConfiguration(wifiName, psw, 3);
		while(wifiManageUtils.isConnected(wifiName)){
			if(!wifiManageUtils.addNetwork(netConfig)){
				Log.e("hotspot","add network fail");
				return CONNECT_FAIL;
			}
		}
		boolean iptoready =false;
		while (!iptoready)
		{
			wifiManageUtils.startscan();
			try
			{
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			}
			catch (InterruptedException ie)
			{
			}

			DhcpInfo dhcp = new WifiManageUtils(context).getDhcpInfo();
			int ipInt = dhcp.gateway;
			if (ipInt != 0)
			{
				rmAddr = Util.intToIp(ipInt);
				iptoready = true;
			}
		}
		return CONNECT_OK;
	}

	/**
	 * 接收文件端调用
	 * @return 二维码
	 */
	@Override
	public Bitmap getQRCode() {
		WifiManageUtils wifiManageUtils = new WifiManageUtils(context);
		//psw = Util.randPsw(10);
		psw="123456789";
		wifiManageUtils.closeWifi();
		wifiManageUtils.stratWifiAp(wifiName, psw,3);
		addr = NetworkDetect.getLocalIpAddress();
		String content = wifiName+Util.SPLITER+psw+Util.SPLITER+addr+Util.SPLITER+port;
		System.out.println("hotSpotImg -------->psw:"+psw);
		return QRcodeUtil.encode(content,300,300);
	}

	@Override
	public void disconnect() {
		if(wifiManageUtils!=null){
			wifiManageUtils.closeWifiAp();
			wifiManageUtils.closeWifi();
		}
	}
	public String getFileName(){
		return fileName;
	}
	public long getlength(){
		return length;
	}
}
