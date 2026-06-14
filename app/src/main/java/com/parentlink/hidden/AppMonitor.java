package com.parentlink.hidden;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import java.util.List;

public class AppMonitor {
    public static String getForegroundApp(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo process : processes) {
                    if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return process.processName;
                    }
                }
            }
        }
        return null;
    }
}