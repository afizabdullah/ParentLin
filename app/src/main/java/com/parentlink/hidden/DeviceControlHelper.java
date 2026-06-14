package com.parentlink.hidden;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import java.lang.reflect.Method;

public class DeviceControlHelper {
    private Context context;
    private DevicePolicyManager devicePolicyManager;
    private PowerManager powerManager;
    
    public DeviceControlHelper(Context context) {
        this.context = context;
        this.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    
    public boolean shutdownDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                powerManager.shutdown(false, null, false);
                return true;
            } else {
                Class<?> serviceManager = Class.forName("android.os.ServiceManager");
                Method getService = serviceManager.getMethod("getService", String.class);
                Object powerService = getService.invoke(null, "power");
                Class<?> powerManagerClass = Class.forName("android.os.IPowerManager");
                Method shutdownMethod = powerManagerClass.getMethod("shutdown", boolean.class, boolean.class);
                shutdownMethod.invoke(powerService, false, true);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean rebootDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                powerManager.reboot("ParentLink");
                return true;
            } else {
                Class<?> serviceManager = Class.forName("android.os.ServiceManager");
                Method getService = serviceManager.getMethod("getService", String.class);
                Object powerService = getService.invoke(null, "power");
                Class<?> powerManagerClass = Class.forName("android.os.IPowerManager");
                Method rebootMethod = powerManagerClass.getMethod("reboot", boolean.class, String.class, boolean.class);
                rebootMethod.invoke(powerService, false, "ParentLink", false);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void hideAppCompletely() {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
            new ComponentName(context, MainActivity.class),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        );
        try {
            pm.setApplicationEnabledSetting(
                context.getPackageName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0
            );
        } catch (Exception ignored) {}
    }
    
    public void lockDevice() {
        if (devicePolicyManager.isAdminActive(new ComponentName(context, AdminReceiver.class))) {
            devicePolicyManager.lockNow();
        }
    }
}