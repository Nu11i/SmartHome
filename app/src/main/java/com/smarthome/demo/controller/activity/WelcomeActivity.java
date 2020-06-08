package com.smarthome.demo.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.smarthome.demo.R;
import com.smarthome.demo.util.ActivityListUtil;
import com.smarthome.demo.util.InternetUtil;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {
    private InternetUtil internetUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        internetUtil = new InternetUtil();
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //设置动画时间 结束 后跳转到指定页面

        final Intent it = new Intent(this, LoginActivity.class);
        Timer timer = new Timer();
        if (internetUtil.NetWorking(this)) {
            Toast.makeText(WelcomeActivity.this, "已连接网络，欢迎进入!", Toast.LENGTH_SHORT).show();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {


                    startActivity(it); //执行

                    finish();//结束欢迎页面

                }


            };
            timer.schedule(task, 1500); //1.5秒后跳转
        }
    }
}
