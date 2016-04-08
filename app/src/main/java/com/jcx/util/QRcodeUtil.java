package com.jcx.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
/**
 * Created by lenovo on 2016/3/27.
 * @author churongShaw
 * @version 1.0
 * 图形码（二维码）的编码与解码
 */
public class QRcodeUtil {
    private final static String CHARSET="utf-8";
    private final static String FORMAT="PNG";
    /**
     * 编码
     * @param contents 转化为二维码的内容。
     * @param width 生成二维码的宽度
     * @param height 生成二维码的高度
     * @return 二维码图片的bitmap
     */
    public static Bitmap encode(String contents,int width,int height){
        if(contents==null||contents.equals(""))return null;
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET,CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        int pixel[] = new int[width*height];
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE,width,height,hints);
            for(int y=0;y<height;y++)
                for(int x=0;x<width;x++)
                    pixel[y*width+x] = bitMatrix.get(x,y)? 0xff000000:0xffffffff;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("QRCode","encoding failure");
        }
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixel,0,width,0,0,width,height);
        return bitmap;
    }
    /**
     * 解码
     * @param bitmap 二维码图片的位图
     * @return 解析二维码的字符串结果
     */
    public static String decode(Bitmap bitmap){

//        try {
//
//            int pixel[]=new int[bitmap.getHeight()*bitmap.getWidth()];
//            bitmap.getPixels(pixel,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
//            LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(),bitmap.getHeight(),pixel);
//            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
//            Result result;
//            Hashtable hints = new Hashtable();
//            hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
//            Reader reader = new QRCodeReader();
//            result = reader.decode(binaryBitmap,hints);
//            //result = new MultiFormatReader().decode(binaryBitmap,hints);
//            return result.getText();
//        }catch (NotFoundException e){
//            e.printStackTrace();
//            Log.d("二维码解析","notfound");
//        }catch (FormatException e){
//            e.printStackTrace();
//            Log.d("二维码解析","format");
//        }catch (ChecksumException e){
//            e.printStackTrace();
//            Log.d("二维码解析","checksum");
//        }
        return "";
    }
    public static String decode(String imagPath){
        Bitmap bitmap = BitmapFactory.decodeFile(imagPath);
        return decode(bitmap);
    }
}
