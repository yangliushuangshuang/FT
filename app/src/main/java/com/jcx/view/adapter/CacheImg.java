package com.jcx.view.adapter;

import android.graphics.Bitmap;

/**
 * Created by Cui on 16-4-3.
 * 缓存加载的图片
 */
public class CacheImg {
    private String path;
    private Bitmap bitmap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheImg cacheImg = (CacheImg) o;

        return !(path != null ? !path.equals(cacheImg.path) : cacheImg.path != null);

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
