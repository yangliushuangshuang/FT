package com.jcx.intpack;
import android.graphics.drawable.Drawable;

import java.io.File;
/**
 * @author lenovo
 * @version 1.0
 * 无网络情况下使用蓝牙通信接口<br>
 * 在版本1。0中，类BlueToothImp实现了该接口。
 */
public interface BlueTooth extends TransBasic{	
	/**
	 * 打开蓝牙，并开始扫描设备。此时需要对方设备打开蓝牙，并提供地址和设备名称。（如需要口令则追加）
	 */
	public void ready();
	/**
	 * 连接设备。
	 * @param qrCode 扫描得到对方设备蓝牙地址和名称（和口令）的二维码，
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL
	 */
	public int connect(Drawable qrCode);
	/**
	 * 作为从设备（接收文件），调用该方法，并返回该设备的蓝牙地址和设备名称，还有口令（如需）。
	 * @return 返回设备的蓝牙地址和设备名称（和口令）的二维码，出错则返回null
	 */
	public Drawable getQRCode();
}
