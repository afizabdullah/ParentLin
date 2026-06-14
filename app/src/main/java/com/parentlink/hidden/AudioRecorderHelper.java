package com.parentlink.hidden;

import android.media.MediaRecorder;
import android.content.Context;
import java.io.IOException;

public class AudioRecorderHelper {
    private static MediaRecorder mediaRecorder;
    private static String outputPath;
    
    public static void startRecording(Context context) {
        outputPath = context.getFilesDir() + "/audio_" + System.currentTimeMillis() + ".3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(outputPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
            return outputPath;
        }
        return null;
    }
}