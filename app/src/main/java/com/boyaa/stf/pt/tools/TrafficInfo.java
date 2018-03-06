package com.boyaa.stf.pt.tools;

import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by GuixiangGui on 2016/9/13.
 */
public class TrafficInfo {
    private long iniSendTraffic;
    private long iniRcvTraffic;

    public void iniTrafficInfo(int uid){
        iniRcvTraffic = getUidRcvTraffic(uid);
        iniSendTraffic = getUidSendTraffic(uid);
    }

    /**
     *
     * @param uid
     * @return [totalTraffic, sendTraffic, rcvTraffic]
     */
    public long[] getUidTrafArray(int uid){
        long[] trafficArray = new long[3];
        long s = getUidSendTraffic(uid) - iniSendTraffic;
        long r = getUidRcvTraffic(uid) - iniRcvTraffic;
        trafficArray[0] = s+r;
        trafficArray[1] = s;
        trafficArray[2] = r;

        return trafficArray;
    }

    public long getUidSendTraffic(int uid) {
        String sendPath = "/proc/uid_stat/" + uid + "/tcp_snd";

        File f = new File(sendPath);
        if(f.exists()){
            String st ="0";
            try {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr,8192);
                st = br.readLine();
                if(br != null){
                    br.close();
                }
                if(st != null && Long.parseLong(st) > 0){
                    return Long.parseLong(st);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long s =  TrafficStats.getUidTxBytes(uid);
        if(s >= 0){
            return s;
        }
        return 0;
    }

    public long getUidRcvTraffic(int uid) {
        String rcvPath = "/proc/uid_stat/" + uid + "/tcp_rcv";

        File f = new File(rcvPath);
        if(f.exists()){
            String rcv ="0";
            try {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr,8192);
                rcv = br.readLine();
                if(br != null){
                    br.close();
                }
                if(rcv != null && Long.parseLong(rcv) > 0){
                    return Long.parseLong(rcv);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long s =  TrafficStats.getUidRxBytes(uid);
        if(s >= 0){
            return s;
        }
        return 0;
    }
}
