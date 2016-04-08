package com.jcx.util;

import java.io.File;
import java.util.Comparator;

/**
 * Created by Cui on 16-4-1.
 * 文件比较，按首字母顺序排序
 */
public class FileComparater implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
        if (f1.isDirectory()&&f2.isDirectory())
        {
            return f1.getName().compareToIgnoreCase(f2.getName());
        }else if (f1.isDirectory()&&f2.isFile())
        {
            return -1;
        }else if (f1.isFile()&&f2.isDirectory())
        {
            return 1;
        }else{
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }
}
