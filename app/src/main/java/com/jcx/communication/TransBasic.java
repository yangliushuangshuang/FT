package com.jcx.communication;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.net.ServerSocket;

/**
 * @author lenovo
 * @version 1.0
 * 该接口无实现类，作为基本的连接接口，不同方式的接口继承该接口。<br>
 * 需要注意的是，传输文件的时候不要在主线程里做。在新的线程里进行传输文件的操作。
 */
public interface TransBasic {
	public final static int TRANS_OK=1;
	public final static int TRANS_FAIL=2;
	public final static int CONNECT_OK=3;
	public final static int CONNECT_FAIL=4;
	public final static int RECI_OK=5;
	public final static int RECI_FAIL=6;
	/**
	 * 传输文件。
	 * @param file 
	 * @return 传输成功则返回TRANS_OK,反之则 返回TRANS_FAIL。
	 */
	public int transFile(File file);

	/**
	 * 接收文件
	 * @return
	 */
	public int receiFile();
	/**
	 * 设备连接。
	 * @param qrCode 扫描得到的二维码，进行连接。 
	 * @return 连接成功则返回CONNECT_OK，反之则CONNECT_FAIL
	 */
	public int connect(Drawable qrCode);


	/**
	 * 断开连接后的善后
	 */
	public void disconnect();
}
