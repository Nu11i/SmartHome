package com.smarthome.demo.controller.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.smarthome.demo.R;
import com.smarthome.demo.controller.activity.MainActivity;
import com.smarthome.demo.util.JsonUtil;
import com.smarthome.demo.util.MqttUtil;
import com.smarthome.demo.util.MusicUtil;

import org.json.JSONException;


public class SceneFragment extends Fragment {
    private ImageButton in, out, open, close;
    private Button aboutCurtain;
    private TextView textView;
    private boolean k = true;
    private MusicUtil musicUtil;
    private ProgressDialog progressDialog;
    private MqttUtil mqttUtil;
    //发布消息
    private static final String payloadJson = "{\"id\":%s,\"params\":{\"FAN\": %s,\"Curtain\":%s,\"LED\":%s,\"SOUND\":%s,\"TIME\":%s},\"method\":\"thing/event/property/post\"}";
    private String led; //led状态
    private String fan;//风扇状态
    private String curtain;//窗帘状态
    private String sound;//报警器状态
    private String time;//定时器状态

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scene, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        in = view.findViewById(R.id.in);
        out = view.findViewById(R.id.out);
        open = view.findViewById(R.id.open);
        close = view.findViewById(R.id.close);
        aboutCurtain = view.findViewById(R.id.aboutCurtain);
        textView = view.findViewById(R.id.textView5);
        musicUtil = new MusicUtil(getActivity());
        musicUtil.initSound();
        showProgressDialog(getActivity(), "加载中……");
        String s = "当前场景：";
        @SuppressLint("HandlerLeak")
        Handler mUiHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                JsonUtil ledJsonUtil = new JsonUtil(msg.getData().getString("Json"), "LED");
                JsonUtil timeJsonUtil = new JsonUtil(msg.getData().getString("Json"), "TIME");
                JsonUtil fanJsonUtil = new JsonUtil(msg.getData().getString("Json"), "FAN");
                JsonUtil soundJsonUtil = new JsonUtil(msg.getData().getString("Json"), "SOUND");
                JsonUtil curtainJsonUtil = new JsonUtil(msg.getData().getString("Json"), "Curtain");
                try {
                    led = ledJsonUtil.JsonSolve();
                    time = timeJsonUtil.JsonSolve();
                    fan = fanJsonUtil.JsonSolve();
                    sound = soundJsonUtil.JsonSolve();
                    curtain = curtainJsonUtil.JsonSolve();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (led.equals("1") && fan.equals("1") && sound.equals("1") && curtain.equals("1") && time.equals("1"))
                    textView.setText(s + "全开");
                else if (led.equals("0") && fan.equals("0") && sound.equals("0") && curtain.equals("0") && time.equals("0"))
                    textView.setText(s + "全关");
                else if (led.equals("1") && fan.equals("1") && sound.equals("0") && curtain.equals("1") && time.equals("0"))
                    textView.setText(s + "回家");
                else if (led.equals("0") && fan.equals("0") && sound.equals("1") && curtain.equals("0") && time.equals("0"))
                    textView.setText(s + "离家");
                else
                    textView.setText(s + "未选择");
                if (k) {
                    dismissProgressDialog();//加载完成后隐藏加载框
                    Toast.makeText(getActivity(), "加载成功！", Toast.LENGTH_SHORT).show();
                    k = false;
                }
            }
        };
        mqttUtil = new MqttUtil();

        mqttUtil.init(getActivity(), mUiHandler);


        //回家
        in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 1, 1, 1, 0, 0);
                mqttUtil.publishMessage(payload);//发布消息
                Toast.makeText(getActivity(), "回家", Toast.LENGTH_SHORT).show();
                textView.setText(s + "回家");
            }
        });

        //离家
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 0, 0, 0, 1, 0);
                mqttUtil.publishMessage(payload);//发布消息
                Toast.makeText(getActivity(), "离家", Toast.LENGTH_SHORT).show();
                textView.setText(s + "离家");
            }
        });
        //全开
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 1, 1, 1, 1, 1);
                mqttUtil.publishMessage(payload);//发布消息
                Toast.makeText(getActivity(), "全开", Toast.LENGTH_SHORT).show();
                textView.setText(s + "全开");
            }
        });
        //全关
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), 0, 0, 0, 0, 0);
                mqttUtil.publishMessage(payload);//发布消息
                Toast.makeText(getActivity(), "全关", Toast.LENGTH_SHORT).show();
                textView.setText(s + "全关");
            }
        });
        aboutCurtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openCurtainAbout();

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

                    ((MainActivity) getActivity()).fail();
                    getActivity().onBackPressed();
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
    public void onDestroy() {
        super.onDestroy();
        mqttUtil.disconnect();
    }
}
