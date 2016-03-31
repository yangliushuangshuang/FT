package com.jcx.communication;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.jcx.util.Configuration;
import com.jcx.util.QRcodeUtil;
import com.jcx.util.Util;

import java.io.File;

public class BlueToothImp implements BlueTooth {

	@Override
	public int transFile(File file) {
		// TODO Auto-generated method stub
		return 0;
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
	 * 作为主设备（发送文件），调用该方法，并返回该设备的蓝牙地址和设备名称，还有口令（如需）。
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
		String psw = Util.randPsw(6);
		contens.append(psw);
		new Configuration().insertBluePsw(psw);//将随机生成的密码（用于蓝牙连接的PIN）插入到配置文件中。
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
	 * 从设备扫描了主设备生成二维码后进行连接
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
		//TODO
		return CONNECT_FAIL;
	}

}
