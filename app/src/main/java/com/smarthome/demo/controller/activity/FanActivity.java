package com.smarthome.demo.controller.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.smarthome.demo.R;
import com.smarthome.demo.util.JsonUtil;
import com.smarthome.demo.util.MqttUtil;
import com.smarthome.demo.util.MusicUtil;


import com.smarthome.demo.util.ActivityListUtil;
import org.json.JSONException;


public class FanActivity extends AppCompatActivity {
    private Switch switch1;
    private SeekBar seekBar;
    private MusicUtil musicUtil;
    private ProgressDialog progressDialog;
    private boolean initialization = true;//设置打开页面时加载当前设备的状态
    private MqttUtil mqttUtil;
    //开关
    private static final String payloadJson = "{\"id\":%s,\"params\":{\"FAN\": %s},\"method\":\"thing/event/property/post\"}";
    //风速
    private static final String payloadJson2 = "{\"id\":%s,\"params\":{\"WindSpeed\": %s},\"method\":\"thing/event/property/post\"}";
    private String fan;//风扇状态
    private String fanSpeed;//风速
    private String fanSpeedPayload;//风速消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
        showProgressDialog(this, "加载中……");
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        switch1 = findViewById(R.id.switch1);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        switch1.setEnabled(false);
        @SuppressLint("HandlerLeak")
        Handler mUiHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (initialization) {
                    JsonUtil fanJsonUtil = new JsonUtil(msg.getData().getString("Json"), "FAN");
                    JsonUtil fanSpeedJsonUtil = new JsonUtil(msg.getData().getString("Json"), "WindSpeed");
                    try {
                        fan = fanJsonUtil.JsonSolve();

                        fanSpeed = fanSpeedJsonUtil.JsonSolve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (fan.equals("1")) {
                        switch1.setChecked(true);
                        seekBar.setEnabled(true);
                        switch1.setEnabled(true);
                        seekBar.setProgress(Integer.parseInt(fanSpeed)-300);
                    } else
                        switch1.setEnabled(true);
                    initialization = false;//界面加载完成，不再接收消息
                    dismissProgressDialog();//加载完成后隐藏加载框
                    Toast.makeText(getApplicationContext(), "加载成功！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mqttUtil = new MqttUtil();

        mqttUtil.init(FanActivity.this, mUiHandler);


        //开关
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                musicUtil.playSound();
                if (isChecked) {
                    if (!initialization) {
                        seekBar.setEnabled(true);
                        seekBar.setProgress(200);
                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 1);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "打开风扇", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!initialization) {
                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 0);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "关闭风扇", Toast.LENGTH_SHORT).show();
                        seekBar.setEnabled(false);
                        seekBar.setProgress(0);
                    }
                }
            }
        });
//风速
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress == 0)
                    fanSpeedPayload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), progress);
                else
                    fanSpeedPayload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), progress + 300);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttUtil.publishMessage(fanSpeedPayload);//发布消息
            }
        });

    }

    public void showProgressDialog(Context mContext, String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);    //设置内容
        progressDialog.setCancelable(false);//点击屏幕和按返回键都不能取消加载框
        progressDialog.show();

        //设置超时自动消失
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //取消加载框
                if (dismissProgressDialog()) {
                    Toast.makeText(getApplicationContext(), "加载失败，设备未连接，请检查！", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }, 20000);//超时时间
    }

    public Boolean dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                return true;//取消成功
            }
        }
        return false;//已经取消过了，不需要取消
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttUtil.disconnect();
    }
}
