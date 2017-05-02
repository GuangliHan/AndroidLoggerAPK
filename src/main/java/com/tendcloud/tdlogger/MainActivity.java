package com.tendcloud.tdlogger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements Chronometer.OnChronometerTickListener {
    private static final String TAG = "MainActivity";
    private Chronometer mTimer;
    private int mMiss = 0;
    private boolean mFlagClickButton = true;
    private Button mLogButton;
    private Button mLogClearButton;
    private TextView mlogPathText;
    private TextView mlogfileText;
    private SimpleDateFormat sdf;
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // 获得计时器对象
        mTimer = (Chronometer)this.findViewById(R.id.chronometer);
        mTimer.setOnChronometerTickListener(this);
        sdf = new SimpleDateFormat("HH:mm:ss");
        // 获得log button
        mLogButton = (Button) this.findViewById(R.id.button_log);
        mLogButton.setText("开始");
        mLogButton.setBackgroundColor(Color.parseColor("#00FF00"));
        // 获得log clear button
        mLogClearButton = (Button) this.findViewById(R.id.button_clear_log);
        mLogClearButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0) {
                Log.i(TAG, "onClick: mLogClearButton");
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("温馨提示");
                dialog.setMessage("是否要清除所有TDlog？");
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //// TODO: 2017/2/8 清除所有log
                        delete(new File(PlantformManager.getInstance(MainActivity.this).getLogPath()));
                    }
                });
                AlertDialog confirmDialog = dialog.create();
                confirmDialog.show();
            }
        });
        mLogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(mFlagClickButton){
                    mFlagClickButton = false;
                    mLogButton.setText("停止");
                    mLogButton.setBackgroundColor(Color.parseColor("#FF0000"));
                    mLogClearButton.setEnabled(false);
                    startLogService();
                }
                else {
                    mFlagClickButton =true;
                    mLogButton.setText("开始");
                    mLogButton.setBackgroundColor(Color.parseColor("#00FF00"));
                    mLogClearButton.setEnabled(true);
                    stopLogService();
                }
            }
        });
        mlogfileText = (TextView) this.findViewById(R.id.text_files);
        mlogfileText.setMovementMethod(ScrollingMovementMethod.getInstance());
        // 获得log path text
        mlogPathText = (TextView) this.findViewById(R.id.text_log_path);
        mlogPathText.setText("Log Path: " + PlantformManager.getInstance(this).getLogPath());
        checkPermission();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mlogfileText.setText(getLogFiles());
    }

    @Override
    public void onBackPressed() {
        //back键不finish activity
        Log.i(TAG, "onBackPressed: ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: " + requestCode);

    }

    // 将秒转化成：小时：分钟：秒
    private String FormatMiss(long miss) {
        miss = miss/1000;
        String hh=miss/3600>9?miss/3600+"":"0"+miss/3600;
        String mm=(miss % 3600)/60>9?(miss % 3600)/60+"":"0"+(miss % 3600)/60;
        String ss=(miss % 3600) % 60>9?(miss % 3600) % 60+"":"0"+(miss % 3600) % 60;
        return hh+":"+mm+":"+ss;
    }

    private  void checkPermission() {
        int REQUEST_CODE = 1;
        String[] REQUEST_PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_LOGS,
                Manifest.permission.WAKE_LOCK
        };
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if we have all permission
            boolean hasPermission = true;
            for (String s:REQUEST_PERMISSIONS) {
                int permission = ActivityCompat.checkSelfPermission(this, s);
                if (PackageManager.PERMISSION_DENIED == permission) {
                    Log.i(TAG, "checkPermission: " + s);
                    hasPermission = false;
                    break;
                }
            }
            if (!hasPermission) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        REQUEST_PERMISSIONS,
                        REQUEST_CODE
                );
            }
        }
    }

    private void startLogService() {
        //开始计时
        //		获取当前的时间
        //time = System.currentTimeMillis();
        time = SystemClock.elapsedRealtime();
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.start();
        Intent intent = new Intent(this,LogService.class);
        startService(intent);
        mlogfileText.setText(getLogFiles());
    }

    private void stopLogService() {
        mTimer.stop();
        mTimer.setBase(SystemClock.elapsedRealtime());
        Intent intent = new Intent(this,LogService.class);
        stopService(intent);
        mlogfileText.setText(getLogFiles());
    }
    private String getLogFiles() {
        String folder = PlantformManager.getInstance(this).getLogPath();
        StringBuffer filesName = new StringBuffer();
        File file = new File(folder);
        if(file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for(File f:childFiles) {
                filesName.append(f.getName()).append('\n');
            }
        }
        return filesName.toString();
    }
    private void delete(File file) {
        if(!file.exists()) {
            Toast.makeText(MainActivity.this, "没有TDlog文件可以删除",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        if(file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
            Toast.makeText(MainActivity.this, "TDlog删除完成",
                    Toast.LENGTH_LONG).show();
            mlogfileText.setText("");
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        //chronometer.setText(FormatMiss(mMiss));
        //long tp = System.currentTimeMillis();
        long tp = SystemClock.elapsedRealtime();
        chronometer.setText(FormatMiss(tp - time));
    }
}
