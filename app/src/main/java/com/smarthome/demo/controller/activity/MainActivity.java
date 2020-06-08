package com.smarthome.demo.controller.activity;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


import com.smarthome.demo.R;
import com.smarthome.demo.controller.fragment.DeviceFragment;
import com.smarthome.demo.controller.fragment.SceneFragment;
import com.smarthome.demo.controller.fragment.SettingFragment;
import com.smarthome.demo.util.MusicUtil;
import com.smarthome.demo.util.ActivityListUtil;
import com.smarthome.demo.util.ShowDialogUtil;


public class MainActivity extends AppCompatActivity {
    private ImageButton deviceButton, sceneButton, settingButton;
    private DeviceFragment deviceFragment;
    private SceneFragment sceneFragment;
    private SettingFragment settingFragment;
    private MusicUtil musicUtil;
    private String phone;//登录转发的账号
    private int currentPage = 1;//判断当前的fragment,1表示deviceFragment，2表示sceneFragment，剩余表示settingFragment


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        deviceButton = findViewById(R.id.deviceButton);
        sceneButton = findViewById(R.id.sceneButton);
        settingButton = findViewById(R.id.settingButton);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
        Bundle bundle = getIntent().getExtras();
        phone = bundle.getString("phone");
        deviceFragment = new DeviceFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentBox, deviceFragment).commitAllowingStateLoss();


        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                musicUtil.playSound();
                if (currentPage == 1) {
                    getSupportFragmentManager().beginTransaction().remove(deviceFragment).commit();
                } else if (currentPage == 2) {
                    getSupportFragmentManager().beginTransaction().remove(sceneFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().remove(settingFragment).commit();
                }
                deviceFragment = new DeviceFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentBox, deviceFragment).commitAllowingStateLoss();


            }
        });
        sceneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                if (currentPage == 1) {
                    getSupportFragmentManager().beginTransaction().remove(deviceFragment).commit();
                } else if (currentPage == 2) {
                    getSupportFragmentManager().beginTransaction().remove(sceneFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().remove(settingFragment).commit();
                }
                sceneFragment = new SceneFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentBox, sceneFragment).commitAllowingStateLoss();
            }
        });
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                if (currentPage == 1) {
                    getSupportFragmentManager().beginTransaction().remove(deviceFragment).commit();
                } else if (currentPage == 2) {
                    getSupportFragmentManager().beginTransaction().remove(sceneFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().remove(settingFragment).commit();
                }
                settingFragment = new SettingFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentBox, settingFragment).commitAllowingStateLoss();
            }
        });

    }


    public void onBackPressed() {
        new ShowDialogUtil().show(this, "退出系统？", new ShowDialogUtil.OnBottomClickListener() {
            @Override
            public void positive() {
                musicUtil.playSound();
                finish();
            }

            @Override
            public void negative() {
                musicUtil.playSound();
            }
        });

    }

    public void openUserActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone", phone);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void openAbout() {


        new ShowDialogUtil().show(this, "应用名称：SmartHome\n版本号：V1.0\n开发人员：NU11\n\n\n使用说明：\n设备管理->通关开关控制相应的家居设备的状态\n\n" +
                "智能场景->根据用户的生活状态，选择相应的场景，可以快速便捷地操控家居设备\n\n我的设置->可以查看用户的相关信息，修改密码，查看应用相关内容", new ShowDialogUtil.OnBottomClickListener() {
            @Override
            public void positive() {
                musicUtil.playSound();

            }

            @Override
            public void negative() {
                musicUtil.playSound();
            }
        });

    }

    public void openCurtainAbout() {

        new ShowDialogUtil().show(this, "全开：打开风扇、打开灯光、打开烟雾报警器、打开窗帘、打开窗帘定时器\n\n全关：关闭风扇、关闭灯光、关闭烟雾报警器、关闭窗帘、关闭窗帘定时器\n\n" +
                "回家:  打开风扇、打开灯光、打开窗帘、关闭烟雾报警器、关闭窗帘定时器\n\n离家：关闭风扇、关闭灯光、关闭窗帘、打开烟雾报警器、关闭窗帘定时器", new ShowDialogUtil.OnBottomClickListener() {
            @Override
            public void positive() {
                musicUtil.playSound();

            }

            @Override
            public void negative() {
                musicUtil.playSound();
            }
        });
    }

    public void openBulbActivity() {
        Intent intent = new Intent(MainActivity.this, BulbActivity.class);
        startActivity(intent);
    }

    public void openTemperatureActivity() {
        Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
        startActivity(intent);
    }

    public void openCurtainActivity() {
        Intent intent = new Intent(MainActivity.this, CurtainActivity.class);
        startActivity(intent);
    }

    public void openFanActivity() {
        Intent intent = new Intent(MainActivity.this, FanActivity.class);
        startActivity(intent);
    }

    public void openSmogActivity() {
        Intent intent = new Intent(MainActivity.this, SmogActivity.class);
        startActivity(intent);
    }

    public void fail() {
        deviceFragment = new DeviceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentBox, deviceFragment).commit();
        Toast.makeText(this, "加载失败，设备未连接，请检查！", Toast.LENGTH_SHORT).show();
    }

}