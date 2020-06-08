package com.smarthome.demo.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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


public class BulbActivity extends AppCompatActivity {
    private Switch switch1;
    private SeekBar seekBar;
    private ProgressDialog progressDialog;
    private MusicUtil musicUtil;
    private MqttUtil mqttUtil;
    private boolean initialization = true;//设置打开页面时加载当前设备的状态
    //开关
    private static final String payloadJson = "{\"id\":%s,\"params\":{\"LED\": %s},\"method\":\"thing/event/property/post\"}";
    //亮度
    private static final String payloadJson2 = "{\"id\":%s,\"params\":{\"LightLuminance\": %s},\"method\":\"thing/event/property/post\"}";

    private String led; //led态
    private String light;//亮度
    private String lightPayload;//亮度消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        showProgressDialog(this, "加载中……");
        setContentView(R.layout.activity_bulb);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
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
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (initialization) {
                    JsonUtil ledJsonUtil = new JsonUtil(msg.getData().getString("Json"), "LED");
                    JsonUtil lightJsonUtil = new JsonUtil(msg.getData().getString("Json"), "LightLuminance");
                    try {
                        led = ledJsonUtil.JsonSolve();
                        light = lightJsonUtil.JsonSolve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (led.equals("1")) {

                        switch1.setEnabled(true);
                        switch1.setChecked(true);
                        seekBar.setEnabled(true);
                        seekBar.setProgress(Integer.parseInt(light));
                    } else {

                        switch1.setEnabled(true);

                    }
                    initialization = false;//界面加载完成，不再接收消息
                    dismissProgressDialog();//加载完成后隐藏加载框
                    Toast.makeText(getApplicationContext(), "加载成功！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mqttUtil = new MqttUtil();

        mqttUtil.init(BulbActivity.this, mUiHandler);

//开关
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                musicUtil.playSound();
                if (isChecked) {
                    if (!initialization) {
                        seekBar.setEnabled(true);

                        seekBar.setProgress(20);

                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 1);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "开灯", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!initialization) {
                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 0);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "关灯", Toast.LENGTH_SHORT).show();
                        seekBar.setEnabled(false);
                        seekBar.setProgress(0);
                    }
                }
            }
        });
//亮度
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置亮度

                lightPayload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), progress);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttUtil.publishMessage(lightPayload);//发布消息
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

