package com.hninstrument.Tools;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;


import com.hninstrument.AppInit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MediaHelper {
    public enum Text {
        first_opt, second_opt, relock_opt, err_connect, err_upload, err_samePerson, msg_patrol,
        msg_visit, err_omtk, wait_reupload, waiting, second_err, alarm ,err_connect_ns,no_registration,err_connect_relock,
    }

    private static MediaPlayer mediaPlayer;

    public static void mediaOpen() {
        mediaPlayer = new MediaPlayer();
        AudioManager audioMgr = (AudioManager) AppInit.getContext().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
                AudioManager.FLAG_PLAY_SOUND);
        Log.e("信息提示","打开音量");
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(AppInit.getContext(), null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaPlayer, subtitleInstance, null);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void play(Text text) {
        if(AppInit.getInstrumentConfig().noise()){
            try {
                AssetFileDescriptor fileDescriptor;
                switch (text) {
                    case first_opt:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "第一位仓管员打卡成功.mp3");
                        play(fileDescriptor);
                        break;
                    case err_upload:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "上传信息有误.mp3");
                        play(fileDescriptor);
                        break;
                    case msg_patrol:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "巡检信息已上传.mp3");
                        play(fileDescriptor);
                        break;
                    case relock_opt:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "仓库已重新设防.mp3");
                        play(fileDescriptor);
                        break;
                    case second_opt:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "第二位仓管员打卡成功，仓库已进入撤防状态.mp3");
                        play(fileDescriptor);
                        break;
                    case err_connect:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "连接服务器失败请检查网络离线数据已保存.mp3");
                        play(fileDescriptor);
                        break;
                    case err_samePerson:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "请不要连续输入同一位管理员的信息.mp3");
                        play(fileDescriptor);
                        break;
                    case err_omtk:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "数据上传失败，请注意是否单人双卡操作.mp3");
                        play(fileDescriptor);
                        break;
                    case msg_visit:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "来访人员信息已上传.mp3");
                        play(fileDescriptor);
                        break;
                    case wait_reupload:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "离线数据过多等待数据上传.mp3");
                        play(fileDescriptor);
                        break;
                    case waiting:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "服务器通信成功，等待用户操作.mp3");
                        play(fileDescriptor);
                        break;
                    case second_err:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "服务器连接失败请检查网络仓库门已进入撤防状态离线数据已保存.mp3");
                        play(fileDescriptor);
                        break;
                    case alarm:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "仓库非法开门，请管理员尽快处理.mp3");
                        play(fileDescriptor);
                        break;
                    case no_registration:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "设备尚未登记请前往系统进行登记操作.mp3");
                        play(fileDescriptor);
                        break;
                    case err_connect_ns:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "服务器连接失败.mp3");
                        play(fileDescriptor);
                        break;
                    case err_connect_relock:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "服务器连接失败请检查网络仓库以重新设防.mp3");
                        play(fileDescriptor);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void play(AssetFileDescriptor fileDescriptor) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public static void mediaRealese() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            Log.e("信息提示", "mediaPlayer解除函数被触发");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}



