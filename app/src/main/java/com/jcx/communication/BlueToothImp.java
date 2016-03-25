package com.jcx.communication;

import java.io.File;

import com.jcx.intpack.BlueTooth;
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
		// TODO Auto-generated method stub
		
	}
	/**
	 * 作为从设备（接收文件），调用该方法，并返回该设备的蓝牙地址和设备名称，还有口令（如需）。
	 * @return 返回设备的蓝牙地址和设备名称（和口令）的二维码，出错则返回null
	 */
	@Override
	public Drawable getQRCode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 连接设备。
	 * @param qrCode 扫描得到对方设备蓝牙地址和名称（和口令）的二维码，
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL
	 */
	@Override
	public int connect(Drawable qrCode) {
		// TODO Auto-generated method stub
		return 0;
	}

}
