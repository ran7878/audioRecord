package com.aispeech.recorddemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.aispeech.recorddemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final static int REQ_CODE = 1;
    private final static String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    private ActivityMainBinding mainBinding = null;
    public int audioFormat = AudioFormat.ENCODING_PCM_16BIT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,permissions,REQ_CODE);
            }else {
                init();
            }
        }else{
            init();
        }

        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED ){
            init();
        }else {
            Toast.makeText(this, "请开启录音和SDCard读写权限", Toast.LENGTH_SHORT).show();
        }
    }
    private void init(){
        mainBinding.btnStart.setOnClickListener(this);
        mainBinding.btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                startRecord();
                break;
            case R.id.btn_stop:
                stopRecord();
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        int checkedId = mainBinding.rgAudio.getCheckedRadioButtonId();
        if (checkedId == mainBinding.rb8k.getId()){
            audioFormat = AudioFormat.ENCODING_PCM_8BIT;
        }
        //开始录音
        RecordAudioManager.getInstance().startRecord(this,audioFormat);

        if (RecordAudioManager.getInstance().isRecording()){
            mainBinding.btnStart.setEnabled(false);
            mainBinding.btnStart.setText("录音中...");
        }
    }

    private void stopRecord() {
        mainBinding.btnStart.setEnabled(true);
        mainBinding.btnStart.setText("开始录音");
        RecordAudioManager.getInstance().stopRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecordAudioManager.getInstance().unInit();
    }
}