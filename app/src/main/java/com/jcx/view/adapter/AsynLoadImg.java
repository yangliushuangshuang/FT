package com.jcx.view.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.jcx.R;
import com.jcx.util.GetApkIcon;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Cui on 16-4-1.
 */
public class AsynLoadImg {
    private Context context;
    private Object lock=new Object();
    public static boolean isAllow=true;
    private ConcurrentLinkedDeque<CacheImg> cacheImgs;//缓存图片缩略图的队列
    private String key;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AsynLoadImg(Context context){
        this.context=context;
        cacheImgs=new ConcurrentLinkedDeque<CacheImg>();
    }
    /**
     *  子线程加载图片
     * 1.在滑动屏幕时让子线程暂停，屏幕上以默认图片显示
     * 2.在松开手指时，通知子线程开始加载图片，并通知主线程更新UI
     * @param imageView 要加载图片的imageView
     */
    public void loadImage(ImageView imageView,String key,String file_path){
        this.key=key;
        if (key.equals("pic")) {
            imageView.setImageResource(R.drawable.file_picture);//设置默认图标
        }else {
            imageView.setImageResource(R.drawable.file_application);//设置默认图标
        }
        for (CacheImg img:cacheImgs)
        {
            if (imageView.getTag().equals(img.getPath()))
            {
                Bitmap bitmap=img.getBitmap();
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        new LoadImageThread(imageView,key,file_path).start();
    }

    /**
     * 线程锁，当手指还在划屏幕时锁住线程
     */
    public void lock(){
        isAllow=false;
    }

    /**
     * 开线程锁，唤醒线程
     */
    public void unLock(){
        isAllow = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * 加载图片bitmap的子线程
     */
    class LoadImageThread extends Thread{
        private ImageView imageView;
        private String file_path;
        private String key;
        private CacheImg cacheImg;
        private Bitmap bitmap;
        private Drawable drawable;
        private Bitmap bitmapDrawable;
        public LoadImageThread(ImageView imageView,String key,String file_path)
        {
            this.imageView=imageView;
            this.file_path=file_path;
            this.key=key;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            if (!isAllow)
            {
                synchronized (lock){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (key.equals("apk")){
                drawable= GetApkIcon.loadApkIcon(context, file_path);
                bitmapDrawable=((BitmapDrawable)drawable).getBitmap();
                cacheImg = new CacheImg();
                cacheImg.setPath(file_path);
                cacheImg.setBitmap(bitmapDrawable);
            }
            else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                bitmap = BitmapFactory.decodeFile(file_path, options);
                //把bitmap和路径存入到内存
                cacheImg = new CacheImg();
                cacheImg.setPath(file_path);
                cacheImg.setBitmap(bitmap);
            }
                if (cacheImgs.size() > 50) {
                    cacheImgs.poll();//移除队列第一个缓存
                }
                cacheImgs.add(cacheImg);
            //加载图片完毕后通知主线程刷新UI
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    if (key.equals("apk")) {
                        imageView.setImageBitmap(bitmapDrawable);
                    }
                    else{
                        if (file_path.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }
            });
        }
    }

}
