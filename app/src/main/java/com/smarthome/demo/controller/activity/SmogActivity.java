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
import android.widget.TextView;
import android.widget.Toast;

import com.smarthome.demo.R;
import com.smarthome.demo.util.JsonUtil;
import com.smarthome.demo.util.MqttUtil;
import com.smarthome.demo.util.MusicUtil;

import com.smarthome.demo.util.ActivityListUtil;
import org.json.JSONException;


public class SmogActivity extends AppCompatActivity {
    private TextView textView3;
    private Switch switch1;
    private SeekBar seekBar;
    private TextView textView5;
    private TextView textView6;
    private MusicUtil musicUtil;
    private ProgressDialog progressDialog;//加载框
    private ProgressDialog warningDialog;//警告框
    private boolean initialization = true;//设置打开页面时加载当前设备的状态
    private MqttUtil mqttUtil;
    //烟雾阈值
    private static final String payloadJson = "{\"id\":%s,\"params\":{\"Threshold\": %s},\"method\":\"thing/event/property/post\"}";
    //报警开关
    private static final String payloadJson2 = "{\"id\":%s,\"params\":{\"SOUND\": %s},\"method\":\"thing/event/property/post\"}";

    private String sound; //报警器状态
    private String threshold;//烟雾阈值
    private String thresholdPayload;//烟雾阈值消息
    private String smog;//烟雾值
    private String H2; //氢气浓度
    private String LPG;//液化石油气浓度
    private String CO;//一氧化碳浓度
    private String Alcohol; //酒精浓度
    private String Propane;//丙烷浓度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smog);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
        showProgressDialog(this, "加载中……");
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        textView3 = findViewById(R.id.textView3);
        switch1 = findViewById(R.id.switch1);
        seekBar = findViewById(R.id.seekBar);
        textView5 = findViewById(R.id.textView5);
        textView5.setText("300PM");
        textView6 = findViewById(R.id.textView6);
        seekBar.setEnabled(false);
        switch1.setEnabled(false);

        @SuppressLint("HandlerLeak")
        Handler mUiHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                JsonUtil smogJsonUtil = new JsonUtil(msg.getData().getString("Json"), "SMOG");
                JsonUtil h2JsonUtil = new JsonUtil(msg.getData().getString("Json"), "H2");
                JsonUtil LPGJsonUtil = new JsonUtil(msg.getData().getString("Json"), "LPG");
                JsonUtil COJsonUtil = new JsonUtil(msg.getData().getString("Json"), "CO");
                JsonUtil alcoholJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Alcohol");
                JsonUtil propaneJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Propane");
                JsonUtil thresholdJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Threshold");
                JsonUtil soundJsonUtil = new JsonUtil(msg.getData().getString("Json"), "SOUND");
                try {
                    smog = smogJsonUtil.JsonSolve();
                    H2 = h2JsonUtil.JsonSolve();
                    LPG = LPGJsonUtil.JsonSolve();
                    CO = COJsonUtil.JsonSolve();
                    Alcohol = alcoholJsonUtil.JsonSolve();
                    Propane = propaneJsonUtil.JsonSolve();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                textView3.setText(smog + "PPM");
                textView6.setText("氢气浓度：" + H2 + "PPM\n液化石油气浓度：" + LPG + "PPM\n一氧化碳浓度：" + CO + "PPM\n酒精浓度：" + Alcohol + "PPM\n丙烷浓度：" + Propane + "PPM");
                if (initialization) {
                    try {
                        threshold = thresholdJsonUtil.JsonSolve();
                        sound = soundJsonUtil.JsonSolve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (sound.equals("1")) {
                        switch1.setChecked(true);
                        seekBar.setEnabled(true);
                        switch1.setEnabled(true);
                        seekBar.setProgress(Integer.parseInt(threshold));
                        textView5.setText(threshold + "PPM");
                    } else
                        switch1.setEnabled(true);
                    initialization = false;
                    dismissProgressDialog();//加载完成后隐藏加载框
                    Toast.makeText(getApplicationContext(), "加载成功！", Toast.LENGTH_SHORT).show();
                }
            }

        };
        mqttUtil = new MqttUtil();

        mqttUtil.init(SmogActivity.this, mUiHandler);


        //报警器开关
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                musicUtil.playSound();
                if (isChecked) {
                    seekBar.setEnabled(true);
                    seekBar.setProgress(10000);
                    textView5.setText("10000PPM");
                    String payload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), 1);
                    mqttUtil.publishMessage(payload);//发布消息
                    Toast.makeText(getApplicationContext(), "报警器启动", Toast.LENGTH_SHORT).show();
                } else {
                    String payload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), 0);
                    mqttUtil.publishMessage(payload);//发布消息
                    Toast.makeText(getApplicationContext(), "报警器关闭", Toast.LENGTH_SHORT).show();
                    seekBar.setEnabled(false);
                    seekBar.setProgress(10000);
                }
            }

            ;
        });
        //阈值设置
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresholdPayload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), progress + 300);
                textView5.setText(String.valueOf(progress + 300) + "PPM");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttUtil.publishMessage(thresholdPayload);//发布消息
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
