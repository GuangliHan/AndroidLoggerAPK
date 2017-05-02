package com.tendcloud.tdlogger;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by jrr on 2017/2/8.
 */

public class PlantformManager {
    private static Context mContext;
    private static PlantformManager sInstance;
    private static String LOG_PATH_MEMORY_DIR;
    private static String LOG_PATH_SDCARD_DIR;
    public static PlantformManager getInstance(Context context) {
        synchronized (PlantformManager.class) {
            if (sInstance == null) {
                sInstance = new PlantformManager();
                mContext = context;
                LOG_PATH_MEMORY_DIR = context.getFilesDir().getAbsolutePath() + File.separator
                        + "TDlog";
                LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + File.separator
                        + "TDlog";
            }
        }
        return sInstance;
    }

    private int getCurrLogType() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Const.MEMORY_TYPE;
        } else {
            return Const.SDCARD_TYPE;
        }
    }

    public String getLogPath() {
        String logPath = LOG_PATH_SDCARD_DIR;
        if(Const.MEMORY_TYPE == getCurrLogType()) {
            logPath = LOG_PATH_MEMORY_DIR;
        }
        return logPath;
    }
}
