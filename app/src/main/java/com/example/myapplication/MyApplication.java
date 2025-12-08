package com.example.myapplication;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // APP冷启动时重置音乐播放状态
        com.example.myapplication.MusicPlayerManager.getInstance(this).resetMuteState();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // 应用终止时释放音乐播放器
        com.example.myapplication.MusicPlayerManager.getInstance(this).release();
    }
}
