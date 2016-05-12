package com.jcx;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.jcx.util.Util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by lenovo on 2016/5/2.
 */
public class UtilTest extends ApplicationTestCase<Application>{
    public UtilTest(){
        super(Application.class);
    }
    public void testInfo(){
        Runnable send = new Runnable() {
            @Override
            public void run() {
                Util.sendInfo("127.0.0.1", 8888, "yes");
            }
        };
        Runnable rev = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket socket = new ServerSocket(8888);
                    Util.receiveInfo(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(send).start();
        new Thread(rev).start();
    }
}
