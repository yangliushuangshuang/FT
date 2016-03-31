package com.jcx.communication;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * @author lenovo
 * @version 1.0
 * 无网络情况下，使用热点的方式进行通信。<br>
 * 在版本1.0中，类HotSpotImp会对该接口进行实现。
 */
public interface HotSpot extends TransBasic{
	
	/**
	 * 该方法由主设备调用，打开热点并获得热点标识和登录口令，生成二维码。
	 * @return 热点打开成功后，获得热点标识和登录口令生成二维码，热点打开失败或其他错误则返回null。
	 */
	public Bitmap getQRCode();
	
	/**
	 * connect方法由从设备（接收文件方）调用
	 * @param qrCode 扫描得到的二维码，信息包括热点标识和登录口令。
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL。
	 */
	public int connect(Drawable qrCode);
}
