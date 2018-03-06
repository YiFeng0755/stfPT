package com.boyaa.stf.pt.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuixiangGui on 2016/9/13.
 */
public class MemoryInfo {

    /**
     *
     * @return [MemTotal, MemFree, Buffers, Cached]    unit:KB
     */
    public long[] getMemInfo(){
        long[] memInfo = new long[4];

        String memInfoPath = "/proc/meminfo";
        String readTemp = "";
        try {
            FileReader fr = new FileReader(memInfoPath);
            BufferedReader br = new BufferedReader(fr,8192);
            while((readTemp = br.readLine()) != null){
                if(readTemp.contains("MemTotal")){
                    memInfo[0] = getLongFromString(readTemp);
                }else if(readTemp.contains("MemFree")){
                    memInfo[1] = getLongFromString(readTemp);
                }else if(readTemp.contains("Buffers")){
                    memInfo[2] = getLongFromString(readTemp);
                }else if(readTemp.contains("Cached")){
                    memInfo[3] = getLongFromString(readTemp);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /*try {
            Class<?> proc = Class.forName("android.os.Process");
            Class<?> paramTypes[] = new Class[]{String.class, String[].class, long[].class};
            Method readProclines = proc.getMethod("readProclines", paramTypes);
            Object[] args = new Object[3];
            final String[] memInfoFields = new String[] {"MemTotal:",
                    "MemFree:", "Buffers:", "Cached:"};
            long[] memInfoSizes = new long[memInfoFields.length];
            memInfoSizes[0] = 30;
            memInfoSizes[1] = -30;
            args[0] = new String("/proc/meminfo");
            args[1] = memInfoFields;
            args[2] = memInfoSizes;
            if(null != readProclines){
                readProclines.invoke(null,args);
                for(int i=0;i<memInfoSizes.length;i++){
                    memInfo[i] = memInfoSizes[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return memInfo;
    }

    private long getLongFromString(String str){
        long mem = 0;
        String[] strArray = str.split(":");
        String[] memArray = strArray[1].trim().split(" ");
        mem = Long.parseLong(memArray[0].trim());
        return mem;
    }

    /**
     *
     * @return unit:KB
     */
    public long getTotalMem(){
        long[] memInfo = getMemInfo();
        return memInfo[0];
    }

    public long getFreeMem(){
        long[] memInfo = getMemInfo();
        return memInfo[1]+memInfo[2]+memInfo[3];
    }

    public int getPidPssMem(Context context, int pid){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] pids = new int[]{pid};
        Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(pids);
        return memInfo[0].getTotalPss();
    }

    /**
     *
     * @param context
     * @param pidList
     * @return pids memory [pidmem, pidmem,....,totalMem]
     */
    public List<Integer> getAllPidsPssMem(Context context, List<Integer> pidList){
        List<Integer> pidsPssMem = new ArrayList<Integer>();
        int allMemSum = 0;
        int[] pids = new int[pidList.size()];
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(int i = 0; i<pidList.size(); i++){
            pids[i] = pidList.get(i);
        }
        Debug.MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
        for(int i = 0; i<memoryInfos.length; i++){
            pidsPssMem.add(memoryInfos[i].getTotalPss());
            allMemSum = allMemSum + memoryInfos[i].getTotalPss();
        }
        pidsPssMem.add(allMemSum);
        return pidsPssMem;
    }
}
