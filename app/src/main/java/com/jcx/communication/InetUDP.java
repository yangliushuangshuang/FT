package com.jcx.communication;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * @author churongShaw
 * @version 1.0
 * 有网络情况下，使用UDP协议传输。<br>
 * 在该版本1.0中，会对该接口进行基本的实现，实现类命名为InetUDPImp。
 */
public interface InetUDP  extends TransBasic{
	
	/**
	 * 获得本机的互联网IP（外网IP）。
	 * @return 返回IP所对应的二维码，如果无互联网IP地址则返回空.
	 */
	public Bitmap getQRCode();
	/**
	 * 扫描二维码，得到IP，并发送握手确认。
	 * @param qrCode 扫描得到的对方设备的IP二维码。
	 * @return 连接是否成功，成功则CONNECT_OK,反之则CONNECT_FAIL。
	 */
	public int connect(Drawable qrCode);
	
}
