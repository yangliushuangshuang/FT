package com.jcx.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by churongShaw on 2016/3/27.
 * done on 2016/3/27
 * @author churongShaw
 * @version 1.0
 * 提供基本的文件操作
 */
public class FileOper {
    /**
     * 删除文件或目录
     * @param path 要删除的文件的路径(绝对路径)
     */
    public boolean delete(String path){
        File file = new File(path);
        if (!file.exists()||file==null)
        {
            return false;
        }
        if(file.isDirectory()){//如果是目录，要递归删除。
            for(File son:file.listFiles()){//逐个删除目录下所有的文件（包括目录）
                this.delete(son.getAbsolutePath());
            }
        }
        //不是目录或者文件已经是空的就直接删除
        file.delete();
        return true;
    }

    /**
     * 新建文件，也可以直接调用File类实例对象的createFile方法（如要新建目录则是mkdir方法）
     * @param directory 新建文件所在的绝对目录
     * @param name 新建文件的名字
     * @param isDir true为新建目录，false为新建文件
     */
    public void touch(String directory,String name,boolean isDir){
        File file = new File(directory,name);
        Log.i("FileOper","创建文件开始");
        if(file.getParentFile().isDirectory()){
            if(!file.exists()){
                try {
                    if(isDir)file.mkdir();
                    else file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("FileOper","创建文件或目录失败");
                }
            }
        }
    }

    /**
     * 获得目录下所有文件<br/>
     * 该方法主要为了功能完备而实现。<br/>
     * 建议使用File类实例化对象的listFiles
     * @param directory File类，目录路径
     * @return 如果directory参数是一个目录，则返回目录下的文件File数组。
     */
    public File[] select(File directory){
        return directory.isDirectory()?directory.listFiles():null;
    }

    /**
     * 修改名字,//建议直接使用File实例化对象的方法renameTo
     * @param oldFile 旧文件或目录名
     * @param newFile 新文件或目录名
     */
    public void modify(String dir,String oldFile,String newFile){
        File file = new File(dir,oldFile);
        if(!file.exists())return;
        File filex=new File(dir,newFile);//File filex=new File(parentPaht+newName)
        file.renameTo(filex);
    }

    /**
     * 复制文件
     * @param srcPath 源文件或目录
     * @param destPath 目标文件或目录
     * @return true复制成功，false复制失败
     */
    public boolean copy(String srcPath,String destPath){
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        if(!destFile.isDirectory()){
            Log.d("copy file","destPath不是个目录");
            return false;
        }
        if(srcFile.isDirectory()){
            Log.i("copy file","源路径是目录，需要递归执行");
            File newDest = new File(destFile,srcFile.getName());
            newDest.mkdir();
            for(File file :srcFile.listFiles()){
                this.copy(file.getAbsolutePath(),newDest.getAbsolutePath());
            }
        }else{
            this.touch(destPath,new File(srcPath).getName(),false);
        }
        return true;
    }

    /**
     * 移动文件，先复制后删除。
     * @param srcPath
     * @param destPath
     * @return
     */
    public boolean move(String srcPath,String destPath){
        if(!new File(destPath).isDirectory())return false;
        if(this.copy(srcPath,destPath)){
            this.delete(srcPath);
        }
        Log.d("FileOper","复制不成功。");
        return true;
     }
}
