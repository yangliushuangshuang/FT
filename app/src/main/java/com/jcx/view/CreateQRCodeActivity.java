package com.jcx.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.jcx.R;
import com.jcx.communication.BlueToothImp;
import com.jcx.communication.HotSpotImp;
import com.jcx.communication.InetUDPImp;
import com.jcx.communication.TransBasic;
import com.jcx.communication.WifiDirectImp;

/**
 * 显示生成的二维码
 * 传入：Intent传入二维码bitmap图像
 */

public class CreateQRCodeActivity extends Activity{

	private ImageView qrcodeImg;
	private Bitmap qrCodeBitmap;
	private BlueToothImp blueToothImp;
	private HotSpotImp hotSpotImp;
	private InetUDPImp inetUDPImp;
	private WifiDirectImp wifiDirectImp;
	private String flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_qrcode);
		qrcodeImg=(ImageView) findViewById(R.id.my_qrcode);

		Intent intent=this.getIntent();
		flag=intent.getStringExtra("flag");
		if (flag.equals("BTR")) {
			createBlueToothQRCode();
		}else if (flag.equals("HSR")) {
			createHotSpotQRCode();
		}else if(flag.equals("UDP"))
		{
			creatInetUDPQRCode();
		}else if(flag.equals("WFD")){
			createWiFiDirQRCode();
		}
	}

	public void createBlueToothQRCode(){
		blueToothImp=new BlueToothImp(this);
		qrCodeBitmap=blueToothImp.getQRCode();
		if (qrCodeBitmap != null) {
			qrcodeImg.setImageBitmap(qrCodeBitmap);
			Intent intent = new Intent();
			intent.putExtra("action", "BTR");
			setResult(1, intent);
		}
	}
	public void createHotSpotQRCode(){
		hotSpotImp =new HotSpotImp(this);
		qrCodeBitmap=hotSpotImp.getQRCode();
		if (qrCodeBitmap != null) {
			qrcodeImg.setImageBitmap(qrCodeBitmap);
			Intent intent = new Intent();
			intent.putExtra("action", "HSR");
			setResult(2, intent);
		}

	}
	public void creatInetUDPQRCode(){
		inetUDPImp=new InetUDPImp("127.0.0.1");
		qrCodeBitmap=inetUDPImp.getQRCode();
		if (qrCodeBitmap != null) {
			qrcodeImg.setImageBitmap(qrCodeBitmap);
			Intent intent = new Intent();
			intent.putExtra("action", "UDP");
			setResult(3, intent);
		}
	}
	public void createWiFiDirQRCode(){
		wifiDirectImp=new WifiDirectImp(this);
		qrCodeBitmap=wifiDirectImp.getQRCode();
		if (qrCodeBitmap != null) {
			qrcodeImg.setImageBitmap(qrCodeBitmap);
			Intent intent=new Intent();
			intent.putExtra("action","WFD");
			setResult(4,intent);
		}
	}
}
