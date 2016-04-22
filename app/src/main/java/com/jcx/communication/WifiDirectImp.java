package com.jcx.communication;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import com.jcx.bcreceiver.WifiDirectBroadcastReceiver;
import com.jcx.util.Configuration;
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
import java.util.ArrayList;
import java.util.List;

public class WifiDirectImp implements WifiDirect{
	private WifiP2pManager.Channel mChannel;
	private WifiP2pManager mManager;
	private	List peers = new ArrayList();
	private Activity activity;
	private WifiP2pManager.PeerListListener peerListListener;
	private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
	private WifiDirectBroadcastReceiver receiver;
	private WifiP2pInfo info;
	public WifiDirectImp(Activity activity){
		this.activity = activity;
		mManager=(WifiP2pManager)activity.getSystemService(Activity.WIFI_P2P_SERVICE);
		mChannel=mManager.initialize(activity,activity.getMainLooper(),null);

		//获取列表
		peerListListener = new WifiP2pManager.PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peerList) {
				peers.clear();
				peers.addAll(peerList.getDeviceList());
				//((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
				if(peers.isEmpty()){
					Log.d("WifiDirect connect","找不到设备");
				}
			}
		};
		//启动了发现对等设备的线程
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				//成功
			}

			@Override
			public void onFailure(int reason) {
				//失败
			}
		});
		connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo minfo) {
				info=minfo;
			}
		};
	}

	/**
	 * 判断此时设备能否发送文件，在WIFI DIRECT协议中服务端和客户端的区别，只有客户端才能发送文件，服务端只能接受文件<br/>
	 * 该方法应该在连接后调用才有意义。主要用于辨别设备是否为服务器。
	 * @return true 表示能够接收文件，反之则false
	 */
	public boolean canTrans(){
		if(info==null)return false;
		if(info.groupFormed){
			if(info.isGroupOwner)return false;
			else return true;
		}
		return false;
	}
	/**
	 * 在当前Activity中的onResume()生命周期中调用该方法。注册广播监听的接收器。
	 */
	public void registerWifiDirectReceiver(){
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		receiver = new WifiDirectBroadcastReceiver(mManager,mChannel,activity,peerListListener,connectionInfoListener);
		activity.registerReceiver(receiver, mFilter);
	}

	/**
	 * 在onPause()生命周期中进行注销
	 */
	public void unregister(){
		activity.unregisterReceiver(receiver);
	}

	/**
	 * 发送文件
	 * @param file
	 * @return
	 */
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		String content = file.getName()+Util.SPLITER+file.getTotalSpace();
		if(canTrans()){
			Socket socket = new Socket();
			String host = info.groupOwnerAddress.getHostAddress();
			int port = new Configuration().getP2PPort();
			if(!Util.sendInfo(host,port,content))return TRANS_FAIL;//发送文件信息失败
			try {
				socket.bind(null);
				socket.connect(new InetSocketAddress(host, port),Util.SOCKET_TIMEOUT);
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				if(Util.copyFile(br,bw))return TRANS_OK;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("transFile","传输出现I/O错误");
			}
		}
		return TRANS_FAIL;
	}

	@Override
	public int receiFile() {
		int port = new Configuration().getP2PPort();
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

	/**
	 * 发现设备，并进行连接
	 * @param qrCode 扫描得到的二维码，进行连接。
	 * @return CONNECT_OK 、CONNECT_FAIL
	 */
	@Override
	public int connect(Drawable qrCode) {
		String addr = QRcodeUtil.decode(((BitmapDrawable) qrCode).getBitmap());
		return connect(addr);
	}
	public int connect(String addr){
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				//成功
			}

			@Override
			public void onFailure(int reason) {
				//失败
			}
		});
		boolean notFound=true;
		for(Object c:peers){
			WifiP2pDevice device = (WifiP2pDevice)c;
			if(device.deviceAddress.equals(addr))notFound=false;
		}
		if(notFound)return CONNECT_FAIL;
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = addr;
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d("WIFIDirect connect", "连接成功");
			}

			@Override
			public void onFailure(int reason) {
				Log.d("WIFIDirect connect", "连接失败");
			}
		});
		return CONNECT_OK;
	}

	/**
	 * 由接收文件方调用
	 * @return 生成的二维码
	 */
	@Override
	public Bitmap getQRCode() {
		//开启WIFI
		WifiManager wifiManager =(WifiManager)activity.getSystemService(Activity.WIFI_SERVICE);
		int state = wifiManager.getWifiState();
		if(state!=WifiManager.WIFI_STATE_ENABLED&&state!=WifiManager.WIFI_STATE_ENABLING){
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String addr;
		if(wifiInfo==null)return null;
		//获得MAC地址并生成二维码
		addr = wifiInfo.getMacAddress().toLowerCase();
		return QRcodeUtil.encode(addr,300,300);
	}
	public void disconnect(){
		mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d("wifi direct disconnect","成功停止发现对等设备");
			}

			@Override
			public void onFailure(int reason) {
				Log.d("wifi direct disconnect","停止失败发现对等设备");
			}
		});
		mManager.cancelConnect(mChannel,null);
	}
}
