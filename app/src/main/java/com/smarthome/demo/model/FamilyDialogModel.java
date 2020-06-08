package com.smarthome.demo.model;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.smarthome.demo.R;

public class FamilyDialogModel extends Dialog {
    private Button yes; //确定按钮
    private Button no; //取消按钮
    private TextView message; //消息提示文本
    private String titleStr; //从外界设置的title文本
    private String messageStr; //从外界设置的消息文本
    private String yesStr, noStr; //确定文本和取消文本的显示内容
    private Window window = null;
    private onYesOnClickListener yesOnClickListener; //确定按钮被点击了的监听器
    private onNoClickListener noOnClickListener; //取消按钮被点击了的监听器


    public FamilyDialogModel(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_family);
        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
        //设置窗口显示
        windowDeploy();
    }

    // 初始化界面控件
    private void initView() {
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        message = findViewById(R.id.message);
    }

    //初始化界面控件的显示数据

    private void initData() {

        if (messageStr != null) {
            message.setText(messageStr);
        }
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        if (noStr != null) {
            no.setText(noStr);
        }
    }

    //初始化界面的确定和取消监听器
    private void initEvent() {
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (yesOnClickListener != null) {
                    yesOnClickListener.onYesClick();
                }
            }
        });

        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noOnClickListener != null) {
                    noOnClickListener.onNoClick();
                }
            }
        });
    }

    private void windowDeploy() {
        window = getWindow();
        window.setGravity(Gravity.CENTER); //设置窗口显示位置
    }

    //设置确定按钮的显示内容和监听
    public void setYesOnClickListener(String str, onYesOnClickListener onYesOnClickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnClickListener = onYesOnClickListener;
    }

    //设置取消按钮的显示内容和监听

    public void setNoOnClickListener(String str, onNoClickListener onNoClickListener) {
        if (str != null) {
            noStr = str;
        }
        this.noOnClickListener = onNoClickListener;
    }

    //从外界Activity为Dialog设置标题

    public void setTitle(String title) {
        titleStr = title;
    }

    //从外界Activity为Dialog设置dialog的message

    public void setMessage(String message) {
        messageStr = message;
    }

    //设置确定按钮和取消被点击的接口

    public interface onYesOnClickListener {
        void onYesClick();
    }

    public interface onNoClickListener {
        void onNoClick();
    }

}
