package com.boyaa.stf.pt;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.boyaa.stf.pt.tools.AppInfo;


/**
 * Created by GuixiangGui on 2017/1/3.
 */

public class WaitAppStartThread implements Runnable {
    public static final long WAITTIME = 1000*60*2;

    private Intent intent = null;
    private MainActivity mainActivity = null;
    private AppInfo appInfo = new AppInfo();
    private String pkgName = "";


    public WaitAppStartThread(Intent intent, MainActivity mainActivity){
        this.intent = intent;
        this.mainActivity = mainActivity;
        pkgName = intent.getStringExtra("packageName");
    }

    @Override
    public void run() {
        waitForAppStart(pkgName);
        if(appInfo.isAppStart(pkgName)) {
            Log.i("stfRainbowPT", "pkg start");
            mainActivity.startService(intent);
        }else {
            Log.i("stfRainbowPT", "pkg not start stf rainbowPT finish");
        }

    }

    private void waitForAppStart(String pkgName){

        boolean isAppStart = false;
        Log.i("stfRainbowPT", "wait for app start pkgname:" + pkgName);
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis()<(startTime + WAITTIME)){
            isAppStart = appInfo.isAppStart(pkgName);
            if(isAppStart){
                break;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
