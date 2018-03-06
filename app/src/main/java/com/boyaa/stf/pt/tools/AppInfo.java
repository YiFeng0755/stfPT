package com.boyaa.stf.pt.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuixiangGui on 2016/9/18.
 */
public class AppInfo {

    /**
     * @param packageName
     * @return List index 0 is pidsList and index 1 is processesNameList
     */
    public List<List> getPidsAndProcessesNameByPackage(String packageName) {
        List<List> pidsAndProcessesName = new ArrayList<List>();
        List<Integer> appPidsList = new ArrayList<Integer>();
        List<String> appProcessesName = new ArrayList<String>();
        pidsAndProcessesName.add(appPidsList);
        pidsAndProcessesName.add(appProcessesName);
        List<String[]> appProcessList = getAppProcessInfoByPackage(packageName);
        for (int i = 0; i < appProcessList.size(); i++) {
            String[] array = appProcessList.get(i);
            appPidsList.add(Integer.parseInt(array[1]));
            appProcessesName.add(array[8]);
        }
        return pidsAndProcessesName;
    }

    public List<Integer> getPidsByPackage(String packageName) {
        List<Integer> appPidList = new ArrayList<Integer>();
        List<String[]> appProcessList = getAppProcessInfoByPackage(packageName);
        for (int i = 0; i < appProcessList.size(); i++) {
            String[] array = appProcessList.get(i);
            appPidList.add(Integer.parseInt(array[1]));
        }
        return appPidList;
    }

    public int getUidByPackageName(String packageName) {
        int appUidList = 0;
        List<String[]> appProcessList = getAppProcessInfoByPackage(packageName);
        if (appProcessList.size() > 0) {
            String[] array = appProcessList.get(0);
            appUidList = Integer.parseInt(array[0].substring(4)) + 10000;
        }
        return appUidList;
    }

    public List<String> getProcessesNameByPackage(String packageName) {
        List<String> appProcessNameList = new ArrayList<String>();
        List<String[]> appProcessList = getAppProcessInfoByPackage(packageName);
        for (int i = 0; i < appProcessList.size(); i++) {
            String[] array = appProcessList.get(i);
            appProcessNameList.add(array[8]);
        }
        return appProcessNameList;
    }

    public List<String[]> getAppProcessInfoByPackage(String packageName) {
        List<String[]> appProcessList = new ArrayList<String[]>();
        BufferedReader bd = null;
        try {
            ProcessBuilder execBuilder = new ProcessBuilder("sh", "-c", "ps");
            execBuilder.redirectErrorStream(true);
            Process exec = execBuilder.start();
            bd = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line = null;
            while ((line = bd.readLine()) != null) {
                Log.i("stfRainbowPT", "get app process info by package ps result: " + line);
                String[] array = line.split("\\s+");
                if (array[array.length-1].contains(packageName)) {
                    appProcessList.add(array);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bd != null) {
                try {
                    bd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return appProcessList;
    }

    public boolean isAppStart(String packageName) {
        BufferedReader bd = null;
        try {
            ProcessBuilder execBuilder = new ProcessBuilder("sh", "-c", "ps");
            execBuilder.redirectErrorStream(true);
            Process exec = execBuilder.start();
            bd = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line = null;
            while ((line = bd.readLine()) != null) {
                Log.i("stfRainbowPT", "ps cmd result:" + line);
                String[] array = line.split("\\s+");
                if (array[array.length-1].equals(packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bd != null) {
                try {
                    bd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public String getTopActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = am.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return (runningTaskInfos.get(0).topActivity).toString();
        } else {
            return null;
        }
    }
}
