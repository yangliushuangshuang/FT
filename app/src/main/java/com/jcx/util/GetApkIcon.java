package com.jcx.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by Cui on 16-4-3.
 * 得到apk文件的图标
 */
public class GetApkIcon {

    public static Drawable loadApkIcon(Context context,String apkPath)
    {
        PackageManager pm=context.getPackageManager();
        PackageInfo packageInfo=pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (packageInfo!=null)
        {
            ApplicationInfo applicationInfo=packageInfo.applicationInfo;
            applicationInfo.publicSourceDir=apkPath;
            Drawable apkIcon=applicationInfo.loadIcon(pm);
            return apkIcon;
        }
        return null;
    }
}
