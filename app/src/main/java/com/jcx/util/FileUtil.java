package com.jcx.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Cui on 16-4-1.
 * 文件排序
 */
public class FileUtil {

    private File[] filelist;

    public static File[] sort(File[] filelist){

        List<File> listfile=Arrays.asList(filelist);//数组转为集合
        Collections.sort(listfile, new FileComparater());
        File[] file_array=listfile.toArray(new File[listfile.size()]);

        return file_array;
    }
}
