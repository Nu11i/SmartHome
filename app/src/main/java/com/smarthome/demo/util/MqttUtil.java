package com.smarthome.demo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smarthome.demo.model.MqttOptionModel;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;


public class MqttUtil {
    private static final String TAG = "MQTT";
    //三元组信息
    private String PRODUCTKEY = "a1PqXtaIQTd";
    private String DEVICENAME = "SmartHome_App";
    private String DEVICESECRET = "ZhLoL2KY97tyX6b2AuGa6l7ABiLza7PC";
    //发布的消息
    private final String PUB_TOPIC = "/sys/" + PRODUCTKEY + "/" + DEVICENAME + "/thing/event/property/post";
    //订阅的消息
    private final String SUB_TOPIC = "/sys/" + PRODUCTKEY + "/" + DEVICENAME + "/thing/service/property/set";
    //阿里云服务器域名
    String host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
    private String clientId;
    private String userName;
    private String passWord;
    @SuppressLint("StaticFieldLeak")
    static MqttAndroidClient mqttAndroidClient;
    static MqttConnectOptions mqttConnectOptions;
    private Context mContext;
    private Handler mHandler;


    //初始化
    public void init(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        MqttOptionModel mqttOption = new MqttOptionModel().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (mqttOption == null) {
            Log.e(TAG, "错误");
        } else {
            clientId = mqttOption.getClientId();
            userName = mqttOption.getUsername();
            passWord = mqttOption.getPassword();
        }
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());
        mqttAndroidClient = new MqttAndroidClient(mContext, host, clientId);
        mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
        doClientConnection();//连接

    }

    //连接MQTT
    private void doClientConnection() {
        try {
            if (!mqttAndroidClient.isConnected()) {
                Log.i(TAG, "连接MQTT服务器");
                mqttAndroidClient.connect(mqttConnectOptions, null, iMqttActionListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //监听MQTT是否连接成功
    private final IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                if (mqttAndroidClient != null) {
                    mqttAndroidClient.subscribe(SUB_TOPIC, 1);//订阅主题
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i(TAG, "连接失败 ");
            doClientConnection();//重连
        }
    };

    // 向主题发布消息
    public void publishMessage(String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            mqttAndroidClient.publish(PUB_TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "发布成功!" + payload);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "发布失败!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    //消息回调
    private final MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage msgStr) throws Exception {

            try {
                String enCodeMsg = new String(msgStr.getPayload());
                Log.i(TAG, "消息： " + enCodeMsg);
                Bundle data = new Bundle();
                data.putString("Json", enCodeMsg);
                Message msg = new Message();
                msg.setData(data);
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "连接断开 ");
            doClientConnection();//连接断开，重连
        }
    };

    //断开连接
    public void disconnect() {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unsubscribe(PUB_TOPIC);
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.disconnect(0); //断开连接
                mqttAndroidClient = null;
                Log.i(TAG, "连接断开 ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
