package com.boyaa.stf.pt.tools;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by GuixiangGui on 2016/9/14.
 */
public class CpuInfo {
    private double idleCpu;
    private double totalCpu;
    private double o_idleCpu;
    private double o_totalCpu;
    private double processCpu;
    private double o_processCpu;
    private int pid;

    public CpuInfo(int pid){
        this.pid = pid;
        readTotalCpuStat();
        readProcessCpuStat();
        if(totalCpu != -100){
            o_idleCpu = idleCpu;
            o_totalCpu = totalCpu;
        }
        if(processCpu != -100){
            o_processCpu = processCpu;
        }

    }

    /**
     *
     * @return [processCpuUsage, totalCpuUsage, processsCpuTimeSlic, totalCpuTimeSlic]
     */
    public String[] getCpuUsedInfo(){
        String[] result =new String[4];
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        readTotalCpuStat();
        readProcessCpuStat();

        if (processCpu != -100 && totalCpu != -100 && (totalCpu - o_totalCpu)>0){
            result[0] = format.format(100*((processCpu - o_processCpu) /(totalCpu - o_totalCpu)));
            result[1] = format.format(100*(((totalCpu - o_totalCpu) - (idleCpu - o_idleCpu))/(totalCpu - o_totalCpu)));
            result[2] = String.valueOf(processCpu - o_processCpu);
            result[3] = String.valueOf(totalCpu - o_totalCpu);
            o_totalCpu = totalCpu;
            o_idleCpu = idleCpu;
            o_processCpu = processCpu;
        }else {
            result[0] = String.valueOf(0);
            result[1] = String.valueOf(0);
            result[2] = String.valueOf(0);
            result[3] = String.valueOf(0);
        }

        return result;
    }

    public String getCpuName(){
        try {
            RandomAccessFile raf = new RandomAccessFile("/proc/cpuinfo", "r");
            if(Build.CPU_ABI.equals("x86")){
                String line;
                while(null != (line = raf.readLine())){
                    String[] values = line.split(":");
                    if(values[0].contains("model name")){
                        raf.close();
                        return values[1];
                    }
                }
            }else {
                String[] cpu = raf.readLine().split(":");
                raf.close();
                return cpu[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void readTotalCpuStat(){
        RandomAccessFile reader = null;
        File f = new File("/proc/stat");
        if(!f.exists() || !f.canRead()){
            idleCpu = -100;
            totalCpu = -100;
            return;
        }
        try {
            reader = new RandomAccessFile(f, "r");
            String line = reader.readLine();
            String[] toks = line.split("\\s+");
            idleCpu = Double.parseDouble(toks[4]);
            totalCpu = Double.parseDouble(toks[1]) + Double.parseDouble(toks[2]) + Double.parseDouble(toks[3]) + Double.parseDouble(toks[4])
                    + Double.parseDouble(toks[5]) + Double.parseDouble(toks[6]) + Double.parseDouble(toks[7]);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void readProcessCpuStat(){
        String cpuStatPath = "/proc/" + pid + "/stat";
        RandomAccessFile reader = null;
        File f = new File(cpuStatPath);
        if(!f.exists() || !f.canRead()){
            processCpu = -100;
            return;
        }
        try {
            reader = new RandomAccessFile(f, "r");
            String line = reader.readLine();
            String[] toks = line.split(" ");
            processCpu = Double.parseDouble(toks[13]) + Double.parseDouble(toks[14]) + Double.parseDouble(toks[15]) + Double.parseDouble(toks[16]);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
