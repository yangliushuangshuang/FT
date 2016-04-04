package com.jcx.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jcx.util.ClsUtils;
import com.jcx.util.Configuration;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class BlueToothImp implements BlueTooth {
	private BluetoothDevice remote;
	public final static String CONNECT_UUID = "00001101-0000-1000-8000-00805F9B34FB";//串口服务
	private String psw;

	public String getPsw(){
		return psw;
	}
	@Override
	public int transFile(File file) {
		if(!file.exists()||file.isDirectory())return TRANS_FAIL;
		UUID uuid = UUID.fromString(CONNECT_UUID);
		try {
			Log.i("蓝牙传输", "开始");
			BluetoothSocket bluetoothSocket = remote.createRfcommSocketToServiceRecord(uuid);
			bluetoothSocket.connect();//Socket连接
			OutputStream outputStream = bluetoothSocket.getOutputStream();
			//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream,"utf-8"));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			if(!Util.copyFile(br,bw))return TRANS_FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("蓝牙传输", "获取socket失败");
			return TRANS_FAIL;
		}
		return TRANS_OK;
	}

	@Override
	public int receiFile() {
		UUID uuid = UUID.fromString(CONNECT_UUID);
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		try {
			Log.i("接收文件", "开始");
			BluetoothServerSocket bluetoothServerSocket = bta.listenUsingRfcommWithServiceRecord("BluetoothServer",uuid);
			BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();//等待"客户端"连接
			Log.d("reciFile","通过了阻塞，即socket通信开始");
			if(bluetoothSocket!=null){
				InputStream inputStream = bluetoothSocket.getInputStream();
				//BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
				//TODO 文件名和路径
				File dir = new File(Util.RECEIVE_DIR);
				if(!dir.exists())dir.mkdirs();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Util.RECEIVE_DIR+File.separator+System.currentTimeMillis())));
				if(!Util.copyFile(br,bw))return RECI_FAIL;
			}
			if(bluetoothSocket!=null)bluetoothSocket.close();
			bluetoothServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("接收文件", "I/O错误");
			return RECI_FAIL;
		}
		return RECI_OK;
	}

	/**
	 * 打开蓝牙，并开始扫描设备。此时需要对方设备打开蓝牙，并提供地址和设备名称。（如需要口令则追加）
	 */
	@Override
	public void ready() {
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		bta.cancelDiscovery();
		if(bta.isEnabled())bta.enable();
	}
	/**
	 * 作为从设备（接收文件），调用该方法，并返回该设备的蓝牙地址和设备名称，还有口令（如需）。
	 * @return 返回设备的蓝牙地址和设备名称（和口令）的二维码，出错则返回null
	 */
	@Override
	public Bitmap getQRCode() {
		StringBuilder contens = new StringBuilder();
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		bta.cancelDiscovery();
		if(bta.isEnabled())bta.enable();
		contens.append(bta.getAddress());
		contens.append(Util.SPLITER);
		psw = Util.randPsw(6);
		contens.append(psw);
		//new Configuration().insertBluePsw(psw);//将随机生成的密码（用于蓝牙连接的PIN）插入到配置文件中。
		//TODO 高度和宽度应该优化。
		return QRcodeUtil.encode(contens.toString(), 300, 300);

	}
	
	/**
	 * 连接设备。
	 * @param qrCode 扫描得到对方设备蓝牙地址和名称（和口令）的二维码，
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL
	 */
	@Override
	public int connect(Drawable qrCode) {
		Bitmap bitmap = ((BitmapDrawable)qrCode).getBitmap();
		String temp = QRcodeUtil.decode(bitmap);
		String[] list = temp.split(Util.SPLITER);
		String address = list[0],psw=list[1];
		return connect(address,psw);
	}

	/**
	 * 主设备扫描了主设备生成二维码后进行连接
	 * @param imagPath 二维码路径
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL
	 */
	public int connect(String imagPath){
		String temp = QRcodeUtil.decode(imagPath);
		String[] list = temp.split(Util.SPLITER);
		String address = list[0],psw=list[1];
		return connect(address,psw);
	}

	/**
	 * 从设备扫描了主设备生成二维码后进行连接
	 * @param addr 扫描二维码得到的设备地址
	 * @param psw 扫描二维码得到的密码
	 * @return 返回结果码
	 */
	public int connect(String addr,String psw){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		ready();
		if(!BluetoothAdapter.checkBluetoothAddress(addr)){
			Log.i("蓝牙","找不到指定地址的蓝牙设备");
			return CONNECT_FAIL;
		}
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(addr);
		if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
			Log.i("从设备蓝牙配对","未连接状态，创建连接开始");
			try {
				ClsUtils.setPin(device.getClass(), device, psw);
				ClsUtils.createBond(device.getClass(), device);
				ClsUtils.cancelPairingUserInput(device.getClass(), device);
				remote=device;
				Log.i("从设备蓝牙配对","未连接状态，创建匹配成功结束");
				return CONNECT_OK;
			} catch (Exception e) {
				Log.d("从设备蓝牙配对","创建匹配失败");
				e.printStackTrace();
				return CONNECT_FAIL;
			}
		}else {
			Log.i("从设备蓝牙匹配", "已匹配");
			try {
				ClsUtils.createBond(device.getClass(), device);
				ClsUtils.setPin(device.getClass(), device, psw);
				ClsUtils.createBond(device.getClass(), device);
				ClsUtils.cancelPairingUserInput(device.getClass(), device);
				remote=device;
				return CONNECT_OK;
			} catch (Exception e) {
				Log.d("has bond","创建匹配失败");
				e.printStackTrace();
				return CONNECT_FAIL;
			}
		}
	}

}
