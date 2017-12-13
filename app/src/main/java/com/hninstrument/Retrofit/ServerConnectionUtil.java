package com.hninstrument.Retrofit;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Tools.SafeCheck;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.DSAKey;
import java.util.UUID;

/**
 * Created by zbsz on 2017/12/2.
 */

public class ServerConnectionUtil {
    public static final String APK_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk";
    private static int TIME_OUT = 20 * 1000;   //超时时间
    private static String CHARSET = "utf-8";
    private BufferedReader in = null;
    private static String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
    private static String CONTENT_TYPE = "multipart/form-data";
    private static int bufferSize = 2048;
    Handler handler = new Handler();

    public void post(final String baseUrl, final byte[] bs, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                final String response = sendPost(baseUrl, bs);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }.start();
    }

    public void download(final String baseUrl, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                final String response =sendDownload(baseUrl);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }.start();
    }

    public void post(final String baseUrl, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                final String response = sendPost(baseUrl);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }.start();
    }


    private String sendDownload(String baseUrl) {
        String result = null;
        OutputStream os= null;
        File file = new File(APK_PATH);
        if (file.exists()) {
            file.delete();
        }
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (200 == conn.getResponseCode()) {
                byte[] bs = new byte[bufferSize];
                int len = -1;
                os = new FileOutputStream(APK_PATH);
                while ((len = conn.getInputStream().read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                if(file.length()>100){
                    result = "true";
                }else{
                    result = null;
                }
            }else{
                result = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                os.flush();
                os.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    private String sendPost(String baseUrl) {
        String result = null;
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (200 == conn.getResponseCode()) {
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result = line;
                }
            } else {
                result = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String sendPost(String baseUrl, byte[] bs) {
        String result = null;
        DataOutputStream ds = null;
        ByteArrayInputStream bin = null;
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            ds = new DataOutputStream(conn.getOutputStream());
            bin = new ByteArrayInputStream(bs);
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            while ((length = bin.read(buffer)) != -1) {
                ds.write(buffer, 0, length);
            }
            if (200 == conn.getResponseCode()) {
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result = line;
                }
            } else {
                result = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ds != null) {
                    ds.flush();
                    ds.close();
                }
                if (bin != null) {
                    bin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    public interface Callback {
        void onResponse(String response);
    }


}
