package com.smarthome.demo.controller.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.smarthome.demo.R;


import com.smarthome.demo.util.*;
import org.json.JSONException;


public class CurtainActivity extends AppCompatActivity {
    private Switch switch1, switch2;
    private ProgressDialog progressDialog;
    private TimePicker mTimepicker;
    private TextView textView;
    private MusicUtil musicUtil;
    private MqttUtil mqttUtil;
    private TimeUtil currentTimeUtil;
    private boolean initialization  = true;//设置打开页面时加载当前设备的状态
    private boolean s = false;
    private boolean a = true;
    //开关
    private static final String payloadJson = "{\"id\":%s,\"params\":{\"Curtain\": %s},\"method\":\"thing/event/property/post\"}";
    private static final String payloadJson2 = "{\"id\":%s,\"params\":{\"TIME\": %s},\"method\":\"thing/event/property/post\"}";
    private static final String payloadJson3 = "{\"id\":%s,\"params\":{\"Runtime\": %s},\"method\":\"thing/event/property/post\"}";
    private String curtain;//窗帘开关
    private String time;//定时器开关
    private String dateTime;//时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityListUtil.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain);
        musicUtil = new MusicUtil(this);
        currentTimeUtil =new TimeUtil("HH:mm");
        musicUtil.initSound();
        showProgressDialog(this, "加载中……");
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        switch1 = findViewById(R.id.switch1);
        switch1.setEnabled(false);
        switch2 = findViewById(R.id.switch2);
        switch2.setEnabled(false);
        mTimepicker = findViewById(R.id.timepicker);
        textView=findViewById(R.id.textView4);
        @SuppressLint("HandlerLeak")
        Handler mUiHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(Message msg) {
                textView.setText(currentTimeUtil.GetTime());
                super.handleMessage(msg);
                JsonUtil curtainJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Curtain");
                JsonUtil timeJsonUtil = new JsonUtil(msg.getData().getString("Json"), "TIME");
                JsonUtil dateTimeJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Runtime");
                if (s) {
                    try {
                        curtain = curtainJsonUtil.JsonSolve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (curtain.equals("1")) {
                        s = false;
                        a = false;
                        switch1.setChecked(true);
                        a = true;
                    }
                }
                if (initialization) {
                    try {
                        curtain = curtainJsonUtil.JsonSolve();

                        time = timeJsonUtil.JsonSolve();
                        dateTime = dateTimeJsonUtil.JsonSolve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int t = Integer.parseInt(dateTime);
                    if (t == 0) {
                        mTimepicker.setHour(0);
                        mTimepicker.setMinute(0);
                    } else {
                        mTimepicker.setHour(t / 100);
                        mTimepicker.setMinute(t - (t / 100) * 100);
                    }
                    if (curtain.equals("1")) {
                        switch1.setEnabled(true);
                        switch1.setChecked(true);

                    } else
                        switch1.setEnabled(true);
                    if (time.equals("1")) {
                        switch2.setEnabled(true);
                        switch2.setChecked(true);
                        mTimepicker.setEnabled(false);

                    } else {
                        switch2.setEnabled(true);
                        mTimepicker.setEnabled(true);
                    }
                    initialization = false;//界面加载完成，不再接收消息
                    dismissProgressDialog();//加载完成后隐藏加载框
                    Toast.makeText(getApplicationContext(), "加载成功！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mqttUtil = new MqttUtil();

        mqttUtil.init(CurtainActivity.this, mUiHandler);


        //定时
        mTimepicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);  //设置点击事件不弹键盘
        mTimepicker.setIs24HourView(true);   //设置时间显示为24小时
        mTimepicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  //获取当前选择的时间
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                dateTime = String.valueOf(hourOfDay * 100 + minute);

            }
        });
        //定时器开关
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                musicUtil.playSound();
                if (isChecked) {
                    if (!initialization) {
                        String payload = String.format(payloadJson3, String.valueOf(System.currentTimeMillis()), dateTime);
                        mqttUtil.publishMessage(payload);//发布消息
                        mTimepicker.setEnabled(false);
                        String payload2 = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), 1);
                        mqttUtil.publishMessage(payload2);//发布消息
                        Toast.makeText(getApplicationContext(), "打开定时器", Toast.LENGTH_SHORT).show();
                        s = true;
                    }
                } else {
                    if (!initialization) {
                        mTimepicker.setEnabled(true);
                        String payload = String.format(payloadJson2, String.valueOf(System.currentTimeMillis()), 0);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "关闭定时器", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //窗帘开关
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                musicUtil.playSound();
                if (isChecked) {
                    if (!initialization && a) {
                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 1);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "打开窗帘", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!initialization && a) {
                        String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 0);
                        mqttUtil.publishMessage(payload);//发布消息
                        Toast.makeText(getApplicationContext(), "关闭窗帘", Toast.LENGTH_SHORT).show();
                    }
                }
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
