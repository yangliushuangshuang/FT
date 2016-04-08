package com.jcx.communication;

import android.content.Context;

import com.jcx.util.NetworkDetect;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author lenovo
 * @version 1.0
 * 通过网络状态等信息来选择最佳传输方式。
 */
public class DecisionHelper {
    public final static int BLUETOOTH=1;
    public final static int WIFIDIRECT=2;
    public final static int INETUDP=3;
    public final static int HOTSPOT=4;
    public final static String REASON="reason";
    public final static String TRANS="trans";
    private final  String NET_TYPE_2G="2g";
    private final  String NET_TYPE_3G="3g";
    private final  String NET_TYPE_4G="4g";
    private final  String NET_TYPE_WIFI="wifi";
    private final  String NET_TYPE_NET="net";
    private final int MAX_2G_SIZE=500;
    private final int MAX_GPRS_SIZE=5*1024;
    private Context context;
    public DecisionHelper(Context context){
        this.context=context;
    }

    /**
     * 获得当前网络状态下，建议使用的传输方式。
     * @param size 文件的大小，单位是KB。
     * @return 返回HashTable,哈希表的KEY值分别是静态变量REASON和TRANS，hashTable.get(DecisionHelper.TRANS)获得的是一个List<Integer>实例。
     */
    public Hashtable bestTrans(int size){
        List<Integer> ways = new ArrayList<Integer>();
        String type = NetworkDetect.getCurrentNetType(context);
        String reason = "";
        switch (type){
            case NET_TYPE_2G:reason="当前网络2G";if(size<MAX_2G_SIZE){ways.add(INETUDP);reason+="文件较小，适合互联网传输";}else{ways.add(BLUETOOTH);ways.add(HOTSPOT);reason+="文件较大，不适合2G网络传输";}break;
            case NET_TYPE_3G:
                //fall through
            case NET_TYPE_4G:reason="当前网络3G/4G";if(size<MAX_GPRS_SIZE){ways.add(INETUDP);reason+="文件较小，适合互联网传输";}else{ways.add(BLUETOOTH);ways.add(HOTSPOT);reason+="文件较大，不适合使用GPRS流量传输";} break;
            // TODO 优化连接WIFI的情况。当WIFI网速有限，或者双方连接同一WIFI时。
            //优化连接WIFI的情况。当WIFI网速有限，或者双方连接同一WIFI时。
            case NET_TYPE_WIFI:reason="当前连接WIFI";ways.add(2);ways.add(3);break;
            default:reason="无网络";break;
        }
        Hashtable hashtable = new Hashtable();
        hashtable.put(REASON,reason);
        hashtable.put(TRANS,ways);
        return hashtable;
    }
}
