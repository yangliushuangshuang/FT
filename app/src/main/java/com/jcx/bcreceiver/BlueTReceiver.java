package com.jcx.bcreceiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jcx.util.ClsUtils;
import com.jcx.util.Configuration;
import com.jcx.util.Util;

/**
 * Created by churongShaw on 2016/3/31.
 * 接收蓝牙匹配请求的广播监听类，静态注册
 */
public class BlueTReceiver extends BroadcastReceiver {
    String strPsw;

    /**
     * 在动态注册接收器时，需要给出密码（PIN码），密码可以通过BlueToothImp的getPsw()方法获得。
     * @param strPsw
     */
    public BlueTReceiver(String strPsw){
        this.strPsw = strPsw;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                "android.bluetooth.device.action.PAIRING_REQUEST"))
        {
            BluetoothDevice btDevice = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // byte[] pinBytes = BluetoothDevice.convertPinToBytes("1234");
            // device.setPin(pinBytes);
            //Log.i("tag11111", "ddd");
            try
            {
                ClsUtils.setPin(btDevice.getClass(), btDevice, strPsw); // 手机和蓝牙采集器配对
                ClsUtils.createBond(btDevice.getClass(), btDevice);
                ClsUtils.cancelPairingUserInput(btDevice.getClass(), btDevice);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
