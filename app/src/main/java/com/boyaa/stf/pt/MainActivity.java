package com.boyaa.stf.pt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        String pkg = i.getStringExtra("packageName");
        long delayTime = (long)i.getLongExtra("delayTime",1000);
        String resultPath = i.getExtras().getString("filePath");
        Intent intent = new Intent(this, StfPTService.class);
        intent.putExtra("packageName", pkg);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("filePath", resultPath);
        Log.i("stfRainbowPT", "package name: " + pkg);
        Log.i("stfRainbowPT", "start wait app start thread");
        new Thread(new WaitAppStartThread(intent, this)).start();
        finish();
    }
}
