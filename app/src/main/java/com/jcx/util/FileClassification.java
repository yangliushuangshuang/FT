package com.jcx.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cui on 16-4-27.
 */
public class FileClassification {
    private static List<MyFile> apkFileList;
    private static List<MyFile> documentFileList;
    private static List<MyFile> picturesFileList;
    private static List<MyFile> vidosFileList;
    private static List<MyFile> musicFileList;
    private static List<MyFile> zipFileList;
    private static List<MyFile> offLineWebPageFileList;
    private static List<MyFile> othersFileList;

    private static String SD_CARD_STATE =null;
    private static File SD_CARD_PATH =null;
    public enum FileType{
        apk,
        document,
        picture,
        video,
        music,
        zip,
        offineWebPage,
        others
    }
    public class MyFile {
        private Drawable apk_icon;
        private String file_name;
        private String file_path;
        private FileType fileType;

        public Drawable getApk_icon() {
            return apk_icon;
        }

        public void setApk_icon(Drawable apk_icon) {
            this.apk_icon = apk_icon;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public String getFile_path() {
            return file_path;
        }

        public void setFile_path(String file_path) {
            this.file_path = file_path;
        }

        public FileType getFileType() {
            return fileType;
        }

        public void setFileType(FileType fileType) {
            this.fileType = fileType;
        }
    }

    private File[] filesList;

    private static Context this_context;

    private static FileClassification _fileClassification=null;

    public FileClassification(){
        SD_CARD_STATE =Environment.getExternalStorageState();
        SD_CARD_PATH =Environment.getExternalStorageDirectory();
        apkFileList=new ArrayList<MyFile>();
        documentFileList=new ArrayList<MyFile>();
        picturesFileList=new ArrayList<MyFile>();
        vidosFileList=new ArrayList<MyFile>();
        musicFileList=new ArrayList<MyFile>();
        zipFileList=new ArrayList<MyFile>();
        offLineWebPageFileList=new ArrayList<MyFile>();
        othersFileList=new ArrayList<MyFile>();
    }

    /**
     * 对文件进行过滤和排序
     * @return File[]数组listfiles，存储SDcard中的文件
     */
    private File[] getListfiles(File getFileLists){
        File file=getFileLists;
        filesList=file.listFiles(new FileFilter());
        filesList= FileUtil.sort(filesList);
        return filesList;
    }

    public static FileClassification getFileClassification(Context context){
        this_context=context;
        if (_fileClassification == null) {
            _fileClassification=new FileClassification();
        }
        return _fileClassification;
    }

    public void ScanSDCard(){
        if (SD_CARD_STATE.equals(Environment.MEDIA_MOUNTED)) {
            cleanList();//扫描要重新写入数据，所以先清空list集合
            File[] sdCard_filesList=getListfiles(Environment.getExternalStorageDirectory());
            for (File file : sdCard_filesList) {
                fileClassification(file);
            }
        }
    }

    public List<MyFile> getApkFileList() {
        if (apkFileList.size()==0) {
            ScanSDCard();
        }
        return apkFileList;
    }

    public List<MyFile> getDocumentFileList() {
        if (documentFileList.size()==0) {
            ScanSDCard();
        }
        return documentFileList;
    }

    public List<MyFile> getPicturesFileList() {
        if (picturesFileList.size()==0 ) {
            ScanSDCard();
        }
        return picturesFileList;
    }

    public List<MyFile> getVidosFileList() {
        if (vidosFileList.size()==0) {
            ScanSDCard();
        }
        return vidosFileList;
    }

    public List<MyFile> getMusicFileList() {
        if (musicFileList.size()==0) {
            ScanSDCard();
        }
        return musicFileList;
    }

    public List<MyFile> getZipFileList() {
        if (zipFileList.size()==0) {
            ScanSDCard();
        }
        return zipFileList;
    }

    public List<MyFile> getOffLineWebPageFileList() {
        if (offLineWebPageFileList.size()==0) {
            ScanSDCard();
        }
        return offLineWebPageFileList;
    }

    public List<MyFile> getOthersFileList() {
        if (othersFileList.size()==0) {
            ScanSDCard();
        }
        return othersFileList;
    }

    public void fileClassification(File file){

        if (file.isDirectory()) {
            File[] files = getListfiles(file);
            for (File file_str : files) {
                fileClassification(file_str);
            }
        }else{
            String name_s = file.getName();
            MyFile myFile = new MyFile();
            String file_name=file.getName().toLowerCase();
            String file_path=file.getAbsolutePath();

            myFile.setFile_path(file_path);
            myFile.setFile_name(name_s);
            // MimeTypeMap.getSingleton()
            if (file_name.endsWith(".apk")) {
                // System.out.println("----" + file.getAbsolutePath() + "" +
                // name_s);
                PackageManager pm = this_context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(file_path, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = packageInfo.applicationInfo;

                /**获取apk的图标 */
                appInfo.sourceDir = file_path;
                appInfo.publicSourceDir = file_path;
                Drawable apk_icon = appInfo.loadIcon(pm);
                myFile.setApk_icon(apk_icon);
                /** 得到包名 */
                String fileName = packageInfo.packageName;
                myFile.setFile_name(fileName);
                /** apk的绝对路劲 */
                myFile.setFileType(FileType.apk);

                apkFileList.add(myFile);
            }else if (file_name.endsWith(".txt")||file_name.endsWith(".doc")||file_name.endsWith(".docx")||file_name.endsWith(".xls")||file_name.endsWith(".xlsx")||file_name.endsWith(".pdf")){
                myFile.setFileType(FileType.document);
                documentFileList.add(myFile);
            }else if (file_name.endsWith(".jpg")||file_name.endsWith(".png")||file_name.endsWith(".jpeg")||file_name.endsWith(".bmp")){
                myFile.setFileType(FileType.picture);
                picturesFileList.add(myFile);
            }else if (file_name.endsWith(".mp4")||file_name.endsWith(".3gp")||file_name.endsWith(".wmv")||file_name.endsWith(".rm")||file_name.endsWith(".rmvb")||file_name.endsWith(".mov")||file_name.endsWith(".avi")) {
                myFile.setFileType(FileType.video);
                vidosFileList.add(myFile);
            }else if (file_name.endsWith(".mp3")||file_name.endsWith(".wav")) {
                myFile.setFileType(FileType.music);
                musicFileList.add(myFile);
            }else if (file_name.endsWith(".zip")||file_name.endsWith(".rar")) {
                myFile.setFileType(FileType.zip);
                zipFileList.add(myFile);
            }else if (file_name.endsWith(".html")||file_name.endsWith(".xml")) {
                myFile.setFileType(FileType.offineWebPage);
                offLineWebPageFileList.add(myFile);
            }else{
                myFile.setFileType(FileType.others);
                othersFileList.add(myFile);
            }
        }
    }

    public void cleanList(){
        if (!apkFileList.isEmpty()) {
            apkFileList.clear();
        }
        if (!documentFileList.isEmpty()) {
            documentFileList.clear();
        }
        if (!picturesFileList.isEmpty()) {
            picturesFileList.clear();
        }
        if (!vidosFileList.isEmpty()) {
            vidosFileList.clear();
        }
        if (!musicFileList.isEmpty()) {
            musicFileList.clear();
        }
        if (!zipFileList.isEmpty()) {
            zipFileList.clear();
        }
        if (!offLineWebPageFileList.isEmpty()) {
            offLineWebPageFileList.clear();
        }
        if (!othersFileList.isEmpty()) {
            othersFileList.clear();
        }
    }
}
