package com.jcx.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.AlgorithmParameterGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lenovo
 * @version 1.0
 * 工具类，解析并查看、修改配置文档。<br/>
 * 在版本1.0中，配置有：服务器IP地址，端口号。
 */
//TODO 配置文件的加密
public class Configuration {
    private final static String NAME="ft.conf";
    private final static String NETWORK="[NETWORK_CONFIG]";
    private final static String UI="[UI_CONFIG]";
    private final static String OTHER="[OTHER_CONFIG]";
    private final static String END_SEG="------";
    private final static String IPADDRESS_KEY="IpAddress";
    private final static String P2P_PORT_KEY="P2P_Port";
    private final static String SERVER_PORT_KEY="Server_Port";
    private final static String BLUE_PSW_KEY="Bluetooth_Psw";
/*    private final static String IPV4_PATTERN="^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
            +"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            +"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            +"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
    private final static String PORT_PATTERN="(\\d+)";*/
    public Configuration(){
        File files = new File(Util.DATA_DIRECTORY);
        if(files.exists()&&!files.isDirectory()){
            files.delete();files.mkdir();
        }
        if(!files.exists())files.mkdir();
        File conf = new File(files,NAME);
        if(conf.isDirectory()){
            new FileOper().delete(conf.getAbsolutePath());
            init();
        }
        if(!conf.exists())
            init();
    }
    //返回配置段的在文件中的偏移量
    private long locate(String segment) throws IOException {
        RandomAccessFile file=new RandomAccessFile(Util.DATA_DIRECTORY+File.separator+NAME,"r");
        String aLine=file.readLine();
        while(aLine!=null&&!aLine.equals(segment))aLine=file.readLine();
        long res= file.getFilePointer();
        file.close();
        return res;
    }
    private String findConfig(String key,RandomAccessFile file) throws IOException {
        String line=file.readLine();
        do{
            //Pattern pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
            /*Pattern pattern = Pattern.compile(strPattern);*/
            if(line!=null){
                String[] tmps = line.split("=");
                String currentKey = tmps[0];
                String currentValue = tmps[1];
                if(currentKey.equals(key)){
                    return currentValue;
                }
                line=file.readLine();
            }
        }while (line!=null&&!line.equals(END_SEG));
        return null;
    }
    private String getValue(String segment,String key){
        String res=null;
        try {
            Log.i("getValue", "开始");
            RandomAccessFile file = new RandomAccessFile(Util.DATA_DIRECTORY+File.separator+NAME,"rw");
            file.seek(locate(segment));//配置段偏移量
            res=findConfig(key, file);//在配置段下根据key找到对应value
            file.close();
            Log.i("getValue","结束");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v("配置文件","打开配置文件失败");
        }catch (IOException e){
            e.printStackTrace();
            Log.v("配置文件","定位配置段出错");
        }
        return res;
    }
    private void init(){
        try {
            new File(Util.DATA_DIRECTORY, NAME).createNewFile();
        } catch (IOException e) {
            Log.d("配置初始化","创建配置文件失败");
            e.printStackTrace();
        }
        //TODO 设置默认的服务器IP地址和端口号
        String defaultServerIP="127.0.0.1";//127.0.0.1 for test
        String defaultP2PPort="9888";//端到端传输
        String defaultServerPort="9888";
        List<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String,String> hashMap = new HashMap<String ,String>();
        hashMap.put("key", IPADDRESS_KEY);
        hashMap.put("value", defaultServerIP);
        list.add(hashMap);
        HashMap<String, String> hashMap1 = new HashMap<String,String>();
        hashMap1.put("key", P2P_PORT_KEY);
        hashMap1.put("value", defaultP2PPort);
        list.add(hashMap1);
        HashMap<String,String> hashMap2 = new HashMap<String,String>();
        hashMap2.put("key",SERVER_PORT_KEY);
        hashMap2.put("value",defaultServerPort);
        list.add(hashMap2);
        insertSeg(NETWORK, list);
    }
    public void insertBluePsw(String value){
        List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("key",BLUE_PSW_KEY);
        hashMap.put("value",value);
        list.add(hashMap);
        insertSeg(OTHER,list);
    }
    private void insertSeg(String segment,List<HashMap<String,String>> list){
        //配置文件插入配置段
        File conf = new File(Util.DATA_DIRECTORY,NAME);
/*        try {
            conf.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("配置文件", "生成配置文件失败");
        }*/
        BufferedWriter writer=null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(conf);
            writer = new BufferedWriter(fw);
            writer.write(segment);
            writer.newLine();
            for(HashMap<String,String> hashMap:list){
                writer.write(hashMap.get("key")+"=");
                writer.write(hashMap.get("value"));
                writer.newLine();
            }
            writer.write(END_SEG);
            writer.newLine();
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("配置文件","打开配置文件失败");
        }catch (IOException e){
            e.printStackTrace();
            Log.d("配置文件","写入失败");
        }finally {
            try {
                if(writer!=null)writer.close();
                if(fw!=null)fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    /**
     * 获得配置文件中服务器IP地址
     * @return 返回IP地址
     */
    public String getIpAddress(){
        return getValue(NETWORK, IPADDRESS_KEY).trim();
    }

    /**
     * 获得端到端通信端口
     * @return 返回端口号
     */
    public int getP2PPort(){
        return Integer.parseInt(getValue(NETWORK, P2P_PORT_KEY).trim());
    }

    /**
     * 获得与服务器通信端口号
     * @return 返回与服务器相连端口号
     */
    public int getServerPort(){
        return Integer.parseInt(getValue(NETWORK,SERVER_PORT_KEY).trim());
    }
    /*public String getBluePsw(){
        return getValue(OTHER,BLUE_PSW_KEY).trim();
    }*/
}
