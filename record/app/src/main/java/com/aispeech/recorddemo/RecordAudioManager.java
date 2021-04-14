package com.aispeech.recorddemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordAudioManager {
    private static final String TAG = "RecordAudioManager";
    private final HandlerThread mHandlerThread = new HandlerThread("mHandlerThread");
    private Handler recordHandler = null;

    //采样率
    private static final int SAMPLE_RATE_INHZ = 44100;
    //声道数 CHANNEL_IN_MONO and CHANNEL_IN_STEREO
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //音频数据的格式 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording;

    private RecordAudioManager(){
        mHandlerThread.start();
        recordHandler = new Handler(mHandlerThread.getLooper());
    }

    public static RecordAudioManager getInstance(){
        return RecordAudioManagerHolder.INSTANCE;
    }
    private static class RecordAudioManagerHolder{
        private static final RecordAudioManager INSTANCE = new RecordAudioManager();
    }


    /**
     * 开始录音
     * @param audioFormat 音频数据格式
     */
    public void startRecord(Context context,int audioFormat) {
        //录音权限以及读写SDcard权限判断
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //抛出异常
                throw new RuntimeException("请开启录音和SDCard读写权限");
            }
        }

        AUDIO_FORMAT = audioFormat;
        // 获取最小录音缓存大小，
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
        // 初始化缓存
        final byte[] data = new byte[minBufferSize];
        final File file = new File(Environment.getExternalStorageDirectory(), "test_"+System.currentTimeMillis()+".pcm");
        Log.i(TAG, "path:" + file.getAbsolutePath());
        if (!file.exists()){
            try {
                if (file.createNewFile()){
                    // 开始录音
                    audioRecord.startRecording();
                    isRecording = true;
                    // 创建数据流，将缓存导入数据流
                    recordHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Log.e(TAG, "文件未找到");
                            }
                            if (fos == null) return;
                            while (isRecording) {
                                int length = audioRecord.read(data, 0, minBufferSize);
                                if (AudioRecord.ERROR_INVALID_OPERATION != length) {
                                    try {
                                        fos.write(data, 0, length);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            try {
                                // 关闭数据流
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public boolean isRecording(){
        return isRecording;
    }

    /**
     * 释放资源
     */
    public void unInit(){
        mHandlerThread.quit();
        recordHandler.removeCallbacksAndMessages(null);
    }
}
