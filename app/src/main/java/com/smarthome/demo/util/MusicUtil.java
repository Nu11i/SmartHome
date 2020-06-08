package com.smarthome.demo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.SoundPool;
import com.smarthome.demo.R;
public class MusicUtil {
    private SoundPool soundPool;
    private int soundID;
    private  Context context;
    public MusicUtil(Context context){
        this.context=context;

    }
    //初始化点击音效
    @SuppressLint("NewApi")
    public void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(context, R.raw.music, 1);
    }
    //播放音效
    public  void playSound() {
        soundPool.play(
                soundID,
                0.1f,      //左耳道音量
                0.5f,      //右耳道音量
                0,         //播放优先级
                0,         //循环模式
                2         //播放速度
        );
    }
}
