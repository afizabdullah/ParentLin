package com.parentlink.hidden;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotHelper {
    
    public static String captureScreen(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            View rootView = new View(context);
            
            display.getRealSize(android.graphics.Point.class.newInstance());
            
            Bitmap bitmap = Bitmap.createBitmap(display.getWidth(), display.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            rootView.draw(canvas);
            
            String path = context.getFilesDir() + "/screenshot_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}