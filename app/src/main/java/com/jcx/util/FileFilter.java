package com.jcx.util;

import java.io.File;

/**
 * Created by Cui on 16-4-1.
 * 文件筛选器，将第一个字符是“.”的系统文件给隐藏
 */
public class FileFilter implements java.io.FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (!pathname.getName().startsWith("."))
            return true;
        else
            return false;
    }
}
