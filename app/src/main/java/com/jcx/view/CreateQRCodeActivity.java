package com.jcx.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.jcx.R;
import com.jcx.communication.BlueToothImp;

/**
 * 显示生成的二维码
 * 传入：Intent传入二维码bitmap图像
 */

public class CreateQRCodeActivity extends Activity{

	private ImageView qrcodeImg;
	private BlueToothImp blueToothImp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_qrcode);

		blueToothImp=new BlueToothImp(this);

		Bitmap qrCodeBitmap=blueToothImp.getQRCode();

		qrcodeImg=(ImageView) findViewById(R.id.my_qrcode);

		qrcodeImg.setImageBitmap(qrCodeBitmap);

	}
}
