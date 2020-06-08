package com.smarthome.demo.controller.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.smarthome.demo.R;
import com.smarthome.demo.util.ActivityListUtil;
import com.smarthome.demo.util.JsonUtil;
import com.smarthome.demo.util.MqttUtil;
import org.json.JSONException;

public class TemperatureActivity extends AppCompatActivity {
  private TextView textView3, textView4;
  private ProgressDialog progressDialog;
  private SeekBar seekBar1, seekBar2;
  private boolean initialization = true; // 设置打开页面时加载当前设备的状态
  private MqttUtil mqttUtil;
  private String humidity;
  private String temperature;
  private ProgressBar progressBar1, progressBar2;
  final int FLAG = 0x001;
  private Message message;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ActivityListUtil.getInstance().addActivity(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_temperature);
    showProgressDialog(this, "加载中……");
    // 隐藏标题栏
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }
    // 隐藏状态栏
    this.getWindow()
        .setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    textView3 = findViewById(R.id.textView3);
    textView4 = findViewById(R.id.textView4);
    seekBar1 = findViewById(R.id.seekBar1);
    seekBar2 = findViewById(R.id.seekBar2);
    progressBar1 = findViewById(R.id.progressBar);
    progressBar2 = findViewById(R.id.progressBar2);
    progressBar1.setVisibility(View.INVISIBLE);
    progressBar2.setVisibility(View.INVISIBLE);
    seekBar1.setEnabled(false);
    seekBar2.setEnabled(false);

    @SuppressLint("HandlerLeak")
    Handler mUiHandler =
        new Handler() {
          @RequiresApi(api = Build.VERSION_CODES.M)
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (initialization) {
              message = Message.obtain();
              message.what = FLAG;
              handler.sendMessage(message);
              dismissProgressDialog(); // 加载完成后隐藏加载框
              Toast.makeText(getApplicationContext(), "加载成功！", Toast.LENGTH_SHORT).show();
              initialization = false; // 界面加载完成，不再显示加载框
            }
            JsonUtil temperatureJsonUtil =
                new JsonUtil(msg.getData().getString("Json"), "CurrentTemperature");
            JsonUtil humidityJsonUtil =
                new JsonUtil(msg.getData().getString("Json"), "CurrentHumidity");
            try {
              temperature = temperatureJsonUtil.JsonSolve();

              humidity = humidityJsonUtil.JsonSolve();
            } catch (JSONException e) {
              e.printStackTrace();
            }
            textView4.setText(temperature + "℃");
            if (temperature.indexOf(".") > 0) // 温度数值存在小数点
            {
              String[] splitAddress = temperature.split("\\.");
              seekBar1.setProgress(Integer.parseInt(splitAddress[0] + splitAddress[1]));
            } else // 温度数值为整数
            seekBar1.setProgress(Integer.parseInt(temperature) * 10);
            textView3.setText(humidity + "%RH");
            seekBar2.setProgress(Integer.parseInt(humidity) - 20);
          }
        };
    mqttUtil = new MqttUtil();

    mqttUtil.init(TemperatureActivity.this, mUiHandler);
  }

  public void showProgressDialog(Context mContext, String text) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(mContext);
      progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
    progressDialog.setMessage(text); // 设置内容
    progressDialog.setCancelable(false); // 点击屏幕和按返回键都不能取消加载框
    progressDialog.show();

    // 设置超时自动消失
    new Handler()
        .postDelayed(
            new Runnable() {
              @Override
              public void run() {
                // 取消加载框
                if (dismissProgressDialog()) {
                  Toast.makeText(getApplicationContext(), "加载失败，设备未连接，请检查！", Toast.LENGTH_SHORT)
                      .show();
                  finish();
                }
              }
            },
            20000); // 超时时间
  }

  public Boolean dismissProgressDialog() {
    if (progressDialog != null) {
      if (progressDialog.isShowing()) {
        progressDialog.dismiss();
        return true; // 取消成功
      }
    }
    return false; // 已经取消过了，不需要取消
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mqttUtil.disconnect();
  }

  @SuppressLint("HandlerLeak")
  private Handler handler =
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          if (msg.what == FLAG) {
            if (progressBar1.getVisibility() == View.VISIBLE) {
              progressBar1.setVisibility(View.INVISIBLE);
              progressBar2.setVisibility(View.INVISIBLE);
            } else {
              progressBar1.setVisibility(View.VISIBLE);
              progressBar2.setVisibility(View.VISIBLE);
            }
            message = handler.obtainMessage(FLAG);
            handler.sendMessageDelayed(message, 2000);
          }
        }
      };
}
