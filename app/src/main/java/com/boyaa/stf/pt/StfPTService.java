package com.boyaa.stf.pt;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.boyaa.stf.pt.tools.AppInfo;
import com.boyaa.stf.pt.tools.CpuInfo;
import com.boyaa.stf.pt.tools.MemoryInfo;
import com.boyaa.stf.pt.tools.TrafficInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

public class StfPTService extends Service {
    public static final long WAITTIME = 1000 * 60 * 10;

    private BufferedWriter bw;
    private FileOutputStream out;
    private OutputStreamWriter osw;

    private String packageName;
    private MemoryInfo memoryInfo;
    private AppInfo appInfo;
    private List<CpuInfo> cpuInfoList = new ArrayList<CpuInfo>();
    private TrafficInfo trafficInfo;
    private List<Integer> pidList;
    private List<String> processNameList;
    private int uid;
    private long delayTime = 1000;
    private String resultPath;

    private DecimalFormat format;
    private Handler handler = new Handler();
    private boolean isServiceStop = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        System.out.println("StfPTService+++++++++++++++++++++");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("StfPTService+++++++++++++++++++++");

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivities(this, 0, new Intent[]{nfIntent}, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("STF PT")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("STF PT is running")
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(172, notification);

        registerBoradcastReceiver();

        packageName = intent.getExtras().getString("packageName");
        delayTime = intent.getLongExtra("delayTime", 1000);
        resultPath = intent.getExtras().getString("filePath");

        format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        memoryInfo = new MemoryInfo();
        trafficInfo = new TrafficInfo();
        appInfo = new AppInfo();
        List<List> pidsAndProcessesName = appInfo.getPidsAndProcessesNameByPackage(packageName);
        pidList = pidsAndProcessesName.get(0);
        processNameList = pidsAndProcessesName.get(1);
        if (pidList.size() > 0) {
            uid = appInfo.getUidByPackageName(packageName);
            trafficInfo.iniTrafficInfo(uid);
            for (int i = 0; i < pidList.size(); i++) {
                cpuInfoList.add(new CpuInfo(pidList.get(i)));
            }

            createResultCsv(resultPath);
            handler.postDelayed(task, delayTime);
        } else {
            Log.e("guixiang", "pids is null service stop");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        if (pidList.size() > 0) {
            isServiceStop = true;
            handler.removeCallbacks(task);
            closeOpenedStream();
        }
        unregisterReceiver(mBR);

        super.onDestroy();
        stopForeground(true);
    }

    private void closeOpenedStream() {
        try {
            if (bw != null) {
                bw.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createResultCsv(String fileName) {
        File resutlFile = new File(fileName);
        try {
            resutlFile.createNewFile();
            out = new FileOutputStream(resutlFile);
            osw = new OutputStreamWriter(out, "UTF-8");
            bw = new BufferedWriter(osw);

            long totalMemorySize = memoryInfo.getTotalMem();
            DecimalFormat df = new DecimalFormat("####.00");
            String totalMemory = df.format((double) totalMemorySize / 1024);

            StringBuffer pidInfo = new StringBuffer();
            StringBuffer memoryTitle = new StringBuffer();
            StringBuffer cpuTitle = new StringBuffer();
            for (int i = 0; i < pidList.size(); i++) {
                pidInfo.append(pidList.get(i) + ":" + processNameList.get(i) + "; ");
                memoryTitle.append(pidList.get(i) + " Used Memory PSS(MB),");
                cpuTitle.append(pidList.get(i) + " Used CPU(%),");
            }

            bw.write(getString(R.string.result_title) + "\r\n");
            bw.write(getString(R.string.process_package) + ": ," + packageName + "\r\n"
                    + getString(R.string.process_pid) + ": ," + pidInfo.toString() + "\r\n"
                    + getString(R.string.mem_size) + "： ," + totalMemory + "MB\r\n"
                    + getString(R.string.cpu_type) + ": ," + cpuInfoList.get(0).getCpuName() + "\r\n"
                    + getString(R.string.android_system_version) + ": ," + android.os.Build.VERSION.RELEASE + "\r\n"
                    + getString(R.string.mobile_type) + ": ," + android.os.Build.MODEL + "\r\n"
                    + "UID" + ": ," + uid + "\r\n");

            bw.write(getString(R.string.timestamp) + "," + getString(R.string.top_activity) + ","
                    + getString(R.string.used_mem_PSS) + "," + getString(R.string.used_mem_ratio) + "," + getString(R.string.mobile_free_mem) + "," + memoryTitle.toString()
                    + getString(R.string.traffic) + "," + getString(R.string.total_send_traffic) + "," + getString(R.string.total_receive_traffic) + ","
                    + getString(R.string.app_used_cpu_ratio) + "," + getString(R.string.total_used_cpu_ratio) + "," + cpuTitle.toString() + "APP Used CPU TimeSlic" + "\r\n");
//                    + getString(R.string.battery) + "," + getString(R.string.current) + "," + getString(R.string.temperature) + "," + getString(R.string.voltage) + "\r\n");
        } catch (IOException e) {
            Log.e("createResultCsv", e.toString());
            System.out.print(e);
        }

    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!isServiceStop) {
                dataRefresh();
                handler.postDelayed(this, delayTime);
            }

        }
    };

    private void dataRefresh() {
        try {
            String performanceData = getPerformanceData();
            if (performanceData != null && bw != null) {
                bw.write(performanceData + "\r\n");
                bw.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPerformanceData() {
        List<Integer> memoryList = new ArrayList();
        List<String> cpuDataList = new ArrayList<String>();
        long[] traffic = null;
        Context context = getBaseContext();
        StringBuffer performanceDate = new StringBuffer();

        performanceDate.append(getCurrentTime() + ",");
        performanceDate.append(appInfo.getTopActivity(getBaseContext()) + ",");

        memoryList = memoryInfo.getAllPidsPssMem(context, pidList);
        performanceDate.append(format.format((memoryList.get(memoryList.size() - 1)) / 1024.0) + ",");
        performanceDate.append(format.format(100 * (double) (memoryList.get(memoryList.size() - 1)) / memoryInfo.getTotalMem()) + ",");
        performanceDate.append(format.format(memoryInfo.getFreeMem() / 1024.0) + ",");
        for (int i = 0; i < memoryList.size() - 1; i++) {
            performanceDate.append(format.format(memoryList.get(i) / 1024.0) + ",");
        }

        traffic = trafficInfo.getUidTrafArray(uid);
        performanceDate.append(traffic[0] / 1024 + ",");
        performanceDate.append(traffic[1] / 1024 + ",");
        performanceDate.append(traffic[2] / 1024 + ",");

        cpuDataList = getCpuListInfo();
        for (int i = 0; i < cpuDataList.size(); i++) {
            performanceDate.append(cpuDataList.get(i) + ",");
        }

        return performanceDate.toString();
    }

    private String getCurrentTime() {
        String time;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (Build.MODEL.equals("sdk") || Build.MODEL.equals("google_sdk")) {
            time = sformat.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
        } else {
            time = sformat.format(cal.getTime().getTime());
        }
        return time;
    }

    private List<String> getCpuListInfo() {
        List<String> cpuListData = new ArrayList<String>();
        String totalCpuUsage = null;
        long processTotalTimeSlic = 0;
        double totalTimeSlic = 0;
        List<String> processCpuUsageList = new ArrayList<String>();
        for (int i = 0; i < cpuInfoList.size(); i++) {
            String[] arrayCpuInfo = cpuInfoList.get(i).getCpuUsedInfo();
            processCpuUsageList.add(arrayCpuInfo[0]);
            totalCpuUsage = arrayCpuInfo[1];
            processTotalTimeSlic += Long.parseLong(arrayCpuInfo[2].split("\\.")[0]);
            totalTimeSlic += Double.parseDouble(arrayCpuInfo[3]);
        }

        if (totalTimeSlic != 0) {
            cpuListData.add(format.format(100 * processTotalTimeSlic / (totalTimeSlic / cpuInfoList.size())));
        } else {
            cpuListData.add("0");
        }

        cpuListData.add(totalCpuUsage);
        for (int i = 0; i < processCpuUsageList.size(); i++) {
            cpuListData.add(processCpuUsageList.get(i));
        }
        cpuListData.add(String.valueOf(processTotalTimeSlic));

        return cpuListData;
    }

    private BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if ("com.boyaa.stf.pt.stopservice".equals(action)) {

                isServiceStop = true;
                stopSelf();

            }
        }
    };

    private void registerBoradcastReceiver() {
        IntentFilter myInterntFilter = new IntentFilter();
        myInterntFilter.addAction("com.boyaa.stf.pt.stopservice");
        registerReceiver(mBR, myInterntFilter);
    }

}
